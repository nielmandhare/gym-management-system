package security;

import connection.MySQL;
import java.sql.*;
import java.util.*;

public final class Accounts {
    private Accounts() { }
    private static Connection db() { return MySQL.getConnection(); }
    private static PreparedStatement statement(String sql,Object... args) throws SQLException {
        PreparedStatement s=db().prepareStatement(sql);
        for(int i=0;i<args.length;i++) s.setObject(i+1,args[i]);
        return s;
    }
    private static void update(String sql,Object... args) throws SQLException {
        try(PreparedStatement s=statement(sql,args)) { s.executeUpdate(); }
    }
    private static List<Object[]> rows(String sql,Object... args) throws SQLException {
        List<Object[]> result=new ArrayList<>();
        try(PreparedStatement s=statement(sql,args);ResultSet r=s.executeQuery()) {
            int count=r.getMetaData().getColumnCount();
            while(r.next()) { Object[] row=new Object[count]; for(int i=0;i<count;i++) row[i]=r.getObject(i+1); result.add(row); }
        }
        return result;
    }
    private interface Work { void run() throws SQLException; }
    private static synchronized void transaction(Work work) throws SQLException {
        Connection c=db(); boolean auto=c.getAutoCommit();
        if(!auto) throw new SQLException("Account operation requires its own transaction.");
        c.setAutoCommit(false);
        try { work.run(); c.commit(); }
        catch(SQLException|RuntimeException e) { c.rollback(); throw e; }
        finally { c.setAutoCommit(auto); }
    }
    /** One-way migration: preserves the password itself, removes legacy plaintext. */
    public static synchronized void migrateLegacyAdmins() throws SQLException {
        transaction(() -> {
            for(Object[] a:rows("SELECT admin_id,username,name,mobile,password,status_status_id FROM admin WHERE password <> 'MIGRATED' FOR UPDATE")) {
                List<Object[]> existing=rows("SELECT user_id FROM app_user WHERE admin_id=?",a[0]);
                if(existing.isEmpty()) update("INSERT INTO app_user(username,display_name,mobile,password_hash,role,active,admin_id) VALUES(?,?,?,?,'ADMIN',?,?)",
                    a[1],a[2],a[3],Passwords.hash(a[4].toString()),((Number)a[5]).intValue()==1,a[0]);
                update("UPDATE admin SET password='MIGRATED' WHERE admin_id=?",a[0]);
            }
        });
    }
    private static final String ACTIVE =
        "SELECT u.user_id,u.username,u.password_hash,u.role,u.trainer_id,u.active,COALESCE(a.status_status_id,1),COALESCE(t.status_status_id,1),u.member_id,COALESCE(m.status_status_id,1) " +
        "FROM app_user u LEFT JOIN admin a ON a.admin_id=u.admin_id LEFT JOIN trainer t ON t.trainer_id=u.trainer_id LEFT JOIN member m ON m.member_id=u.member_id ";
    public static Session.Identity login(String username,String password) throws SQLException {
        Session.logout(); migrateLegacyAdmins();
        List<Object[]> found=rows(ACTIVE+"WHERE u.username=? OR u.mobile=?",username,username);
        if(found.size()!=1 || !Passwords.verify(password,found.get(0)[2].toString())) throw new SecurityException("Invalid Credentials! Try Again.");
        Object[] u=found.get(0);
        if(!active(u)) throw new SecurityException("User Access Has Been Deactivated!");
        Integer trainer=u[4]==null?null:((Number)u[4]).intValue();
        Integer member=u[8]==null?null:((Number)u[8]).intValue();
        Session.Identity identity=new Session.Identity(((Number)u[0]).intValue(),u[1].toString(),u[3].toString(),trainer,member);
        Session.start(identity); return identity;
    }
    private static boolean active(Object[] u) {
        return Boolean.TRUE.equals(u[5]) || (u[5] instanceof Number && ((Number)u[5]).intValue()==1)
            ? ((Number)u[6]).intValue()==1 && ((Number)u[7]).intValue()==1 && ((Number)u[9]).intValue()==1 : false;
    }
    public static void requireActive() {
        try {
            Session.Identity identity=Session.current();
            if(identity==null) throw new SecurityException("Please log in.");
            List<Object[]> data=rows(ACTIVE+"WHERE u.user_id=?",identity.userId());
            if(data.size()!=1 || !active(data.get(0)) || !identity.role().equals(data.get(0)[3]) ||
                !Objects.equals(identity.trainerId(),data.get(0)[4]==null?null:((Number)data.get(0)[4]).intValue()) ||
                !Objects.equals(identity.memberId(),data.get(0)[8]==null?null:((Number)data.get(0)[8]).intValue())) {
                Session.logout(); throw new SecurityException("Account permissions changed. Please log in again.");
            }
        } catch(SQLException e) { throw new SecurityException("Cannot verify account permissions.",e); }
    }
    public static List<Object[]> users(String search) throws SQLException {
        Session.requireAdmin();
        return rows("SELECT user_id,username,display_name,mobile,role,active,trainer_id,member_id FROM app_user WHERE username LIKE ? OR display_name LIKE ? OR mobile LIKE ? ORDER BY username",
            "%"+search+"%","%"+search+"%","%"+search+"%");
    }
    public static List<Object[]> trainerChoices() throws SQLException {
        Session.requireAdmin(); return rows("SELECT trainer_id,name FROM trainer ORDER BY name");
    }
    public static List<Object[]> memberChoices() throws SQLException {
        Session.requireAdmin(); return rows("SELECT member_id,name FROM member ORDER BY name");
    }
    private static void validate(String username,String name,String mobile,String role,Integer trainer) {
        if(!username.matches("[A-Za-z][A-Za-z0-9_.]{2,44}")) throw new IllegalArgumentException("Username: 3–45 letters, digits, underscores or periods; start with a letter.");
        if(name.isBlank() || name.length()>45 || !mobile.matches("\\d{10}")) throw new IllegalArgumentException("Name (up to 45 characters) and 10-digit mobile are required.");
        if(!Set.of("ADMIN","TRAINER","MEMBER").contains(role)) throw new IllegalArgumentException("Unsupported role.");
        if(role.equals("TRAINER") && trainer==null) throw new IllegalArgumentException("Select a trainer profile.");
    }
    private static void password(String value) {
        if(value==null || value.length()<3 || value.length()>128) throw new IllegalArgumentException("New passwords must contain 3–128 characters.");
    }
    public static void save(Integer id,String username,String name,String mobile,String role,boolean active,Integer trainer,String newPassword) throws SQLException {
        save(id,username,name,mobile,role,active,trainer,null,newPassword);
    }
    public static void save(Integer id,String username,String name,String mobile,String role,boolean active,Integer trainer,Integer member,String newPassword) throws SQLException {
        Session.requireAdmin();
        validate(username,name,mobile,role,trainer);
        if(role.equals("MEMBER") && member==null) throw new IllegalArgumentException("Select a member profile.");
        if(id==null) password(newPassword);
        transaction(() -> {
            // Serialize account changes so last-admin checks cannot race.
            List<Object[]> all=rows("SELECT user_id,role,active FROM app_user FOR UPDATE");
            if(id!=null) {
                if(all.stream().noneMatch(a->((Number)a[0]).intValue()==id)) throw new IllegalArgumentException("User no longer exists.");
                if(id==Session.current().userId() && (!role.equals("ADMIN") || !active)) throw new IllegalArgumentException("You cannot disable or demote your own account.");
                long otherAdmins=all.stream().filter(a->((Number)a[0]).intValue()!=id && a[1].equals("ADMIN") && Boolean.TRUE.equals(a[2])).count();
                if((!role.equals("ADMIN") || !active) && otherAdmins==0 && all.stream().anyMatch(a->((Number)a[0]).intValue()==id && a[1].equals("ADMIN"))) throw new IllegalArgumentException("Keep at least one active administrator.");
                update("UPDATE app_user SET username=?,display_name=?,mobile=?,role=?,active=?,trainer_id=?,member_id=?,admin_id=CASE WHEN ?='ADMIN' THEN admin_id ELSE NULL END WHERE user_id=?",
                    username,name,mobile,role,active,role.equals("TRAINER")?trainer:null,role.equals("MEMBER")?member:null,role,id);
                update("UPDATE admin a JOIN app_user u ON u.admin_id=a.admin_id SET a.status_status_id=? WHERE u.user_id=?",active?1:2,id);
            } else update("INSERT INTO app_user(username,display_name,mobile,password_hash,role,active,trainer_id,member_id) VALUES(?,?,?,?,?,?,?,?)",
                username,name,mobile,Passwords.hash(newPassword),role,active,role.equals("TRAINER")?trainer:null,role.equals("MEMBER")?member:null);
        });
    }
    public static void resetPassword(int id,String value) throws SQLException {
        Session.requireAdmin(); password(value);
        if(rows("SELECT user_id FROM app_user WHERE user_id=?",id).isEmpty()) throw new IllegalArgumentException("User no longer exists.");
        update("UPDATE app_user SET password_hash=? WHERE user_id=?",Passwords.hash(value),id);
    }
    public static void changePassword(String old,String value) throws SQLException {
        requireActive(); password(value);
        int id=Session.current().userId();
        List<Object[]> hash=rows("SELECT password_hash FROM app_user WHERE user_id=?",id);
        if(hash.isEmpty() || !Passwords.verify(old,hash.get(0)[0].toString())) throw new IllegalArgumentException("Current password is incorrect.");
        update("UPDATE app_user SET password_hash=? WHERE user_id=?",Passwords.hash(value),id);
    }
    public static void createTrainer(String name,String mobile,String nic,String date,String username,String pass) throws SQLException {
        Session.requireAdmin(); validate(username,name,mobile,"TRAINER",1); password(pass);
        transaction(() -> {
            try(PreparedStatement s=db().prepareStatement("INSERT INTO trainer(name,mobile,nic,hired_date,username,password,status_status_id) VALUES(?,?,?,?,?,'PROFILE_ONLY',1)",Statement.RETURN_GENERATED_KEYS)) {
                s.setString(1,name);s.setString(2,mobile);s.setString(3,nic);s.setString(4,date);s.setString(5,username);s.executeUpdate();
                try(ResultSet keys=s.getGeneratedKeys()) {
                    if(!keys.next()) throw new SQLException("Trainer creation failed.");
                    update("INSERT INTO app_user(username,display_name,mobile,password_hash,role,trainer_id) VALUES(?,?,?,?,'TRAINER',?)",username,name,mobile,Passwords.hash(pass),keys.getInt(1));
                }
            }
        });
    }
    public static List<Object[]> trainerData(String view) throws SQLException {
        Session.requireTrainer(); int trainer=Session.current().trainerId();
        return switch(view) {
            case "Profile" -> rows("SELECT trainer_id,name,mobile,nic,hired_date FROM trainer WHERE trainer_id=?",trainer);
            case "Assigned members" -> rows("SELECT m.member_id,m.name,m.mobile,p.start_date,p.end_date,p.status FROM personal_training p JOIN member m ON m.member_id=p.member_member_id WHERE p.trainer_trainer_id=?",trainer);
            case "My attendance" -> rows("SELECT date,check_in_time,check_out_time FROM trainer_attendance WHERE trainer_trainer_id=? ORDER BY date DESC",trainer);
            case "Member attendance" -> rows("SELECT m.name,a.date,a.check_in_time FROM member_attendance a JOIN member m ON m.member_id=a.member_member_id WHERE EXISTS(SELECT 1 FROM personal_training p WHERE p.member_member_id=m.member_id AND p.trainer_trainer_id=?) ORDER BY a.date DESC",trainer);
            case "Member progress" -> rows("SELECT m.name,p.date,p.height_cm,p.weight_kg,p.body_fat,p.bmi FROM progress_track p JOIN member m ON m.member_id=p.member_member_id WHERE EXISTS (SELECT 1 FROM personal_training pt WHERE pt.member_member_id=m.member_id AND pt.trainer_trainer_id=?) ORDER BY p.date DESC",trainer);
            case "Sessions" -> rows("SELECT pt_id,member_member_id,start_date,end_date,total_sessions,sessions_completed,status FROM personal_training WHERE trainer_trainer_id=?",trainer);
            default -> throw new IllegalArgumentException("Unknown trainer view.");
        };
    }
    public static void updateProfile(String name,String mobile) throws SQLException {
        Session.requireTrainer(); if(name.isBlank() || name.length()>45 || !mobile.matches("\\d{10}")) throw new IllegalArgumentException("Valid name and 10-digit mobile required.");
        transaction(() -> {
            update("UPDATE trainer SET name=?,mobile=? WHERE trainer_id=?",name,mobile,Session.current().trainerId());
            update("UPDATE app_user SET display_name=?,mobile=? WHERE user_id=?",name,mobile,Session.current().userId());
        });
    }
    public static List<Object[]> memberData(String view) throws SQLException {
        Session.requireMember(); int member=Session.current().memberId();
        return switch(view) {
            case "Profile / Membership" -> rows("SELECT m.member_id,m.name,m.mobile,m.joined_date,ms.membership_type,ms.price,m.payment_status,s.status_status FROM member m JOIN membership ms ON ms.membership_type_id=m.membership_type_membership_type_id JOIN status s ON s.status_id=m.status_status_id WHERE m.member_id=?",member);
            case "My attendance" -> rows("SELECT date,check_in_time FROM member_attendance WHERE member_member_id=? ORDER BY date DESC,check_in_time DESC",member);
            case "My progress" -> rows("SELECT date,height_cm,weight_kg,body_fat,bmi FROM progress_track WHERE member_member_id=? ORDER BY date DESC,progress_id DESC",member);
            case "My training" -> rows("SELECT t.name,p.start_date,p.end_date,p.total_sessions,p.sessions_completed,p.status FROM personal_training p JOIN trainer t ON t.trainer_id=p.trainer_trainer_id WHERE p.member_member_id=?",member);
            default -> throw new IllegalArgumentException("Unknown member view.");
        };
    }
    public static void updateMemberProfile(String name,String mobile) throws SQLException {
        Session.requireMember();
        if(name.isBlank() || name.length()>45 || !mobile.matches("\\d{10}")) throw new IllegalArgumentException("Valid name and 10-digit mobile required.");
        transaction(() -> {
            update("UPDATE member SET name=?,mobile=? WHERE member_id=?",name,mobile,Session.current().memberId());
            update("UPDATE app_user SET display_name=?,mobile=? WHERE user_id=?",name,mobile,Session.current().userId());
        });
    }
    public static List<Object[]> progressChoices() throws SQLException {
        Session.requireTrainer();
        return rows("SELECT DISTINCT m.member_id,m.name FROM personal_training p JOIN member m ON m.member_id=p.member_member_id WHERE p.trainer_trainer_id=? AND p.status='ACTIVE' AND m.status_status_id=1",Session.current().trainerId());
    }
    public static void recordProgress(int member,String date,double height,double weight,double fat) throws SQLException {
        Session.requireTrainer();
        java.time.LocalDate day=java.time.LocalDate.parse(date);
        if(day.isAfter(java.time.LocalDate.now()) || !Double.isFinite(height) || !Double.isFinite(weight) || !Double.isFinite(fat) ||
            height<50 || height>250 || weight<20 || weight>400 || fat<1 || fat>70) throw new IllegalArgumentException("Use a non-future date, height 50–250 cm, weight 20–400 kg, and body fat 1–70%.");
        double bmi=Math.round(weight/Math.pow(height/100,2)*100)/100.0;
        if(bmi>99.99) throw new IllegalArgumentException("BMI exceeds the supported range.");
        transaction(() -> {
            if(rows("SELECT p.pt_id FROM personal_training p JOIN member m ON m.member_id=p.member_member_id WHERE p.member_member_id=? AND p.trainer_trainer_id=? AND p.status='ACTIVE' AND m.status_status_id=1 FOR UPDATE",member,Session.current().trainerId()).isEmpty()) throw new SecurityException("Member is not actively assigned to you.");
            update("INSERT INTO progress_track(member_member_id,date,height_cm,weight_kg,body_fat,bmi) VALUES(?,?,?,?,?,?)",member,date,height,weight,fat,bmi);
        });
    }
    public static List<Object[]> products() throws SQLException {
        Session.requireAdmin();return rows("SELECT product_id,name,price,stock,description FROM product ORDER BY product_id");
    }
    public static void saveProduct(Integer id,String name,double price,int stock,String description) throws SQLException {
        Session.requireAdmin();
        if(name.isBlank() || name.length()>255 || !Double.isFinite(price) || price<0 || stock<0) throw new IllegalArgumentException("Product name, nonnegative price and stock required.");
        if(id==null) update("INSERT INTO product(name,price,stock,description) VALUES(?,?,?,?)",name,price,stock,description);
        else {
            if(rows("SELECT product_id FROM product WHERE product_id=?",id).isEmpty()) throw new IllegalArgumentException("Product no longer exists.");
            update("UPDATE product SET name=?,price=?,stock=?,description=? WHERE product_id=?",name,price,stock,description,id);
        }
    }
    public static void deleteProduct(int id) throws SQLException {
        Session.requireAdmin();update("DELETE FROM product WHERE product_id=?",id);
    }
    public static List<Object[]> memberships() throws SQLException {
        Session.requireAdmin();return rows("SELECT membership_type_id,membership_type,price FROM membership ORDER BY membership_type_id");
    }
    public static void setMembership(int member,int membership,boolean paid) throws SQLException {
        Session.requireAdmin();
        if(rows("SELECT member_id FROM member WHERE member_id=?",member).isEmpty()) throw new IllegalArgumentException("Select an existing member.");
        update("UPDATE member SET membership_type_membership_type_id=?,payment_status=? WHERE member_id=?",membership,paid,member);
    }
    public static void setMembershipPrice(int membership,double price) throws SQLException {
        Session.requireAdmin();if(!Double.isFinite(price) || price<0) throw new IllegalArgumentException("Use a nonnegative membership price.");
        update("UPDATE membership SET price=? WHERE membership_type_id=?",price,membership);
    }
}

