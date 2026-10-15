package web;

import com.sun.net.httpserver.*;
import security.Passwords;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.SecureRandom;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;

/** Browser service over the existing GMS MySQL schema. */
public final class GymWebServer {
    private static final Properties CONFIG = new Properties();
    private static final Map<String, User> SESSIONS = new ConcurrentHashMap<>();
    private static final String JDBC = "com.mysql.cj.jdbc.Driver";
    private record User(int id,String username,String name,String mobile,String role,Integer trainer,Integer member) { }
    private record Result(List<String> columns,List<List<Object>> rows) { }

    public static void main(String[] args) throws Exception {
        Path file=Path.of("src/resources/db.properties");
        if(Files.exists(file)) try(InputStream in=Files.newInputStream(file)){CONFIG.load(in);}
        String host=System.getenv().getOrDefault("GMS_DB_HOST",CONFIG.getProperty("db.host","127.0.0.1"));
        String port=System.getenv().getOrDefault("GMS_DB_PORT",CONFIG.getProperty("db.port","3307"));
        String db=System.getenv().getOrDefault("GMS_DB_NAME",CONFIG.getProperty("db.name","gms"));
        String jdbcUrl="jdbc:mysql://"+host+":"+port+"/"+db+"?useAffectedRows=true&serverTimezone=UTC";
        String dbUser=System.getenv().getOrDefault("GMS_DB_USER",CONFIG.getProperty("db.user","gms_dev"));
        String dbPass=System.getenv().getOrDefault("GMS_DB_PASSWORD",CONFIG.getProperty("db.password","1234"));
        Class.forName(JDBC);
        Database.init(jdbcUrl,dbUser,dbPass);
        int portNumber=Integer.parseInt(System.getenv().getOrDefault("GMS_WEB_PORT","8081"));
        HttpServer server=HttpServer.create(new InetSocketAddress("127.0.0.1",portNumber),0);
        server.createContext("/api/",GymWebServer::api);
        server.createContext("/",GymWebServer::staticFile);
        server.setExecutor(Executors.newFixedThreadPool(Math.max(8,Runtime.getRuntime().availableProcessors())));
        server.start();
        System.out.println("Gym Management System web app running at http://127.0.0.1:"+portNumber);
    }

