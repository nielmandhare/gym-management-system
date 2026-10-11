import connection.MySQL;
import security.*;
import gui.*;
import panels.*;
import java.awt.*;
import java.lang.reflect.*;
import java.sql.*;
import javax.swing.*;

public class AccountVerification {
    static int checks;
    static void check(boolean value,String text) { if(!value) throw new AssertionError(text);checks++;System.out.println("PASS: "+text); }
    interface Action { void run() throws Exception; }
    static void rejected(Action action,String text) throws Exception {
        try { action.run();throw new AssertionError(text+" was allowed"); }
        catch(SecurityException|IllegalArgumentException|SQLIntegrityConstraintViolationException expected){check(true,text);}
    }
    static Object field(Object o,String name) throws Exception { Field f=o.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(o); }
    static void call(Object o,String name) throws Exception { Method m=o.getClass().getDeclaredMethod(name);m.setAccessible(true);m.invoke(o); }
    static void text(Object o,String name,String value) throws Exception {((javax.swing.text.JTextComponent)field(o,name)).setText(value);}
    static int scalar(String sql,Object... params) throws Exception {try(ResultSet r=MySQL.executeSearch(sql,params)){r.next();return r.getInt(1);}}
    static String string(String sql,Object... params) throws Exception {try(ResultSet r=MySQL.executeSearch(sql,params)){r.next();return r.getString(1);}}
    static void windows() {for(Window w:Window.getWindows())w.dispose();}
    static void cleanup() throws Exception {
        Accounts.login("admin","123");
        MySQL.executeIUD("DELETE FROM personal_training WHERE trainer_trainer_id IN (SELECT trainer_id FROM trainer WHERE username='verify_trainer')");
        MySQL.executeIUD("DELETE FROM trainer_attendance WHERE trainer_trainer_id IN (SELECT trainer_id FROM trainer WHERE username='verify_trainer')");
        MySQL.executeIUD("DELETE FROM trainer WHERE username='verify_trainer'");
        MySQL.executeIUD("DELETE FROM app_user WHERE username IN ('verify_admin','verify_editor','verify_duplicate')");
    }
    public static void main(String[] args) throws Exception {
        if(args.length>0) {
            Accounts.login("verify_trainer","Changed123!");
            check(Session.current().role().equals("TRAINER"),"trainer login persists in fresh JVM");
            check(Accounts.trainerData("Profile").get(0)[1].equals("Verified Trainer"),"profile update persists in fresh JVM");
            SwingUtilities.invokeAndWait(()->{TrainerDashboard d=new TrainerDashboard();d.setVisible(true);check(d.isShowing(),"trainer dashboard after application restart");d.dispose();});
            System.exit(0);
        }
        javax.swing.Timer dialogs=new javax.swing.Timer(100,e->{for(Window w:Window.getWindows()) if(w instanceof JDialog d)closeDialogs(d);});
        dialogs.start();int code=0;
        try {
            com.formdev.flatlaf.themes.FlatMacDarkLaf.setup();cleanup();
            check(Session.current().role().equals("ADMIN"),"existing admin login");
            check(string("SELECT password FROM admin WHERE username='admin'").equals("MIGRATED"),"legacy admin plaintext removed");
            String adminHash=string("SELECT password_hash FROM app_user WHERE username='admin'");
            check(Passwords.verify("123",adminHash),"admin hash verifies simplified password");
            Accounts.migrateLegacyAdmins();check(adminHash.equals(string("SELECT password_hash FROM app_user WHERE username='admin'")),"hash migration idempotent");
            String first=Passwords.hash("SamePassword123!"),second=Passwords.hash("SamePassword123!");
            check(!first.equals(second) && Passwords.verify("SamePassword123!",first),"random salt and password verification");
            rejected(()->Accounts.login("' OR 1=1 --","123"),"login injection rejected");Accounts.login("admin","123");
            rejected(()->Accounts.save(null,"bad","", "123","ADMIN",true,null,"NewPassword123!"),"required field validation");
            SwingUtilities.invokeAndWait(()->{
                try {
                    UserManagementPanel panel=new UserManagementPanel();check(((JTable)field(panel,"table")).getRowCount()>0,"admin user table");
                    text(panel,"username","verify_admin");text(panel,"name","Verify Admin");text(panel,"mobile","9876999901");text(panel,"password","Initial123!");
                    call(panel,"save");check(scalar("SELECT COUNT(*) FROM app_user WHERE username='verify_admin'")==1,"user creation through real Swing form");
                    text(panel,"search","verify_admin");panel.refresh();check(((JTable)field(panel,"table")).getRowCount()==1,"user search in real Swing form");
                    JTable table=(JTable)field(panel,"table");table.setRowSelectionInterval(0,0);text(panel,"name","Edited Admin");call(panel,"save");
                    check(string("SELECT display_name FROM app_user WHERE username='verify_admin'").equals("Edited Admin"),"user edit through real Swing form");
                } catch(Exception e){throw new RuntimeException(e);}
            });
            int aid=scalar("SELECT user_id FROM app_user WHERE username='verify_admin'");
            rejected(()->Accounts.save(null,"verify_admin","Duplicate","9876999902","ADMIN",true,null,"Initial123!"),"duplicate username rejected");
            Accounts.save(aid,"verify_admin","Edited Admin","9876999901","ADMIN",false,null,"");
            rejected(()->Accounts.login("verify_admin","Initial123!"),"inactive account rejected");Accounts.login("admin","123");
            Accounts.save(aid,"verify_admin","Edited Admin","9876999901","ADMIN",true,null,"");
            Accounts.resetPassword(aid,"Reset123!");rejected(()->Accounts.login("verify_admin","Initial123!"),"old password rejected after reset");
            Accounts.login("verify_admin","Reset123!");check(Session.current().role().equals("ADMIN"),"password reset works");Accounts.login("admin","123");
            check(!string("SELECT password_hash FROM app_user WHERE user_id=?",aid).contains("Reset123!"),"new account stores only hash");
            int freeTrainer=scalar("SELECT trainer_id FROM trainer WHERE trainer_id NOT IN (SELECT trainer_id FROM app_user WHERE trainer_id IS NOT NULL) LIMIT 1");
            Accounts.save(aid,"verify_admin","Edited Admin","9876999901","TRAINER",true,freeTrainer,"");
            Accounts.login("verify_admin","Reset123!");check(Session.current().role().equals("TRAINER"),"administrator assigns trainer role and profile");
            Accounts.login("admin","123");Accounts.save(aid,"verify_admin","Edited Admin","9876999901","ADMIN",true,null,"");
            Accounts.login("verify_admin","Reset123!");check(Session.current().role().equals("ADMIN"),"administrator reassigns admin role");Accounts.login("admin","123");
            rejected(()->Accounts.createTrainer("Failed Trainer","9876999906","990099990006","2026-10-11","verify_admin","Initial123!"),"conflicting trainer account rejected");
            check(scalar("SELECT COUNT(*) FROM trainer WHERE mobile='9876999906'")==0,"failed trainer/account creation leaves no partial profile");
            int original=Session.current().userId();
            rejected(()->Accounts.save(original,"admin","Local Admin","0770000001","ADMIN",false,null,""),"self deactivation rejected");
            Accounts.createTrainer("Test Trainer","9876999903","990099990003","2026-10-11","verify_trainer","Trainer123!");
            int tid=scalar("SELECT trainer_id FROM trainer WHERE username='verify_trainer'"),uid=scalar("SELECT user_id FROM app_user WHERE username='verify_trainer'");
            check(tid>0 && uid>0,"atomic trainer profile and login creation");
            check(string("SELECT password FROM trainer WHERE trainer_id=?",tid).equals("PROFILE_ONLY"),"trainer profile contains no plaintext password");
            rejected(()->Accounts.save(null,"verify_duplicate","Duplicate Link","9876999904","TRAINER",true,tid,"Initial123!"),"duplicate trainer profile link rejected");
            check(scalar("SELECT COUNT(*) FROM app_user WHERE username='verify_duplicate'")==0,"failed account operation rolled back");
            MySQL.executeIUD("INSERT INTO personal_training(member_member_id,trainer_trainer_id,start_date,session_price,total_sessions,status) VALUES(1,?,'2026-10-11',500,10,'ACTIVE')",tid);
            MySQL.executeIUD("INSERT INTO trainer_attendance(trainer_trainer_id,date,check_in_time,check_out_time) VALUES(?,'2026-10-11','09:00:00','10:00:00')",tid);
            Accounts.save(uid,"verify_trainer","Test Trainer","9876999903","TRAINER",false,tid,"");
            rejected(()->Accounts.login("verify_trainer","Trainer123!"),"inactive trainer account rejected");Accounts.login("admin","123");
            Accounts.save(uid,"verify_trainer","Test Trainer","9876999903","TRAINER",true,tid,"");
            MySQL.executeIUD("UPDATE trainer SET status_status_id=2 WHERE trainer_id=?",tid);
            rejected(()->Accounts.login("verify_trainer","Trainer123!"),"inactive trainer profile rejected");Accounts.login("admin","123");
            MySQL.executeIUD("UPDATE trainer SET status_status_id=1 WHERE trainer_id=?",tid);
            rejected(()->Accounts.login("verify_trainer","wrong"),"incorrect trainer password rejected");
            SwingUtilities.invokeAndWait(()->{
                try {
                    LoginForm f=new LoginForm();f.setVisible(true);text(f,"usernameField","verify_trainer");text(f,"passwordField","Trainer123!");call(f,"logIn");
                    boolean dashboard=false;for(Window w:Window.getWindows())if(w instanceof TrainerDashboard && w.isShowing())dashboard=true;
                    check(dashboard && !f.isDisplayable(),"real trainer login routes to dashboard");
                }catch(Exception e){throw new RuntimeException(e);}
            });
            check(Accounts.trainerData("Profile").size()==1 && ((Number)Accounts.trainerData("Profile").get(0)[0]).intValue()==tid,"profile scoped to authenticated trainer");
            check(Accounts.trainerData("Assigned members").size()==1,"only linked member returned");
            check(Accounts.trainerData("My attendance").size()==1,"own trainer attendance");
            check(Accounts.trainerData("Member progress").size()==3,"assigned member progress");
            check(Accounts.trainerData("Sessions").size()==1,"own personal-training sessions");
            rejected(()->Accounts.users(""),"trainer cannot list users");
            rejected(()->Accounts.save(null,"verify_editor","Escalation","9876999905","ADMIN",true,null,"Attack123!"),"trainer cannot create admin");
            rejected(()->Accounts.save(uid,"verify_trainer","Escalation","9876999903","ADMIN",true,null,""),"trainer cannot promote own account");
            rejected(()->Accounts.resetPassword(aid,"Attack123!"),"trainer cannot reset another password");
            rejected(()->Accounts.trainerChoices(),"trainer cannot read other trainer profiles");
            rejected(()->MainInterface.getInstance(),"trainer cannot open admin dashboard");
            rejected(()->new MembersPanel(),"trainer cannot open member management");
            rejected(()->new UserManagementPanel(),"trainer cannot open user management");
            rejected(()->MySQL.executeIUD("DELETE FROM member WHERE member_id=1"),"trainer cannot delete arbitrary members");
            rejected(()->MySQL.backupDatabase(),"trainer cannot access admin backup");
            Accounts.updateProfile("Verified Trainer","9876999903");
            check(Accounts.trainerData("Profile").get(0)[1].equals("Verified Trainer"),"own profile edit");
            rejected(()->Accounts.changePassword("incorrect","Changed123!"),"password change checks old password");
            Accounts.changePassword("Trainer123!","Changed123!");
            rejected(()->Accounts.login("verify_trainer","Trainer123!"),"old trainer password rejected");
            Accounts.login("verify_trainer","Changed123!");check(true,"trainer password change works");
            SwingUtilities.invokeAndWait(()->{for(Window w:Window.getWindows())if(w instanceof TrainerDashboard d && w.isShowing())d.logOut();});
            check(Session.current()==null,"trainer logout clears session");
            rejected(()->new MembersPanel(),"logged-out session cannot reopen admin module");
            rejected(()->Accounts.trainerData("Profile"),"logged-out trainer cannot read profile");
            Process child=new ProcessBuilder(System.getProperty("java.home")+"/bin/java", "-cp",System.getProperty("java.class.path"),"AccountVerification","persist").inheritIO().start();
            check(child.waitFor()==0,"fresh application process verifies persisted changes");
            Accounts.login("admin","123");check(scalar("SELECT COUNT(*) FROM member")==25,"members preserved");
            check(scalar("SELECT COUNT(*) FROM app_user WHERE username='verify_editor'")==0,"no unauthorized account created");
            System.out.println("Account feature assertions: "+checks);
        } catch(Throwable e){e.printStackTrace();code=1;}
        finally{try{cleanup();}catch(Exception e){e.printStackTrace();code=1;}dialogs.stop();windows();Session.logout();MySQL.closeConnection();}
        System.exit(code);
    }
    static void closeDialogs(Container c){for(Component child:c.getComponents()){if(child instanceof JOptionPane p)p.setValue(JOptionPane.OK_OPTION);if(child instanceof Container nested)closeDialogs(nested);}}
}

