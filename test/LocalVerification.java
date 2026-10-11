import connection.MySQL;
import gui.*;
import java.awt.*;
import java.lang.reflect.*;
import java.sql.*;
import java.util.*;
import javax.swing.*;
import net.sf.jasperreports.engine.*;

/** Integration checks against the dedicated local database and real Swing forms. */
public class LocalVerification {
    static Object field(Object o, String name) throws Exception {
        Field f=o.getClass().getDeclaredField(name); f.setAccessible(true); return f.get(o);
    }
    static void call(Object o, String name) throws Exception {
        Method m=o.getClass().getDeclaredMethod(name); m.setAccessible(true); m.invoke(o);
    }
    static void call(Object o, String name, boolean value) throws Exception {
        Method m=o.getClass().getDeclaredMethod(name,boolean.class); m.setAccessible(true); m.invoke(o,value);
    }
    static int scalar(String sql,Object... params) throws Exception {
        try(ResultSet rs=MySQL.executeSearch(sql,params)) { rs.next(); return rs.getInt(1); }
    }
    static void click(Object o,String name) throws Exception { ((JButton)field(o,name)).doClick(); }
    static void crud() throws Exception {
        panels.dialog.ManageMemberDialog member=new panels.dialog.ManageMemberDialog(null,false);
        member.setVisible(true);
        text(member,"nameField","Test O'Neil"); text(member,"mobileField","0779990001");
        text(member,"dateChooserTextField","2026-10-07"); text(member,"healthField","No issues; O'Neil sample");
        text(member,"heightField","175"); text(member,"weightField","70"); text(member,"bodyfatField","18"); text(member,"bmiField","22.86");
        text(member,"heightField","invalid"); click(member,"newMemberBtn");
        check(scalar("SELECT COUNT(*) FROM member WHERE mobile=?","0779990001")==0,"invalid measurements leave no partial member");
        messages.clear(); text(member,"heightField","175");
        click(member,"newMemberBtn");
        check(scalar("SELECT COUNT(*) FROM member WHERE mobile=?","0779990001")==1,"member create with apostrophe");
        int memberId=scalar("SELECT member_id FROM member WHERE mobile=?","0779990001");
        check(scalar("SELECT COUNT(*) FROM progress_track WHERE member_member_id=?",memberId)==1,"initial progress insert");
        text(member,"mobileField_3","0779990001"); ((JComboBox<?>)field(member,"statusCombo_3")).setSelectedIndex(1); click(member,"editMemberBtn");
        check(scalar("SELECT status_status_id FROM member WHERE member_id=?",memberId)==2,"member status update");
        ((JComboBox<?>)field(member,"statusCombo_3")).setSelectedIndex(0); click(member,"editMemberBtn");
        panels.dialog.AttendanceDialog attendance=new panels.dialog.AttendanceDialog(null,false);
        text(attendance,"idField",""); click(attendance,"confirmBtn");
        check(((JLabel)field(attendance,"markedLabel")).getText().contains("valid"),"attendance blank ID validation");
        text(attendance,"idField","99999999"); click(attendance,"confirmBtn");
        check(((JLabel)field(attendance,"markedLabel")).getText().contains("not found"),"attendance unknown member validation");
        text(attendance,"idField",String.valueOf(memberId)); click(attendance,"confirmBtn"); click(attendance,"confirmBtn");
        check(scalar("SELECT COUNT(*) FROM member_attendance WHERE member_member_id=?",memberId)==1,"attendance write and duplicate prevention"); attendance.dispose();
        text(member,"idField_2",String.valueOf(memberId)); ((JRadioButton)field(member,"idRadio_2")).setSelected(true); click(member,"jButton1"); click(member,"removeMemberBtn");
        check(scalar("SELECT COUNT(*) FROM member WHERE member_id=?",memberId)==0,"member delete");
        check(scalar("SELECT COUNT(*) FROM progress_track WHERE member_member_id=?",memberId)==0 && scalar("SELECT COUNT(*) FROM member_attendance WHERE member_member_id=?",memberId)==0,"member dependent records cascade"); member.dispose();

        panels.dialog.ManageTrainerDialog trainer=new panels.dialog.ManageTrainerDialog(null,false); trainer.setVisible(true);
        text(trainer,"nameField","Test D'Souza"); text(trainer,"mobileField","0779990002"); text(trainer,"nicField","200199990002");
        text(trainer,"dateChooserTextField","2026-10-07"); text(trainer,"usernameField","test_trainer");
        text(trainer,"passwordField","Test1234@"); text(trainer,"confirmPasswordField","Test1234@"); click(trainer,"newTrainerBtn");
        int trainerId=scalar("SELECT trainer_id FROM trainer WHERE username='test_trainer'"); check(trainerId>0,"trainer create with apostrophe");
        text(trainer,"mobileField_3","0779990002"); ((JComboBox<?>)field(trainer,"statusCombo_3")).setSelectedIndex(1); click(trainer,"editMemberBtn");
        check(scalar("SELECT status_status_id FROM trainer WHERE trainer_id=?",trainerId)==2,"trainer status update");
        text(trainer,"idField_2",String.valueOf(trainerId)); ((JRadioButton)field(trainer,"idRadio_2")).setSelected(true); click(trainer,"jButton1"); click(trainer,"removeMemberBtn");
        check(scalar("SELECT COUNT(*) FROM trainer WHERE trainer_id=?",trainerId)==0,"trainer delete"); trainer.dispose();

        panels.dialog.ManageEquipmentDialog equipment=new panels.dialog.ManageEquipmentDialog(null,false); equipment.setVisible(true);
        text(equipment,"nameField","Test Bench"); text(equipment,"serialField","VERIFY-EQUIPMENT");
        text(equipment,"purchaseField","2026-10-07"); text(equipment,"warrantyField","2027-10-07"); call(equipment,"save",false);
        int eqId=scalar("SELECT equipment_id FROM equipment WHERE serial_number='VERIFY-EQUIPMENT'"); check(eqId>0,"equipment create");
        JTable table=(JTable)field(equipment,"table"); for(int r=0;r<table.getRowCount();r++) if(((Number)table.getValueAt(r,0)).intValue()==eqId) table.setRowSelectionInterval(r,r);
        text(equipment,"nameField","Test Bench Updated"); call(equipment,"save",true);
        check(scalar("SELECT COUNT(*) FROM equipment WHERE equipment_id=? AND name='Test Bench Updated'",eqId)==1,"equipment update");
        for(int r=0;r<table.getRowCount();r++) if(((Number)table.getValueAt(r,0)).intValue()==eqId) table.setRowSelectionInterval(r,r);
        call(equipment,"deleteSelected"); check(scalar("SELECT COUNT(*) FROM equipment WHERE equipment_id=?",eqId)==0,"equipment delete"); equipment.dispose();
        messages.clear();
    }
    static void check(boolean ok, String name) { if(!ok) throw new AssertionError(name); System.out.println("PASS: "+name); }
    static String renderedText(String file) throws Exception {
        try(var input=LocalVerification.class.getClassLoader().getResourceAsStream("report/"+file)) {
            JasperPrint print=JasperFillManager.fillReport(input,new HashMap<>(),MySQL.getConnection());
            StringBuilder result=new StringBuilder();
            for(var page:print.getPages()) appendReportElements(page.getElements(),result);
            return result.toString();
        }
    }
    static void appendReportElements(java.util.List<net.sf.jasperreports.engine.JRPrintElement> elements,StringBuilder result) {
        for(var element:elements) {
            if(element instanceof net.sf.jasperreports.engine.JRPrintText text) result.append(text.getFullText()).append(' ');
            else if(element instanceof net.sf.jasperreports.engine.JRPrintFrame frame) appendReportElements(frame.getElements(),result);
        }
    }
    static void text(Object o,String name,String text) throws Exception { ((javax.swing.text.JTextComponent)field(o,name)).setText(text); }
    static java.util.List<String> messages=new ArrayList<>();
    static void dialogs(Container c) {
        for(Component child:c.getComponents()) {
            if(child instanceof JOptionPane p) { messages.add(String.valueOf(p.getMessage())); p.setValue(JOptionPane.OK_OPTION); }
            if(child instanceof Container sub) dialogs(sub);
        }
    }
    public static void main(String[] args) throws Exception {
        int code=0;
        java.util.List<String> errors=new ArrayList<>();
        javax.swing.Timer closer=new javax.swing.Timer(100,e->{ for(Window w:Window.getWindows()) if(w.isShowing() && w instanceof JDialog) dialogs(w); });
        closer.start();
        try {
            com.formdev.flatlaf.themes.FlatMacDarkLaf.setup();
            check(MySQL.getConnection()!=null && MySQL.getConnection().isValid(5),"JDBC connection");
            security.Accounts.login("admin","123");
            check(security.Session.current().role().equals("ADMIN"),"admin credential preserved through hash migration");
            check(scalar("SELECT COUNT(*) FROM member")==25,"25 realistic members seeded");
            check(scalar("SELECT COUNT(*) FROM trainer")==6,"6 trainer profiles seeded");
            int attendanceCount=scalar("SELECT COUNT(*) FROM member_attendance");
            check(attendanceCount>=250 && scalar("SELECT COUNT(DISTINCT member_member_id) FROM member_attendance")>=24,"varied member attendance seeded");
            check(scalar("SELECT COUNT(*) FROM payment_history")==19,"payment history seeded");
            check(scalar("SELECT COUNT(*) FROM progress_track")==75,"three dated progress entries per member");
            check(scalar("SELECT COUNT(*) FROM workout_plan")==6,"weekly plans seeded");
            check(scalar("SELECT COUNT(*) FROM equipment")==12 && scalar("SELECT COUNT(*) FROM maintenance_log")==7,"equipment and maintenance seeded");
            check(scalar("SELECT COUNT(*) FROM product")==8 && scalar("SELECT COUNT(*) FROM order_history")==20,"products and sales seeded");
            String mobilePattern=validation.Regex.Validation.MOBILE.validate();
            check("9876501002".matches(mobilePattern) && "0771234567".matches(mobilePattern) && !"12345".matches(mobilePattern),"Indian and existing mobile validation");
            java.util.logging.Logger.getLogger("").addHandler(new java.util.logging.Handler() {
                public void publish(java.util.logging.LogRecord record) { if(record.getLevel().intValue()>=java.util.logging.Level.SEVERE.intValue()) errors.add(record.getMessage()); }
                public void flush() {} public void close() {}
            });
            MySQL.executeIUD("DELETE FROM member WHERE mobile='0779990001' AND name='Test O''Neil'");
            MySQL.executeIUD("DELETE FROM trainer WHERE username='test_trainer' AND mobile='0779990002'");
            MySQL.executeIUD("DELETE FROM equipment WHERE serial_number='VERIFY-EQUIPMENT'");
            SwingUtilities.invokeAndWait(()->{
                try {
                    LoginForm f=new LoginForm(); f.setVisible(true);
                    text(f,"usernameField","admin"); text(f,"passwordField","wrong"); call(f,"logIn");
                    check(f.isDisplayable() && messages.stream().anyMatch(s->s.contains("Invalid Credentials")),"invalid password rejected"); messages.clear();
                    text(f,"usernameField","' OR 1=1 -- "); text(f,"passwordField","123"); call(f,"logIn");
                    check(f.isDisplayable() && messages.stream().anyMatch(s->s.contains("Invalid Credentials")),"login SQL injection rejected"); messages.clear();
                    // Privileged fixture changes are independent of the rejected login session.
                    try(var fixture=MySQL.getConnection().prepareStatement("UPDATE admin SET status_status_id=2 WHERE username='admin'")){fixture.executeUpdate();}
                    try {
                        text(f,"usernameField","admin"); text(f,"passwordField","123"); call(f,"logIn");
                        check(f.isDisplayable() && messages.stream().anyMatch(s->s.contains("Deactivated")),"inactive admin rejected");
                    } finally { try(var fixture=MySQL.getConnection().prepareStatement("UPDATE admin SET status_status_id=1 WHERE username='admin'")){fixture.executeUpdate();} }
                    messages.clear();
                    text(f,"usernameField","admin"); text(f,"passwordField","123"); call(f,"logIn");
                    MainInterface main=MainInterface.getInstance();
                    check(main.isShowing() && !f.isDisplayable(),"admin login and dashboard");
                    for(String name: new String[]{"dashboardBtn","membersBtn","trainersBtn","workoutPlansBtn","attendanceBtn","progressTrackBtn","equipmentBtn","otherProductsBtn","paymentRecordsBtn","generateReportsBtn"}) {
                        ((JButton)field(main,name)).doClick(); check(true,"menu "+name);
                    }
                    main.refreshAllData();
                    for(String name:new String[]{"membersPanel","trainersPanel","workoutPlansPanel","attendancePanel","progressTrackPanel","equipmentPanel","otherProductsPanel","paymentRecordsPanel"}) {
                        Object panel=field(main,name); text(panel,"searchTextField","Demo"); ((JButton)field(panel,"searchBtn")).doClick();
                        text(panel,"searchTextField","O'Neil"); ((JButton)field(panel,"searchBtn")).doClick();
                        text(panel,"searchTextField",""); ((JButton)field(panel,"searchBtn")).doClick(); check(true,"search/refresh "+name);
                    }
                    check(((JTable)field(field(main,"membersPanel"),"membersTable")).getRowCount()==25,"all 25 members visible in the table");
                    check(((JTable)field(field(main,"trainersPanel"),"membersTable")).getRowCount()==6,"all trainers visible in the table");
                    check(((JTable)field(field(main,"attendancePanel"),"membersTable")).getRowCount()==attendanceCount,"attendance history visible in the table");
                    check(((JTable)field(field(main,"progressTrackPanel"),"membersTable")).getRowCount()==75,"progress history visible in the table");
                    check(((JTable)field(field(main,"workoutPlansPanel"),"membersTable")).getRowCount()==6,"workout plans visible in the table");
                    check(((JTable)field(field(main,"equipmentPanel"),"membersTable")).getRowCount()==12,"equipment inventory visible in the table");
                    check(((JTable)field(field(main,"otherProductsPanel"),"membersTable")).getRowCount()==8,"product inventory visible in the table");
                    check(((JTable)field(field(main,"paymentRecordsPanel"),"membersTable")).getRowCount()==19,"payments visible in the table");
                    String memberReportText=renderedText("gms_member_report.jasper");
                    check(memberReportText.contains("user"),"member report includes demo member data");
                    check(renderedText("gms_trainer_report.jasper").contains("Shruti Patil"),"trainer report includes demo trainer data");
                    check(renderedText("gms_member_attendance_report.jasper").contains("user"),"attendance report includes member visits");
                    check(renderedText("gms_payments_report.jasper").contains("user"),"payment report includes member payments");
                    check(renderedText("gms_product_sales_report.jasper").contains("Protein Bar"),"product report includes sale data");
                    check(renderedText("gms_equipment_report.jasper").contains("Commercial Treadmill"),"equipment report includes inventory data");
                    crud();
                    main.refreshAllData();
                    main.logOut();
                    LoginForm again=null; for(Window w:Window.getWindows()) if(w instanceof LoginForm l && l.isShowing()) again=l;
                    check(again!=null && !main.isDisplayable(),"logout closes session");
                    text(again,"usernameField","0770000001"); text(again,"passwordField","123"); call(again,"logIn");
                    check(MainInterface.getInstance().isShowing() && !again.isDisplayable(),"login again by mobile");
                    Object reports=field(MainInterface.getInstance(),"generateReportsPanel");
                    for(String button:new String[]{"memberReportBtn","trainerReportBtn","attendanceReportBtn","paymentReportBtn","productSalesReport","equipmentReportBtn"}) {
                        click(reports,button);
                        boolean viewer=false;
                        for(Window w:Window.getWindows()) if(w instanceof net.sf.jasperreports.view.JasperViewer && w.isShowing()) { viewer=true; w.dispose(); }
                        check(viewer,"report viewer "+button);
                    }
                    MainInterface finalMain=MainInterface.getInstance();
                    ((JButton)field(finalMain,"dashboardBtn")).doClick();
                    java.awt.image.BufferedImage screen=new java.awt.image.BufferedImage(finalMain.getWidth(),finalMain.getHeight(),java.awt.image.BufferedImage.TYPE_INT_RGB);
                    Graphics2D graphics=screen.createGraphics(); finalMain.paintAll(graphics); graphics.dispose();
                    javax.imageio.ImageIO.write(screen,"png",new java.io.File("work/dashboard.png"));
                }catch(Exception e){throw new RuntimeException(e);}
            });
            for(String name:new String[]{"member","trainer","member_attendance","payments","product_sales","equipment"}) {
                try(var in=LocalVerification.class.getClassLoader().getResourceAsStream("report/gms_"+name+"_report.jasper")) {
                    JasperPrint print=JasperFillManager.fillReport(in,new HashMap<>(),MySQL.getConnection());
                    check(print!=null,"report "+name+" ("+print.getPages().size()+" pages)");
                    JasperExportManager.exportReportToPdfFile(print,"work/report-"+name+".pdf");
                }
            }
            check(MySQL.backupDatabase(),"database backup");
            MySQL.getConnection().setAutoCommit(false);
            try {
                check(MySQL.executeIUD("INSERT INTO product (name,price,stock) VALUES ('VERIFY product',10,5)"),"product database write");
                int productId=scalar("SELECT product_id FROM product WHERE name='VERIFY product'");
                check(MySQL.executeIUD("INSERT INTO order_history (product_product_id,quantity,unit_price,total_amount,paid_method,reference_no) VALUES (?,2,10,20,'CASH','VERIFY-ORDER')",productId),"product sale database write");
                check(MySQL.executeIUD("INSERT INTO payment_history (member_member_id,paid_amount,paid_method,reference_no) VALUES (1,100,'CASH','VERIFY-PAY')"),"payment database write");
                check(MySQL.executeIUD("INSERT INTO workout_plan (level,day1) VALUES ('ADVANCED','VERIFY plan')"),"workout database write");
                SwingUtilities.invokeAndWait(()->MainInterface.getInstance().refreshAllData());
                check(scalar("SELECT COUNT(*) FROM order_history WHERE reference_no='VERIFY-ORDER'")==1 && scalar("SELECT COUNT(*) FROM payment_history WHERE reference_no='VERIFY-PAY'")==1,"sale and payment database reads");
                MySQL.executeIUD("DELETE FROM order_history"); MySQL.executeIUD("DELETE FROM product");
                MySQL.executeIUD("DELETE FROM maintenance_log"); MySQL.executeIUD("DELETE FROM equipment");
                SwingUtilities.invokeAndWait(()->MainInterface.getInstance().refreshAllData());
                check(panels.OtherProductsPanel.totalProductCount.equals("00"),"empty product/order tables");
                check(panels.EquipmentPanel.totalEquipment.equals("00"),"empty equipment/maintenance tables");
            } finally { MySQL.getConnection().rollback(); MySQL.getConnection().setAutoCommit(true); }
            check(messages.isEmpty(),"no unexpected error dialogs");
            check(errors.isEmpty(),"no SEVERE application log entries");
        } catch(Throwable e) { e.printStackTrace(); code=1; }
        finally { closer.stop(); for(Window w:Window.getWindows()) w.dispose(); MySQL.closeConnection(); System.exit(code); }
    }
}