    private static void api(HttpExchange x) throws IOException {
        try {
            String path=x.getRequestURI().getPath(); String method=x.getRequestMethod();
            boolean mutation=Set.of("/api/login","/api/logout","/api/action","/api/backup").contains(path);
            if(!method.equals(mutation?"POST":"GET")){json(x,405,Map.of("error","HTTP method is not allowed."));return;}
            if(mutation){String origin=x.getRequestHeaders().getFirst("Origin");if(origin!=null&&!origin.equals("http://"+x.getRequestHeaders().getFirst("Host")))throw new ForbiddenException("Request origin is not allowed.");}
            Map<String,String> form=method.equals("POST")?form(x):Map.of();
            if(path.equals("/api/login")&&method.equals("POST")) { login(x,form); return; }
            User user=session(x);
            if(path.equals("/api/me")){json(x,200,userMap(user));return;}
            if(path.equals("/api/logout")&&method.equals("POST")){SESSIONS.remove(token(x));x.getResponseHeaders().add("Set-Cookie","gms_session=; Path=/; HttpOnly; SameSite=Lax; Max-Age=0");json(x,200,Map.of("ok",true));return;}
            if(path.equals("/api/dashboard")){dashboard(x,user);return;}
            if(path.equals("/api/data")){data(x,user,query(x).getOrDefault("view","members"));return;}
            if(path.equals("/api/options")){options(x,user);return;}
            if(path.equals("/api/report")){report(x,user,query(x).getOrDefault("name","members"));return;}
            if(path.equals("/api/backup")&&method.equals("POST")){admin(user);json(x,200,Map.of("ok",true,"file",Database.backup()));return;}
            if(path.equals("/api/action")&&method.equals("POST")){action(x,user,form);return;}
            json(x,404,Map.of("error","Unknown endpoint."));
        } catch(ForbiddenException e){json(x,403,Map.of("error",e.getMessage()));}
          catch(SecurityException e){json(x,401,Map.of("error",e.getMessage()));}
          catch(IllegalArgumentException e){json(x,400,Map.of("error",e.getMessage()));}
          catch(SQLException e){e.printStackTrace();String message=switch(e.getErrorCode()){case 1062->"That username, mobile number, profile link, or serial number is already in use.";case 1451->"This record is referenced by existing history and cannot be deleted.";case 1452->"The selected related record no longer exists.";case 3819->"The role requires its corresponding profile link.";default->"The database request could not be completed.";};json(x,e.getErrorCode()==1062||e.getErrorCode()==1451?409:500,Map.of("error",message));}
          catch(LinkageError e){e.printStackTrace();json(x,500,Map.of("error","The report libraries are incompatible. Restart using Run-Web.ps1."));}
          catch(Exception e){e.printStackTrace();json(x,500,Map.of("error","The request could not be completed. Check the database connection and required migrations."));}
    }
    private static final class ForbiddenException extends SecurityException {ForbiddenException(String message){super(message);}}
    private static User session(HttpExchange x)throws SQLException{String t=token(x);User u=SESSIONS.get(t);if(u==null)throw new SecurityException("Your session has ended. Please sign in again.");List<Map<String,Object>> live=Database.query("SELECT u.username,u.display_name,u.mobile,u.role,u.trainer_id,u.member_id,u.active,COALESCE(a.status_status_id,1) admin_active,COALESCE(tr.status_status_id,1) trainer_active,COALESCE(m.status_status_id,1) member_active FROM app_user u LEFT JOIN admin a ON a.admin_id=u.admin_id LEFT JOIN trainer tr ON tr.trainer_id=u.trainer_id LEFT JOIN member m ON m.member_id=u.member_id WHERE u.user_id=?",u.id);if(live.size()!=1){SESSIONS.remove(t);throw new SecurityException("Your session is no longer valid.");}Map<String,Object> r=live.get(0);boolean active=truth(r.get("active"))&&num(r.get("admin_active"))==1&&num(r.get("trainer_active"))==1&&num(r.get("member_active"))==1;if(!active||!u.role.equals(str(r.get("role")))||!Objects.equals(u.trainer,nullableNum(r.get("trainer_id")))||!Objects.equals(u.member,nullableNum(r.get("member_id")))){SESSIONS.remove(t);throw new SecurityException("Account access changed. Please sign in again.");}return new User(u.id,str(r.get("username")),str(r.get("display_name")),str(r.get("mobile")),str(r.get("role")),nullableNum(r.get("trainer_id")),nullableNum(r.get("member_id")));}
    private static String token(HttpExchange x){String c=x.getRequestHeaders().getFirst("Cookie");if(c==null)return "";for(String p:c.split(";")){String[] a=p.trim().split("=",2);if(a.length==2&&a[0].equals("gms_session"))return a[1];}return "";}
    private static void login(HttpExchange x,Map<String,String> f)throws Exception{
        String who=f.getOrDefault("username","").trim(), pass=f.getOrDefault("password","");
        List<Map<String,Object>> rows=Database.query("SELECT u.user_id,u.username,u.display_name,u.mobile,u.password_hash,u.role,u.trainer_id,u.member_id,u.active,COALESCE(a.status_status_id,1) admin_active,COALESCE(t.status_status_id,1) trainer_active,COALESCE(m.status_status_id,1) member_active FROM app_user u LEFT JOIN admin a ON a.admin_id=u.admin_id LEFT JOIN trainer t ON t.trainer_id=u.trainer_id LEFT JOIN member m ON m.member_id=u.member_id WHERE u.username=? OR u.mobile=?",who,who);
        if(rows.size()!=1)throw new SecurityException("We couldn't match that username or mobile number.");
        Map<String,Object> r=rows.get(0);
        if(!Passwords.verify(pass,String.valueOf(r.get("password_hash"))))throw new SecurityException("The password doesn't match. Try again.");
        if(!truth(r.get("active"))||num(r.get("admin_active"))!=1||num(r.get("trainer_active"))!=1||num(r.get("member_active"))!=1)throw new SecurityException("This account or profile is inactive.");
        User u=new User(num(r.get("user_id")),str(r.get("username")),str(r.get("display_name")),str(r.get("mobile")),str(r.get("role")),nullableNum(r.get("trainer_id")),nullableNum(r.get("member_id")));
        byte[] b=new byte[32];new SecureRandom().nextBytes(b);String t=Base64.getUrlEncoder().withoutPadding().encodeToString(b);SESSIONS.put(t,u);
        x.getResponseHeaders().add("Set-Cookie","gms_session="+t+"; Path=/; HttpOnly; SameSite=Lax");json(x,200,userMap(u));
    }
    private static Map<String,Object> userMap(User u){return Map.of("id",u.id,"username",u.username,"name",u.name,"mobile",u.mobile,"role",u.role,"profileId",u.member!=null?u.member:u.trainer!=null?u.trainer:0);}
    private static void dashboard(HttpExchange x,User u)throws Exception{
        Map<String,Object> out=new LinkedHashMap<>();out.put("user",userMap(u));
        if(u.role.equals("ADMIN")){
            out.put("stats",Database.one("SELECT (SELECT COUNT(*) FROM member) members,(SELECT COUNT(*) FROM trainer) trainers,(SELECT COUNT(*) FROM member_attendance WHERE date=CURRENT_DATE()) checkins,(SELECT COALESCE(SUM(paid_amount),0) FROM payment_history WHERE YEAR(date)=YEAR(CURRENT_DATE()) AND MONTH(date)=MONTH(CURRENT_DATE())) payments,(SELECT COALESCE(SUM(total_amount),0) FROM order_history WHERE YEAR(date)=YEAR(CURRENT_DATE()) AND MONTH(date)=MONTH(CURRENT_DATE())) sales"));
            out.put("recent",Database.query("SELECT m.name AS `member`,a.date,a.check_in_time time FROM member_attendance a JOIN member m ON m.member_id=a.member_member_id ORDER BY a.date DESC,a.check_in_time DESC LIMIT 6"));
            out.put("activity",Database.query("SELECT DATE_FORMAT(date,'%Y-%m-%d') day,COUNT(*) value FROM member_attendance WHERE date>=DATE_SUB(CURRENT_DATE(),INTERVAL 6 DAY) GROUP BY date ORDER BY date"));
        } else if(u.role.equals("TRAINER")){
            out.put("stats",Database.one("SELECT (SELECT COUNT(DISTINCT member_member_id) FROM personal_training WHERE trainer_trainer_id=? AND status='ACTIVE') members,(SELECT COUNT(*) FROM trainer_attendance WHERE trainer_trainer_id=? AND date=CURRENT_DATE()) checkins,(SELECT COUNT(*) FROM personal_training WHERE trainer_trainer_id=? AND status='ACTIVE') sessions",u.trainer,u.trainer,u.trainer));
            out.put("recent",Database.query("SELECT m.name AS `member`,p.start_date,p.end_date,p.status FROM personal_training p JOIN member m ON m.member_id=p.member_member_id WHERE p.trainer_trainer_id=? ORDER BY p.start_date DESC LIMIT 6",u.trainer));
            out.put("activity",Database.query("SELECT DATE_FORMAT(date,'%Y-%m-%d') day,COUNT(*) value FROM trainer_attendance WHERE trainer_trainer_id=? AND date>=DATE_SUB(CURRENT_DATE(),INTERVAL 6 DAY) GROUP BY date ORDER BY date",u.trainer));
        }else{
            out.put("stats",Database.one("SELECT (SELECT COUNT(*) FROM member_attendance WHERE member_member_id=?) visits,(SELECT COUNT(*) FROM progress_track WHERE member_member_id=?) measurements,(SELECT COUNT(*) FROM personal_training WHERE member_member_id=? AND status='ACTIVE') sessions",u.member,u.member,u.member));
            out.put("recent",Database.query("SELECT date,weight_kg,bmi FROM progress_track WHERE member_member_id=? ORDER BY date DESC LIMIT 6",u.member));
            out.put("activity",Database.query("SELECT DATE_FORMAT(date,'%Y-%m-%d') day,COUNT(*) value FROM member_attendance WHERE member_member_id=? AND date>=DATE_SUB(CURRENT_DATE(),INTERVAL 6 DAY) GROUP BY date ORDER BY date",u.member));
        }
        json(x,200,out);
    }
    private static void report(HttpExchange x,User u,String name)throws Exception{
        admin(u);
        byte[] pdf;
        try(Connection c=Database.connect()){pdf=WebReports.export(name,sql->Database.query(c,sql));}
        x.getResponseHeaders().set("Content-Type","application/pdf");x.getResponseHeaders().set("Content-Disposition","attachment; filename=\""+name+"-report.pdf\"");x.getResponseHeaders().set("Cache-Control","no-store");x.sendResponseHeaders(200,pdf.length);try(OutputStream out=x.getResponseBody()){out.write(pdf);}
    }
    private static void data(HttpExchange x,User u,String view)throws Exception{
        String sql; Object[] args=new Object[0];
        switch(view){
            case "members"->{admin(u);sql="SELECT m.member_id id,m.name,m.mobile,m.health_info health,m.membership_type_membership_type_id membership_id,DATE_FORMAT(m.joined_date,'%Y-%m-%d') joined,m.payment_status paid,ms.membership_type membership,s.status_status active FROM member m JOIN membership ms ON ms.membership_type_id=m.membership_type_membership_type_id JOIN status s ON s.status_id=m.status_status_id ORDER BY m.member_id DESC";}
            case "trainers"->{admin(u);sql="SELECT trainer_id id,name,mobile,nic,DATE_FORMAT(hired_date,'%Y-%m-%d') hired,status_status_id=1 active FROM trainer ORDER BY trainer_id DESC";}
            case "attendance"->{if(u.role.equals("ADMIN"))sql="SELECT a.attendance_id id,m.name AS `member`,m.mobile,DATE_FORMAT(a.date,'%Y-%m-%d') date,a.check_in_time time FROM member_attendance a JOIN member m ON m.member_id=a.member_member_id ORDER BY a.date DESC,a.check_in_time DESC";else if(u.role.equals("TRAINER")){sql="SELECT a.attendance_id id,m.name AS `member`,m.mobile,DATE_FORMAT(a.date,'%Y-%m-%d') date,a.check_in_time time FROM member_attendance a JOIN member m ON m.member_id=a.member_member_id WHERE EXISTS(SELECT 1 FROM personal_training p WHERE p.member_member_id=m.member_id AND p.trainer_trainer_id=?) ORDER BY a.date DESC,a.check_in_time DESC";args=new Object[]{u.trainer};}else throw new ForbiddenException("This view is unavailable.");}
            case "equipment"->{admin(u);sql="SELECT equipment_id id,name,status,serial_number serial,DATE_FORMAT(purchase_date,'%Y-%m-%d') purchased,DATE_FORMAT(warranty_exp,'%Y-%m-%d') warranty FROM equipment ORDER BY equipment_id DESC";}
            case "products"->{admin(u);sql="SELECT product_id id,name,price,stock,description FROM product ORDER BY product_id DESC";}
            case "payments"->{admin(u);sql="SELECT p.payment_id id,m.name AS `member`,p.paid_amount amount,p.paid_method method,p.reference_no reference,DATE_FORMAT(p.date,'%Y-%m-%d') date FROM payment_history p JOIN member m ON m.member_id=p.member_member_id ORDER BY p.date DESC";}
            case "sales"->{admin(u);sql="SELECT o.order_id id,p.name AS `product`,o.quantity,o.unit_price price,o.total_amount total,o.paid_method method,o.reference_no reference,DATE_FORMAT(o.date,'%Y-%m-%d') date FROM order_history o JOIN product p ON p.product_id=o.product_product_id ORDER BY o.date DESC";}
            case "workouts"->{admin(u);sql="SELECT plan_id id,level,day1,day2,day3,day4,day5,day6,day7 FROM workout_plan ORDER BY plan_id DESC";}
            case "progress"->{if(u.role.equals("ADMIN"))sql="SELECT p.progress_id id,m.name AS `member`,p.date,p.height_cm height,p.weight_kg weight,p.body_fat,p.bmi FROM progress_track p JOIN member m ON m.member_id=p.member_member_id ORDER BY p.date DESC";else if(u.role.equals("TRAINER")){sql="SELECT p.progress_id id,m.name AS `member`,p.date,p.height_cm height,p.weight_kg weight,p.body_fat,p.bmi FROM progress_track p JOIN member m ON m.member_id=p.member_member_id WHERE EXISTS(SELECT 1 FROM personal_training pt WHERE pt.member_member_id=m.member_id AND pt.trainer_trainer_id=?) ORDER BY p.date DESC";args=new Object[]{u.trainer};}else{sql="SELECT progress_id id,date,height_cm height,weight_kg weight,body_fat,bmi FROM progress_track WHERE member_member_id=? ORDER BY date DESC";args=new Object[]{u.member};}}
            case "my-attendance"->{if(u.role.equals("TRAINER")){sql="SELECT DATE_FORMAT(date,'%Y-%m-%d') date,check_in_time checkin,check_out_time checkout FROM trainer_attendance WHERE trainer_trainer_id=? ORDER BY date DESC";args=new Object[]{u.trainer};}else if(u.role.equals("MEMBER")){sql="SELECT DATE_FORMAT(date,'%Y-%m-%d') date,check_in_time checkin FROM member_attendance WHERE member_member_id=? ORDER BY date DESC";args=new Object[]{u.member};}else throw new ForbiddenException("This view is unavailable.");}
            case "assigned-members"->{if(!u.role.equals("TRAINER"))throw new ForbiddenException("Trainer access required.");sql="SELECT m.member_id id,m.name,m.mobile,p.start_date,p.end_date,p.status FROM personal_training p JOIN member m ON m.member_id=p.member_member_id WHERE p.trainer_trainer_id=? ORDER BY m.name";args=new Object[]{u.trainer};}
            case "sessions"->{if(u.role.equals("ADMIN"))sql="SELECT pt.pt_id id,m.name AS `member`,t.name trainer,pt.start_date,pt.end_date,pt.total_sessions total,pt.sessions_completed completed,pt.status FROM personal_training pt JOIN member m ON m.member_id=pt.member_member_id JOIN trainer t ON t.trainer_id=pt.trainer_trainer_id ORDER BY pt.start_date DESC";else if(u.role.equals("TRAINER")){sql="SELECT pt.pt_id id,m.name AS `member`,pt.start_date,pt.end_date,pt.total_sessions total,pt.sessions_completed completed,pt.status FROM personal_training pt JOIN member m ON m.member_id=pt.member_member_id WHERE pt.trainer_trainer_id=? ORDER BY pt.start_date DESC";args=new Object[]{u.trainer};}else{sql="SELECT t.name trainer,pt.start_date,pt.end_date,pt.total_sessions total,pt.sessions_completed completed,pt.status FROM personal_training pt JOIN trainer t ON t.trainer_id=pt.trainer_trainer_id WHERE pt.member_member_id=? ORDER BY pt.start_date DESC";args=new Object[]{u.member};}}
            case "users"->{admin(u);sql="SELECT user_id id,username,display_name name,mobile,role,active,trainer_id,member_id FROM app_user ORDER BY user_id DESC";}
            case "memberships"->{admin(u);sql="SELECT membership_type_id id,membership_type name,price FROM membership ORDER BY membership_type_id";}
            case "profile"->{if(u.role.equals("TRAINER")){sql="SELECT name,mobile,nic,DATE_FORMAT(hired_date,'%Y-%m-%d') hired FROM trainer WHERE trainer_id=?";args=new Object[]{u.trainer};}else if(u.role.equals("MEMBER")){sql="SELECT m.name,m.mobile,DATE_FORMAT(m.joined_date,'%Y-%m-%d') joined,ms.membership_type membership,ms.price,m.payment_status paid,s.status_status active FROM member m JOIN membership ms ON ms.membership_type_id=m.membership_type_membership_type_id JOIN status s ON s.status_id=m.status_status_id WHERE m.member_id=?";args=new Object[]{u.member};}else throw new ForbiddenException("This view is unavailable.");}
            default->throw new IllegalArgumentException("Unknown data view.");
        }
        json(x,200,Database.query(sql,args));
    }
    private static void options(HttpExchange x,User u)throws Exception {
        Map<String,Object> out=new LinkedHashMap<>();
        if(u.role.equals("ADMIN")) {
            out.put("members",Database.query("SELECT member_id id,name,status_status_id=1 active FROM member ORDER BY name"));
            out.put("trainers",Database.query("SELECT trainer_id id,name,status_status_id=1 active FROM trainer ORDER BY name"));
            out.put("memberships",Database.query("SELECT membership_type_id id,membership_type name,price FROM membership ORDER BY membership_type_id"));
        } else if(u.role.equals("TRAINER")) {
            out.put("members",Database.query("SELECT DISTINCT m.member_id id,m.name FROM personal_training p JOIN member m ON m.member_id=p.member_member_id WHERE p.trainer_trainer_id=? AND p.status='ACTIVE' AND m.status_status_id=1 ORDER BY m.name",u.trainer));
        }
        json(x,200,out);
    }
    private static void action(HttpExchange x,User u,Map<String,String> f)throws Exception {
        String action=f.getOrDefault("action","");
        if(!Set.of("profile-save","progress-add","password-change").contains(action)) admin(u);
        Map<String,Object> result=Database.transaction(c -> {
            int id=integer(f,"id",0);
            switch(action) {
                case "member-save" -> {
                    String name=text(f,"name",45),mobile=mobile(f);int plan=integer(f,"membership",0);
                    if(Database.query(c,"SELECT membership_type_id FROM membership WHERE membership_type_id=?",plan).isEmpty()) throw new IllegalArgumentException("Choose a membership plan.");
                    boolean paid=integer(f,"paid",0)==1;
                    if(id==0) {
                        double h=measure(f,"height",50,250),w=measure(f,"weight",20,400),fat=measure(f,"fat",1,70),bmi=bmi(h,w);
                        int member=Database.insert(c,"INSERT INTO member(name,mobile,health_info,membership_type_membership_type_id,payment_status) VALUES(?,?,?,?,?)",name,mobile,f.getOrDefault("health",""),plan,paid);
                        Database.execute(c,"INSERT INTO progress_track(member_member_id,date,height_cm,weight_kg,body_fat,bmi) VALUES(?,CURRENT_DATE(),?,?,?,?)",member,h,w,fat,bmi);
                        return Map.of("ok",true,"id",member,"message","Member and initial measurements saved.");
                    }
                    exists(c,"member","member_id",id);
                    Database.execute(c,"UPDATE member SET name=?,mobile=?,health_info=?,membership_type_membership_type_id=?,payment_status=? WHERE member_id=?",name,mobile,f.getOrDefault("health",""),plan,paid,id);
                    Database.execute(c,"UPDATE app_user SET display_name=?,mobile=? WHERE member_id=?",name,mobile,id);
                }
                case "member-status" -> { exists(c,"member","member_id",id);Database.execute(c,"UPDATE member SET status_status_id=? WHERE member_id=?",integer(f,"active",1)==1?1:2,id); }
                case "member-delete" -> { exists(c,"member","member_id",id);Database.execute(c,"DELETE FROM member WHERE member_id=?",id); }
                case "attendance-add" -> {
                    int member=integer(f,"member",0);
                    List<Map<String,Object>> found=Database.query(c,"SELECT member_id,payment_status FROM member WHERE member_id=? AND status_status_id=1 FOR UPDATE",member);
                    if(found.isEmpty()) throw new IllegalArgumentException("Choose an active member.");
                    if(!Database.query(c,"SELECT attendance_id FROM member_attendance WHERE member_member_id=? AND date=CURRENT_DATE()",member).isEmpty()) throw new IllegalArgumentException("This member already checked in today.");
                    Database.execute(c,"INSERT INTO member_attendance(member_member_id) VALUES(?)",member);
                    return Map.of("ok",true,"message",truth(found.get(0).get("payment_status"))?"Attendance recorded. Membership is paid.":"Attendance recorded. Membership payment is due.");
                }
                case "trainer-save" -> {
                    String name=text(f,"name",45),mobile=mobile(f),nic=text(f,"nic",12),date=day(f,"hired",false);
                    if(id==0) {
                        String username=username(f),password=password(f,"password");
                        int trainer=Database.insert(c,"INSERT INTO trainer(name,mobile,nic,hired_date,username,password,status_status_id) VALUES(?,?,?,?,?,'PROFILE_ONLY',1)",name,mobile,nic,date,username);
                        Database.execute(c,"INSERT INTO app_user(username,display_name,mobile,password_hash,role,trainer_id) VALUES(?,?,?,?,'TRAINER',?)",username,name,mobile,Passwords.hash(password),trainer);
                        return Map.of("ok",true,"id",trainer,"message","Trainer profile and login created.");
                    }
                    exists(c,"trainer","trainer_id",id);
                    Database.execute(c,"UPDATE trainer SET name=?,mobile=?,nic=?,hired_date=? WHERE trainer_id=?",name,mobile,nic,date,id);
                    Database.execute(c,"UPDATE app_user SET display_name=?,mobile=? WHERE trainer_id=?",name,mobile,id);
                }
                case "trainer-status" -> {exists(c,"trainer","trainer_id",id);Database.execute(c,"UPDATE trainer SET status_status_id=? WHERE trainer_id=?",integer(f,"active",1)==1?1:2,id);}
                case "trainer-delete" -> {exists(c,"trainer","trainer_id",id);Database.execute(c,"DELETE FROM trainer WHERE trainer_id=?",id);}
                case "equipment-save" -> {
                    String name=text(f,"name",45),status=choice(f,"status",Set.of("GOOD","UNDER MAINTENANCE","DAMAGED","RETIRED"));
                    String purchased=optionalDay(f,"purchased"),warranty=optionalDay(f,"warranty");String serial=empty(f.get("serial"));
                    if(serial!=null&&serial.length()>255)throw new IllegalArgumentException("Serial number is too long.");
                    if(id==0) Database.insert(c,"INSERT INTO equipment(name,purchase_date,status,serial_number,warranty_exp) VALUES(?,?,?,?,?)",name,purchased,status,serial,warranty);
                    else {exists(c,"equipment","equipment_id",id);Database.execute(c,"UPDATE equipment SET name=?,purchase_date=?,status=?,serial_number=?,warranty_exp=? WHERE equipment_id=?",name,purchased,status,serial,warranty,id);}
                }
                case "equipment-delete" -> {exists(c,"equipment","equipment_id",id);Database.execute(c,"DELETE FROM equipment WHERE equipment_id=?",id);}
                case "product-save" -> {
                    String name=text(f,"name",255);double price=money(f,"price");int stock=integer(f,"stock",0);
                    if(stock<0)throw new IllegalArgumentException("Stock cannot be negative.");
                    if(id==0)Database.insert(c,"INSERT INTO product(name,price,stock,description) VALUES(?,?,?,?)",name,price,stock,f.getOrDefault("description",""));
                    else {exists(c,"product","product_id",id);Database.execute(c,"UPDATE product SET name=?,price=?,stock=?,description=? WHERE product_id=?",name,price,stock,f.getOrDefault("description",""),id);}
                }
                case "product-delete" -> {exists(c,"product","product_id",id);Database.execute(c,"DELETE FROM product WHERE product_id=?",id);}
                case "membership-price" -> {exists(c,"membership","membership_type_id",id);Database.execute(c,"UPDATE membership SET price=? WHERE membership_type_id=?",money(f,"price"),id);}
                case "user-save" -> {
                    String username=username(f),name=text(f,"name",45),mobile=mobile(f),role=choice(f,"role",Set.of("ADMIN","TRAINER","MEMBER"));
                    boolean active=integer(f,"active",1)==1;int profile=integer(f,"profile",0);
                    if(role.equals("TRAINER"))exists(c,"trainer","trainer_id",profile);if(role.equals("MEMBER"))exists(c,"member","member_id",profile);
                    List<Map<String,Object>> accounts=Database.query(c,"SELECT user_id,role,active FROM app_user FOR UPDATE");
                    if(id==0)Database.insert(c,"INSERT INTO app_user(username,display_name,mobile,password_hash,role,active,trainer_id,member_id) VALUES(?,?,?,?,?,?,?,?)",username,name,mobile,Passwords.hash(password(f,"password")),role,active,role.equals("TRAINER")?profile:null,role.equals("MEMBER")?profile:null);
                    else {
                        Map<String,Object> target=accounts.stream().filter(r->num(r.get("user_id"))==id).findFirst().orElseThrow(()->new IllegalArgumentException("Account no longer exists."));
                        if(id==u.id&&(!active||!role.equals("ADMIN")))throw new IllegalArgumentException("You cannot disable or demote your own account.");
                        if(target.get("role").equals("ADMIN")&&(!active||!role.equals("ADMIN"))&&accounts.stream().noneMatch(r->num(r.get("user_id"))!=id&&r.get("role").equals("ADMIN")&&truth(r.get("active"))))throw new IllegalArgumentException("Keep at least one active administrator.");
                        Database.execute(c,"UPDATE app_user SET username=?,display_name=?,mobile=?,role=?,active=?,trainer_id=?,member_id=?,admin_id=CASE WHEN ?='ADMIN' THEN admin_id ELSE NULL END WHERE user_id=?",username,name,mobile,role,active,role.equals("TRAINER")?profile:null,role.equals("MEMBER")?profile:null,role,id);
                        Database.execute(c,"UPDATE admin a JOIN app_user u ON u.admin_id=a.admin_id SET a.status_status_id=? WHERE u.user_id=?",active?1:2,id);
                    }
                }
                case "user-status" -> {
                    List<Map<String,Object>> accounts=Database.query(c,"SELECT user_id,role,active FROM app_user FOR UPDATE");boolean active=integer(f,"active",1)==1;
                    Map<String,Object> target=accounts.stream().filter(r->num(r.get("user_id"))==id).findFirst().orElseThrow(()->new IllegalArgumentException("Account no longer exists."));
                    if(id==u.id&&!active)throw new IllegalArgumentException("You cannot disable your own account.");
                    if(!active&&target.get("role").equals("ADMIN")&&accounts.stream().noneMatch(r->num(r.get("user_id"))!=id&&r.get("role").equals("ADMIN")&&truth(r.get("active"))))throw new IllegalArgumentException("Keep at least one active administrator.");
                    Database.execute(c,"UPDATE app_user SET active=? WHERE user_id=?",active,id);
                    Database.execute(c,"UPDATE admin a JOIN app_user u ON u.admin_id=a.admin_id SET a.status_status_id=? WHERE u.user_id=?",active?1:2,id);
                }
                case "user-reset-password" -> {exists(c,"app_user","user_id",id);Database.execute(c,"UPDATE app_user SET password_hash=? WHERE user_id=?",Passwords.hash(password(f,"password")),id);}
                case "profile-save" -> {
                    String name=text(f,"name",45),mobile=mobile(f);
                    if(u.role.equals("TRAINER"))Database.execute(c,"UPDATE trainer SET name=?,mobile=? WHERE trainer_id=?",name,mobile,u.trainer);
                    else if(u.role.equals("MEMBER"))Database.execute(c,"UPDATE member SET name=?,mobile=? WHERE member_id=?",name,mobile,u.member);
                    else throw new ForbiddenException("Trainer or member access required.");
                    Database.execute(c,"UPDATE app_user SET display_name=?,mobile=? WHERE user_id=?",name,mobile,u.id);
                }
                case "progress-add" -> {
                    if(!u.role.equals("TRAINER"))throw new ForbiddenException("Trainer access required.");
                    int member=integer(f,"member",0);String date=day(f,"date",true);double h=measure(f,"height",50,250),w=measure(f,"weight",20,400),fat=measure(f,"fat",1,70);
                    if(Database.query(c,"SELECT p.pt_id FROM personal_training p JOIN member m ON m.member_id=p.member_member_id WHERE p.member_member_id=? AND p.trainer_trainer_id=? AND p.status='ACTIVE' AND m.status_status_id=1 FOR UPDATE",member,u.trainer).isEmpty())throw new ForbiddenException("Member is not actively assigned to you.");
                    Database.execute(c,"INSERT INTO progress_track(member_member_id,date,height_cm,weight_kg,body_fat,bmi) VALUES(?,?,?,?,?,?)",member,date,h,w,fat,bmi(h,w));
                }
                case "password-change" -> {
                    String next=password(f,"newPassword");if(!next.equals(f.getOrDefault("confirmPassword","")))throw new IllegalArgumentException("Passwords do not match.");
                    List<Map<String,Object>> hashes=Database.query(c,"SELECT password_hash FROM app_user WHERE user_id=? FOR UPDATE",u.id);
                    if(hashes.isEmpty()||!Passwords.verify(f.getOrDefault("oldPassword",""),str(hashes.get(0).get("password_hash"))))throw new IllegalArgumentException("Current password is incorrect.");
                    Database.execute(c,"UPDATE app_user SET password_hash=? WHERE user_id=?",Passwords.hash(next),u.id);
                }
                default -> throw new IllegalArgumentException("Unknown action.");
            }
            return Map.of("ok",true,"message","Changes saved.");
        });
        json(x,200,result);
    }
    private static void exists(Connection c,String table,String key,int id)throws SQLException {
        if(id<1||Database.query(c,"SELECT "+key+" FROM "+table+" WHERE "+key+"=?",id).isEmpty())throw new IllegalArgumentException("Select an existing "+table.replace('_',' ')+" record.");
    }
    private static String text(Map<String,String> f,String key,int max){String value=required(f,key);if(value.length()>max)throw new IllegalArgumentException(key+" must be at most "+max+" characters.");return value;}
    private static String username(Map<String,String> f){String s=required(f,"username");if(!s.matches("[A-Za-z][A-Za-z0-9_.]{2,44}"))throw new IllegalArgumentException("Username must be 3–45 letters, digits, underscores or periods and start with a letter.");return s;}
    private static String password(Map<String,String> f,String key){String s=f.getOrDefault(key,"");if(s.length()<3||s.length()>128)throw new IllegalArgumentException("Password must contain 3–128 characters.");return s;}
    private static String choice(Map<String,String> f,String key,Set<String> values){String s=required(f,key);if(!values.contains(s))throw new IllegalArgumentException("Select a valid "+key+".");return s;}
    private static String day(Map<String,String> f,String key,boolean past){try{LocalDate date=LocalDate.parse(required(f,key));if(past&&date.isAfter(LocalDate.now()))throw new IllegalArgumentException("Date cannot be in the future.");return date.toString();}catch(java.time.format.DateTimeParseException e){throw new IllegalArgumentException("Enter a valid date for "+key+".");}}
    private static String optionalDay(Map<String,String> f,String key){return empty(f.get(key))==null?null:day(f,key,false);}
    private static double money(Map<String,String> f,String key){double d=decimal(f,key);if(d<0||d>99999999.99)throw new IllegalArgumentException("Enter a nonnegative amount within the supported range.");return d;}
    private static double measure(Map<String,String> f,String key,double min,double max){double d=decimal(f,key);if(d<min||d>max)throw new IllegalArgumentException(key+" must be between "+min+" and "+max+".");return d;}
    private static double bmi(double h,double w){double b=Math.round(w/Math.pow(h/100,2)*100.0)/100.0;if(b>99.99)throw new IllegalArgumentException("BMI exceeds the supported range.");return b;}

