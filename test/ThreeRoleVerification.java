import connection.MySQL;
import security.*;
import gui.*;
import panels.*;
import panels.dialog.*;
import java.awt.*;
import java.lang.reflect.*;
import java.sql.*;
import javax.swing.*;

public class ThreeRoleVerification {
    static int checks;
    static int progressMember;
    interface Action {void run() throws Exception;}
    static void check(boolean value,String label){if(!value)throw new AssertionError(label);checks++;System.out.println("PASS: "+label);}
    static void rejected(Action action,String label)throws Exception{
        try{action.run();throw new AssertionError(label+" was allowed");}
        catch(SecurityException|IllegalArgumentException|SQLIntegrityConstraintViolationException expected){check(true,label);}
    }
    static Object field(Object obj,String name)throws Exception{Field f=obj.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(obj);}
    static void call(Object obj,String name)throws Exception{Method m=obj.getClass().getDeclaredMethod(name);m.setAccessible(true);m.invoke(obj);}
    static void text(Object obj,String name,String value)throws Exception{((javax.swing.text.JTextComponent)field(obj,name)).setText(value);}
    // Fixtures use JDBC directly so status revocation can be tested without granting a user admin access.
    static void sql(String query,Object...args)throws Exception{try(PreparedStatement s=MySQL.getConnection().prepareStatement(query)){for(int i=0;i<args.length;i++)s.setObject(i+1,args[i]);s.executeUpdate();}}
    static Object value(String query,Object...args)throws Exception{try(PreparedStatement s=MySQL.getConnection().prepareStatement(query)){for(int i=0;i<args.length;i++)s.setObject(i+1,args[i]);try(ResultSet rs=s.executeQuery()){if(!rs.next())throw new AssertionError("Missing fixture");return rs.getObject(1);}}}
    static int number(String query,Object...args)throws Exception{return ((Number)value(query,args)).intValue();}
    static void windows()throws Exception{SwingUtilities.invokeAndWait(()->{for(Window w:Window.getWindows())w.dispose();});SwingUtilities.invokeAndWait(()->{});Session.logout();}
    static void cleanup()throws Exception{
        sql("DELETE FROM member WHERE mobile IN ('9876999801','9876999802')");
        sql("DELETE FROM trainer WHERE username='rbac_role_profile'");
        sql("DELETE FROM app_user WHERE username IN ('rbac_member','rbac_duplicate')");
        sql("DELETE FROM product WHERE name IN ('RBAC test product','RBAC edited product')");
    }
    static void choose(JComboBox<?> combo,int id)throws Exception{
        for(int i=0;i<combo.getItemCount();i++)if(combo.getItemAt(i).toString().startsWith(id+" ")){combo.setSelectedIndex(i);return;}
        throw new AssertionError("Missing profile choice");
    }
    public static void main(String[] args)throws Exception{
        com.formdev.flatlaf.themes.FlatMacDarkLaf.setup();
        if(args.length>0){
            Accounts.login("rbac_member","MemberChanged123!");
            check(Session.current().role().equals("MEMBER"),"member role persists in a fresh JVM");
            check(Accounts.memberData("Profile / Membership").get(0)[1].equals("RBAC Updated Member"),"member profile edit persists in a fresh JVM");
            check(Accounts.memberData("My progress").size()==1,"trainer-recorded progress persists in a fresh JVM");
            SwingUtilities.invokeAndWait(()->{MemberDashboard d=new MemberDashboard();d.setVisible(true);check(d.isShowing(),"member dashboard after application restart");d.dispose();});
            System.exit(0);
        }
        javax.swing.Timer closer=new javax.swing.Timer(100,e->{for(Window w:Window.getWindows())if(w instanceof JDialog d)closeDialogs(d);});closer.start();
        int code=0;String originalDemoName=null;double originalPrice=3000;
        try{
            cleanup();Accounts.login("admin","123");
            check(Session.current().role().equals("ADMIN"),"administrator login with simplified password");
            originalPrice=((Number)value("SELECT price FROM membership WHERE membership_type_id=2")).doubleValue();
            check(number("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='gms'")==16,"no redundant permanent tables");
            check(number("SELECT COUNT(*) FROM information_schema.table_constraints WHERE constraint_schema='gms' AND constraint_name='chk_app_user_role_profile'")==1,"three-role profile constraint installed");
            int demoTrainer=number("SELECT trainer_id FROM app_user WHERE username='trainer'");
            int demoMember=number("SELECT member_id FROM app_user WHERE username='user'");
            originalDemoName=value("SELECT name FROM member WHERE member_id=?",demoMember).toString();
            check(number("SELECT COUNT(*) FROM personal_training WHERE trainer_trainer_id=? AND member_member_id=?",demoTrainer,demoMember)>0,"demo accounts linked to an existing assignment");
            check(Passwords.verify("123",value("SELECT password_hash FROM app_user WHERE username='trainer'").toString()),"trainer demo hash verifies");
            check(Passwords.verify("123",value("SELECT password_hash FROM app_user WHERE username='user'").toString()),"member demo hash verifies");
            sql("INSERT INTO member(name,mobile,membership_type_membership_type_id,status_status_id) VALUES('RBAC Member','9876999801',2,1)");
            sql("INSERT INTO member(name,mobile,membership_type_membership_type_id,status_status_id) VALUES('Other Private Member','9876999802',2,1)");
            int mid=number("SELECT member_id FROM member WHERE mobile='9876999801'");
            int other=number("SELECT member_id FROM member WHERE mobile='9876999802'");
            sql("INSERT INTO member_attendance(member_member_id,date,check_in_time) VALUES(?,'2026-10-07','09:00:00')",mid);
            sql("INSERT INTO member_attendance(member_member_id,date,check_in_time) VALUES(?,'2026-10-06','08:00:00')",other);
            sql("INSERT INTO progress_track(member_member_id,date,height_cm,weight_kg,body_fat,bmi) VALUES(?,'2026-10-06',180,90,20,27.78)",other);
            sql("INSERT INTO personal_training(member_member_id,trainer_trainer_id,start_date,session_price,total_sessions,status) VALUES(?,?,'2026-10-07',500,10,'ACTIVE')",mid,demoTrainer);
            SwingUtilities.invokeAndWait(()->{try{
                UserManagementPanel panel=new UserManagementPanel();text(panel,"username","rbac_member");text(panel,"name","RBAC Member");text(panel,"mobile","9876999801");text(panel,"password","MemberInitial123!");
                ((JComboBox<?>)field(panel,"role")).setSelectedItem("MEMBER");choose((JComboBox<?>)field(panel,"member"),mid);call(panel,"save");
                check(number("SELECT COUNT(*) FROM app_user WHERE username='rbac_member' AND member_id=?",mid)==1,"administrator creates a member account through Swing");
            }catch(Exception e){throw new RuntimeException(e);}});
            int uid=number("SELECT user_id FROM app_user WHERE username='rbac_member'");
            rejected(()->Accounts.save(null,"rbac_duplicate","Duplicate","9876999803","MEMBER",true,null,mid,"MemberInitial123!"),"duplicate member profile link rejected");
            check(number("SELECT COUNT(*) FROM app_user WHERE username='rbac_duplicate'")==0,"failed member account creation rolls back");
            rejected(()->Accounts.save(null,"rbac_duplicate","Missing","9876999803","MEMBER",true,null,null,"MemberInitial123!"),"member role requires a profile");
            rejected(()->Accounts.login("rbac_member","incorrect"),"incorrect member password rejected");
            Accounts.login("admin","123");Accounts.save(uid,"rbac_member","RBAC Member","9876999801","MEMBER",false,null,mid,"");
            rejected(()->Accounts.login("rbac_member","MemberInitial123!"),"inactive member account rejected");
            Accounts.login("admin","123");Accounts.save(uid,"rbac_member","RBAC Member","9876999801","MEMBER",true,null,mid,"");
            sql("UPDATE member SET status_status_id=2 WHERE member_id=?",mid);
            rejected(()->Accounts.login("rbac_member","MemberInitial123!"),"inactive member profile rejected");
            sql("UPDATE member SET status_status_id=1 WHERE member_id=?",mid);
            SwingUtilities.invokeAndWait(()->{try{
                LoginForm login=new LoginForm();login.setVisible(true);text(login,"usernameField","rbac_member");text(login,"passwordField","MemberInitial123!");call(login,"logIn");
                boolean member=false,trainer=false,admin=false;for(Window w:Window.getWindows()){if(w instanceof MemberDashboard && w.isShowing())member=true;if(w instanceof TrainerDashboard && w.isShowing())trainer=true;if(w instanceof MainInterface && w.isShowing())admin=true;}
                check(member && !trainer && !admin && !login.isDisplayable(),"real member login routes only to member dashboard");
                for(Window w:Window.getWindows())if(w instanceof MemberDashboard){JTabbedPane tabs=(JTabbedPane)field(w,"tabs");check(tabs.getTabCount()==4,"member role has four permitted navigation tabs");}
            }catch(Exception e){throw new RuntimeException(e);}});
            check(((Number)Accounts.memberData("Profile / Membership").get(0)[0]).intValue()==mid,"own member profile derived from session");
            check(Accounts.memberData("My attendance").size()==1 && Accounts.memberData("My attendance").get(0)[0].toString().equals("2026-10-07"),"member attendance excludes another member's records");
            check(Accounts.memberData("My progress").isEmpty(),"member progress excludes another member's records");
            check(Accounts.memberData("My training").size()==1,"own training association");
            rejected(()->Accounts.memberData("Other member "+other),"arbitrary member view rejected");
            rejected(()->Accounts.trainerData("Profile"),"member cannot read trainer dashboard data");
            rejected(()->Accounts.users(""),"member cannot list accounts");
            rejected(()->new UserManagementPanel(),"member cannot open User Management");
            rejected(()->new TrainerDashboard(),"member cannot open Trainer Dashboard");
            rejected(()->MainInterface.getInstance(),"member cannot open Admin Dashboard");
            rejected(()->new ManageProductsDialog(null,false),"member cannot open product management");
            rejected(()->new MembershipDialog(null,other,2,false),"member cannot open membership management");
            rejected(()->Accounts.save(uid,"rbac_member","Escalation","9876999801","ADMIN",true,null,null,""),"member cannot self-promote");
            rejected(()->Accounts.resetPassword(uid,"Attack123!"),"member cannot use admin password reset");
            rejected(()->Accounts.setMembership(other,3,true),"member cannot change another membership");
            rejected(()->Accounts.saveProduct(null,"Attack",1,1,""),"member cannot write inventory");
            rejected(()->MySQL.executeSearch("SELECT * FROM member WHERE member_id=?",other),"member cannot use unrestricted legacy query");
            rejected(()->MySQL.executeIUD("DELETE FROM member WHERE member_id=?",other),"member cannot use unrestricted legacy write");
            rejected(()->MySQL.backupDatabase(),"member cannot export database backup");
            rejected(()->Accounts.recordProgress(mid,"2026-10-07",170,68,19),"member cannot record trainer progress");
            Accounts.updateMemberProfile("RBAC Updated Member","9876999801");check(Accounts.memberData("Profile / Membership").get(0)[1].equals("RBAC Updated Member"),"permitted own member profile update");
            rejected(()->Accounts.changePassword("incorrect","MemberChanged123!"),"member password change checks old password");
            Accounts.changePassword("MemberInitial123!","MemberChanged123!");
            rejected(()->Accounts.login("rbac_member","MemberInitial123!"),"old member password rejected after change");
            Accounts.login("rbac_member","MemberChanged123!");check(true,"changed member password works");
            sql("UPDATE app_user SET active=0 WHERE user_id=?",uid);
            rejected(()->Accounts.memberData("My attendance"),"permission checks revoke an already logged-in disabled account");
            check(Session.current()==null,"revoked session cleared");sql("UPDATE app_user SET active=1 WHERE user_id=?",uid);
            windows();Accounts.login("trainer","123");
            check(Session.current().trainerId()==demoTrainer,"trainer demo linked identity");
            check(Accounts.progressChoices().stream().anyMatch(row->((Number)row[0]).intValue()==mid),"assigned active member available for progress entry");
            check(Accounts.trainerData("Member attendance").stream().anyMatch(row->row[0].equals("RBAC Updated Member")),"trainer sees assigned member attendance");
            check(Accounts.trainerData("Member attendance").stream().noneMatch(row->row[0].equals("Other Private Member")),"trainer attendance excludes unassigned member");
            rejected(()->Accounts.recordProgress(other,"2026-10-07",180,90,20),"trainer cannot write unassigned member progress");
            rejected(()->Accounts.recordProgress(mid,java.time.LocalDate.now().plusDays(1).toString(),170,68,19),"future progress date rejected");
            rejected(()->Accounts.recordProgress(mid,"2026-10-07",0,68,19),"invalid progress measurements rejected");
            rejected(()->Accounts.memberData("My progress"),"trainer cannot impersonate member view");
            progressMember=mid;
            SwingUtilities.invokeAndWait(()->{TrainerDashboard d=new TrainerDashboard();try{call(d,"recordProgress");}catch(Exception e){throw new RuntimeException(e);}});
            check(number("SELECT COUNT(*) FROM progress_track WHERE member_member_id=?",mid)==1,"authorized trainer progress persisted");
            check(Math.abs(((Number)value("SELECT bmi FROM progress_track WHERE member_member_id=?",mid)).doubleValue()-23.53)<.001,"BMI computed from entered height and weight");
            SwingUtilities.invokeAndWait(()->{TrainerDashboard d=new TrainerDashboard();d.setVisible(true);try{check(((JTabbedPane)field(d,"tabs")).getTabCount()==6,"trainer attendance tab available");}catch(Exception e){throw new RuntimeException(e);}d.logOut();});
            check(Session.current()==null,"trainer logout clears identity");
            Accounts.login("rbac_member","MemberChanged123!");check(Accounts.memberData("My progress").size()==1,"member sees trainer-recorded own progress");
            SwingUtilities.invokeAndWait(()->{MemberDashboard d=new MemberDashboard();d.setVisible(true);d.logOut();});
            check(Session.current()==null,"member logout clears identity");
            rejected(()->MySQL.executeSearch("SELECT * FROM member"),"logged-out legacy reads denied");
            rejected(()->MySQL.executeIUD("DELETE FROM member WHERE member_id=?",other),"logged-out legacy writes denied");
            rejected(()->MySQL.backupDatabase(),"logged-out backups denied");
            Accounts.login("admin","123");check(Session.current().role().equals("ADMIN"),"logout then login switches to correct admin role");
            Accounts.setMembership(mid,3,true);check(number("SELECT membership_type_membership_type_id FROM member WHERE member_id=?",mid)==3,"administrator changes member membership");
            SwingUtilities.invokeAndWait(()->{try{
                MembershipDialog d=new MembershipDialog(null,mid,3,true);((JComboBox<?>)field(d,"membership")).setSelectedIndex(1);((JCheckBox)field(d,"paid")).setSelected(false);call(d,"save");
                check(number("SELECT membership_type_membership_type_id FROM member WHERE member_id=?",mid)==2,"membership saved through Swing");
                check(number("SELECT payment_status FROM member WHERE member_id=?",mid)==0,"membership payment status saved through Swing");
                MembershipDialog rate=new MembershipDialog(null,mid,2,false);text(rate,"price","3456");call(rate,"rate");rate.dispose();
                check(number("SELECT price FROM membership WHERE membership_type_id=2")==3456,"admin plan price update through Swing");
                ManageProductsDialog products=new ManageProductsDialog(null,false);text(products,"name","RBAC test product");text(products,"price","99.50");text(products,"stock","7");text(products,"description","Test inventory");call(products,"save");
                check(number("SELECT COUNT(*) FROM product WHERE name='RBAC test product'")==1,"admin product create through Swing");
                JTable t=(JTable)field(products,"table");for(int row=0;row<t.getRowCount();row++)if(t.getValueAt(row,1).equals("RBAC test product"))t.setRowSelectionInterval(row,row);
                text(products,"name","RBAC edited product");text(products,"stock","9");call(products,"save");check(number("SELECT stock FROM product WHERE name='RBAC edited product'")==9,"admin product edit through Swing");
                for(int row=0;row<t.getRowCount();row++)if(t.getValueAt(row,1).equals("RBAC edited product"))t.setRowSelectionInterval(row,row);
                call(products,"delete");check(number("SELECT COUNT(*) FROM product WHERE name='RBAC edited product'")==0,"admin product delete through Swing");products.dispose();
            }catch(Exception e){throw new RuntimeException(e);}});
            Accounts.setMembershipPrice(2,originalPrice);
            sql("INSERT INTO trainer(name,mobile,nic,hired_date,username,password,status_status_id) VALUES('RBAC Role Profile','9876999804','990099999804','2026-10-07','rbac_role_profile','PROFILE_ONLY',1)");
            int tid=number("SELECT trainer_id FROM trainer WHERE username='rbac_role_profile'");
            Accounts.save(uid,"rbac_member","RBAC Updated Member","9876999801","TRAINER",true,tid,null,"");
            Accounts.login("rbac_member","MemberChanged123!");check(Session.current().role().equals("TRAINER") && Session.current().trainerId()==tid && Session.current().memberId()==null,"member-to-trainer role change replaces profile association");
            sql("UPDATE app_user SET role='MEMBER',trainer_id=NULL,member_id=? WHERE user_id=?",mid,uid);
            rejected(()->Accounts.trainerData("Profile"),"role change invalidates stale trainer session");
            Accounts.login("rbac_member","MemberChanged123!");check(Session.current().memberId()==mid && Session.current().trainerId()==null,"changed member role and profile persist");
            Process child=new ProcessBuilder(System.getProperty("java.home")+"/bin/java","-cp",System.getProperty("java.class.path"),"ThreeRoleVerification","persist").inheritIO().start();
            check(child.waitFor()==0,"fresh process verifies member password, role, profile and progress");
            Accounts.login("user","123");Accounts.updateMemberProfile("Demo Persistence Check","9876501002");
            Process setup=new ProcessBuilder("powershell","-NoProfile","-ExecutionPolicy","Bypass","-File","Setup-Database.ps1").inheritIO().start();
            check(setup.waitFor()==0,"setup migration reruns successfully");
            check(Accounts.memberData("Profile / Membership").get(0)[1].equals("Demo Persistence Check"),"normal setup preserves edited demo profile");
            Accounts.updateMemberProfile(originalDemoName,"9876501002");
            Accounts.login("rbac_member","MemberChanged123!");check(Accounts.memberData("My progress").size()==1,"setup preserves new trainer progress and changed member password");
            Accounts.login("admin","123");check(number("SELECT COUNT(*) FROM product")==8,"demo product inventory preserved");
            System.out.println("Three-role assertions: "+checks);
        }catch(Throwable e){e.printStackTrace();code=1;}
        finally{
            try{windows();Accounts.login("admin","123");if(originalDemoName!=null)sql("UPDATE member SET name=? WHERE mobile='9876501002'",originalDemoName);
                if(originalDemoName!=null)sql("UPDATE app_user SET display_name=? WHERE username='user'",originalDemoName);
                Accounts.setMembershipPrice(2,originalPrice);cleanup();
            }catch(Exception e){e.printStackTrace();code=1;}
            closer.stop();windows();MySQL.closeConnection();
        }
        System.exit(code);
    }
    static void closeDialogs(Container c){for(Component child:c.getComponents()){
        if(child instanceof JOptionPane p){
            if(p.getMessage() instanceof JPanel form && form.getComponentCount()==10 && form.getComponent(0) instanceof JLabel label && label.getText().equals("Assigned member")){
                try{choose((JComboBox<?>)form.getComponent(1),progressMember);}catch(Exception e){throw new RuntimeException(e);}
                ((JTextField)form.getComponent(3)).setText("2026-10-07");((JTextField)form.getComponent(5)).setText("170");((JTextField)form.getComponent(7)).setText("68");((JTextField)form.getComponent(9)).setText("19");
            }
            p.setValue(JOptionPane.OK_OPTION);
        }
        if(child instanceof Container nested)closeDialogs(nested);
    }}
}