    private static void admin(User u){if(!u.role.equals("ADMIN"))throw new ForbiddenException("Administrator access required.");}
    private static String required(Map<String,String> f,String k){String v=f.getOrDefault(k,"").trim();if(v.isEmpty())throw new IllegalArgumentException("Enter a value for "+k+".");return v;}
    private static String mobile(Map<String,String> f){String s=required(f,"mobile");if(!s.matches("\\d{10}"))throw new IllegalArgumentException("Mobile must contain 10 digits.");return s;}
    private static String empty(String s){return s==null||s.isBlank()?null:s;}
    private static int integer(Map<String,String> f,String k,int d){try{return Integer.parseInt(f.getOrDefault(k,"").isBlank()?""+d:f.get(k));}catch(Exception e){throw new IllegalArgumentException("Invalid number for "+k+".");}}
    private static double decimal(Map<String,String> f,String k){try{double v=Double.parseDouble(f.getOrDefault(k,""));if(!Double.isFinite(v))throw new NumberFormatException();return v;}catch(Exception e){throw new IllegalArgumentException("Enter a valid number for "+k+".");}}
    private static boolean truth(Object o){return Boolean.TRUE.equals(o)||(o instanceof Number&&((Number)o).intValue()!=0);}
    private static int num(Object o){return o==null?0:((Number)o).intValue();}
    private static Integer nullableNum(Object o){return o==null?null:num(o);}
    private static String str(Object o){return Objects.toString(o,"");}
    private static Map<String,String> query(HttpExchange x){return decode(x.getRequestURI().getRawQuery());}
    private static Map<String,String> form(HttpExchange x)throws IOException{return decode(new String(x.getRequestBody().readAllBytes(),StandardCharsets.UTF_8));}
    private static Map<String,String> decode(String data){Map<String,String> m=new HashMap<>();if(data!=null)for(String p:data.split("&")){String[] a=p.split("=",2);m.put(URLDecoder.decode(a[0],StandardCharsets.UTF_8),a.length>1?URLDecoder.decode(a[1],StandardCharsets.UTF_8):"");}return m;}
    private static void staticFile(HttpExchange x)throws IOException {
        if(!x.getRequestMethod().equals("GET")){x.sendResponseHeaders(405,-1);x.close();return;}
        String requested=x.getRequestURI().getPath();
        if(!Set.of("/","/index.html","/ui.css","/ui.js").contains(requested)){x.sendResponseHeaders(404,-1);x.close();return;}
        Path file=Path.of("web",requested.equals("/")?"index.html":requested.substring(1));
        if(!Files.isRegularFile(file)){x.sendResponseHeaders(404,-1);x.close();return;}
        String type=requested.endsWith(".css")?"text/css; charset=utf-8":requested.endsWith(".js")?"text/javascript; charset=utf-8":"text/html; charset=utf-8";
        x.getResponseHeaders().set("Content-Type",type);x.getResponseHeaders().set("X-Content-Type-Options","nosniff");x.getResponseHeaders().set("X-Frame-Options","DENY");x.getResponseHeaders().set("Cache-Control","no-cache");
        byte[] bytes=Files.readAllBytes(file);x.sendResponseHeaders(200,bytes.length);try(OutputStream out=x.getResponseBody()){out.write(bytes);}
    }
    private static void json(HttpExchange x,int status,Object value)throws IOException{byte[] b=toJson(value).getBytes(StandardCharsets.UTF_8);x.getResponseHeaders().set("Content-Type","application/json; charset=utf-8");x.getResponseHeaders().set("Cache-Control","no-store");x.sendResponseHeaders(status,b.length);try(OutputStream o=x.getResponseBody()){o.write(b);}}
    private static String toJson(Object v){if(v==null)return"null";if(v instanceof String||v instanceof Character||v instanceof java.sql.Date||v instanceof java.sql.Time||v instanceof java.sql.Timestamp)return quote(v.toString());if(v instanceof Number||v instanceof Boolean)return v.toString();if(v instanceof Map<?,?>m){StringJoiner j=new StringJoiner(",","{","}");m.forEach((k,val)->j.add(quote(k.toString())+":"+toJson(val)));return j.toString();}if(v instanceof Iterable<?>it){StringJoiner j=new StringJoiner(",","[","]");for(Object o:it)j.add(toJson(o));return j.toString();}return quote(v.toString());}
    private static String quote(String s){StringBuilder b=new StringBuilder("\"");for(char c:s.toCharArray())switch(c){case'"'->b.append("\\\"");case'\\'->b.append("\\\\");case'\n'->b.append("\\n");case'\r'->b.append("\\r");case'\t'->b.append("\\t");default->{if(c<32)b.append(String.format("\\u%04x",(int)c));else b.append(c);}}return b.append('"').toString();}

    private static final class Database{
        static String url,user,password;static void init(String u,String n,String p){url=u;user=n;password=p;}
        static Connection connect()throws SQLException{return DriverManager.getConnection(url,user,password);}
        private interface SQLWork {Map<String,Object> run(Connection c)throws Exception;}
        static Map<String,Object> transaction(SQLWork work)throws Exception {
            try(Connection c=connect()) {
                c.setAutoCommit(false);
                try {Map<String,Object> result=work.run(c);c.commit();return result;}
                catch(Exception e){c.rollback();throw e;}
            }
        }
        static List<Map<String,Object>> query(Connection c,String sql,Object...args)throws SQLException {
            try(PreparedStatement s=c.prepareStatement(sql)) {
                for(int i=0;i<args.length;i++)s.setObject(i+1,args[i]);
                try(ResultSet r=s.executeQuery()) {
                    ResultSetMetaData md=r.getMetaData();List<Map<String,Object>> out=new ArrayList<>();
                    while(r.next()){Map<String,Object> row=new LinkedHashMap<>();for(int i=1;i<=md.getColumnCount();i++)row.put(md.getColumnLabel(i),r.getObject(i));out.add(row);}return out;
                }
            }
        }
        static int execute(Connection c,String sql,Object...args)throws SQLException {try(PreparedStatement s=c.prepareStatement(sql)){for(int i=0;i<args.length;i++)s.setObject(i+1,args[i]);return s.executeUpdate();}}
        static int insert(Connection c,String sql,Object...args)throws SQLException {try(PreparedStatement s=c.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)){for(int i=0;i<args.length;i++)s.setObject(i+1,args[i]);s.executeUpdate();try(ResultSet keys=s.getGeneratedKeys()){if(!keys.next())throw new SQLException("Record creation failed.");return keys.getInt(1);}}}
        static List<Map<String,Object>> query(String sql,Object...args)throws SQLException{try(Connection c=connect();PreparedStatement s=c.prepareStatement(sql)){for(int i=0;i<args.length;i++)s.setObject(i+1,args[i]);try(ResultSet r=s.executeQuery()){ResultSetMetaData md=r.getMetaData();List<Map<String,Object>> out=new ArrayList<>();while(r.next()){Map<String,Object> row=new LinkedHashMap<>();for(int i=1;i<=md.getColumnCount();i++)row.put(md.getColumnLabel(i),r.getObject(i));out.add(row);}return out;}}}
        static Map<String,Object> one(String sql,Object...a)throws SQLException{List<Map<String,Object>> r=query(sql,a);return r.isEmpty()?Map.of():r.get(0);}
        static int update(String sql,Object...args)throws SQLException{try(Connection c=connect();PreparedStatement s=c.prepareStatement(sql)){for(int i=0;i<args.length;i++)s.setObject(i+1,args[i]);return s.executeUpdate();}}
        static String backup()throws Exception{
            Path root=Path.of(".").toAbsolutePath().normalize(),dir=root.resolve("backups").normalize();
            if(!dir.startsWith(root)||!dir.equals(root.resolve("backups")))throw new IOException("Backup path must remain inside the project checkout.");
            Files.createDirectories(dir);
            String dbName=CONFIG.getProperty("db.name","gms");String stamp=java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));Path file=dir.resolve(dbName+"_web_"+stamp+".sql").normalize();if(!file.startsWith(dir))throw new IOException("Invalid backup file path.");
            String dump=System.getenv().getOrDefault("GMS_MYSQLDUMP_PATH",CONFIG.getProperty("db.mysqldump_path","mysqldump"));
            List<String> cmd=List.of(dump,"--defaults-extra-file=src/resources/mysql.cnf","--no-tablespaces","--single-transaction",dbName);
            Process process=new ProcessBuilder(cmd).directory(root.toFile()).redirectOutput(file.toFile()).redirectError(ProcessBuilder.Redirect.DISCARD).start();int code=process.waitFor();
            if(code!=0){Files.deleteIfExists(file);throw new IOException("mysqldump failed. Check the configured MySQL tools and local connection file.");}
            return file.getFileName().toString();
        }
    }
}
