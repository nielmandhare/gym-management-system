package web;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Locale;

/** The six desktop report types, with browser-export layouts and readable status values. */
public final class WebReports {
    private WebReports() { }
    public interface Query { List<Map<String,Object>> run(String sql) throws Exception; }
    private record Definition(String title,String[] keys,String[] labels,float[] widths,String sql) { }
    private static final Color GREEN=new Color(30,57,45),MUTED=new Color(105,124,100),LINE=new Color(218,227,210),PALE=new Color(246,249,241);
    private static final Font BODY=FontFactory.getFont(FontFactory.HELVETICA,9,Font.NORMAL,GREEN);
    private static final Font SMALL=FontFactory.getFont(FontFactory.HELVETICA,8,Font.NORMAL,MUTED);
    private static final Font HEAD=FontFactory.getFont(FontFactory.HELVETICA,9,Font.BOLD,Color.WHITE);

    private static Definition definition(String name) {
        return switch(name) {
            case "members" -> new Definition("Member report",new String[]{"id","name","joined","mobile","membership","status"},new String[]{"ID","Member","Joined","Mobile","Membership","Status"},new float[]{.5f,2f,1.1f,1.2f,1.15f,.9f},"SELECT m.member_id id,m.name,DATE_FORMAT(m.joined_date,'%Y-%m-%d') joined,m.mobile,ms.membership_type membership,CASE WHEN m.status_status_id=1 THEN 'Active' ELSE 'Inactive' END status FROM member m JOIN membership ms ON ms.membership_type_id=m.membership_type_membership_type_id ORDER BY m.member_id");
            case "trainers" -> new Definition("Trainer report",new String[]{"id","name","mobile","nic","hired","status"},new String[]{"ID","Trainer","Mobile","Identification","Hire date","Status"},new float[]{.5f,2f,1.2f,1.3f,1.1f,.9f},"SELECT trainer_id id,name,mobile,nic,DATE_FORMAT(hired_date,'%Y-%m-%d') hired,CASE WHEN status_status_id=1 THEN 'Active' ELSE 'Inactive' END status FROM trainer ORDER BY trainer_id");
            case "attendance" -> new Definition("Member attendance report",new String[]{"member","mobile","date","checkin"},new String[]{"Member","Mobile","Date","Check-in time"},new float[]{2.3f,1.3f,1.3f,1.1f},"SELECT m.name AS `member`,m.mobile,DATE_FORMAT(a.date,'%Y-%m-%d') date,a.check_in_time checkin FROM member_attendance a JOIN member m ON m.member_id=a.member_member_id ORDER BY a.date DESC,a.check_in_time DESC");
            case "payments" -> new Definition("Membership payment report",new String[]{"member","amount","method","reference","date"},new String[]{"Member","Amount (INR)","Method","Reference","Date"},new float[]{1.8f,1.1f,1.3f,2f,1.2f},"SELECT m.name AS `member`,p.paid_amount amount,p.paid_method method,p.reference_no reference,DATE_FORMAT(p.date,'%Y-%m-%d') date FROM payment_history p JOIN member m ON m.member_id=p.member_member_id ORDER BY p.date DESC");
            case "sales" -> new Definition("Product sales report",new String[]{"product","quantity","price","total","method","reference","date"},new String[]{"Product","Quantity","Unit price (INR)","Total (INR)","Method","Reference","Date"},new float[]{1.6f,.6f,1f,1f,1.25f,1.8f,1.1f},"SELECT p.name AS `product`,o.quantity,o.unit_price price,o.total_amount total,o.paid_method method,o.reference_no reference,DATE_FORMAT(o.date,'%Y-%m-%d') date FROM order_history o JOIN product p ON p.product_id=o.product_product_id ORDER BY o.date DESC");
            case "equipment" -> new Definition("Equipment and maintenance report",new String[]{"id","name","condition","serial","purchased","warranty"},new String[]{"ID","Equipment","Condition","Serial number","Purchased","Warranty expiry"},new float[]{.5f,1.7f,1.4f,1.8f,1.1f,1.1f},"SELECT equipment_id id,name,status `condition`,serial_number serial,DATE_FORMAT(purchase_date,'%Y-%m-%d') purchased,DATE_FORMAT(warranty_exp,'%Y-%m-%d') warranty FROM equipment ORDER BY equipment_id");
            default -> throw new IllegalArgumentException("Unknown report.");
        };
    }
    public static byte[] export(String name,Query query) throws Exception {
        Definition report=definition(name);List<Map<String,Object>> rows=query.run(report.sql);
        ByteArrayOutputStream output=new ByteArrayOutputStream();
        Document document=new Document(name.equals("sales")||name.equals("equipment")?PageSize.A4.rotate():PageSize.A4,36,36,46,44);
        PdfWriter writer=PdfWriter.getInstance(document,output);
        writer.setPageEvent(new PdfPageEventHelper(){
            @Override public void onEndPage(PdfWriter writer,Document doc) {
                ColumnText.showTextAligned(writer.getDirectContent(),Element.ALIGN_LEFT,new Phrase("GYM MANAGEMENT SYSTEM  |  "+report.title,SMALL),doc.left(),doc.getPageSize().getHeight()-24,0);
                ColumnText.showTextAligned(writer.getDirectContent(),Element.ALIGN_LEFT,new Phrase("Generated from saved gym records",SMALL),doc.left(),23,0);
                ColumnText.showTextAligned(writer.getDirectContent(),Element.ALIGN_RIGHT,new Phrase("Page "+writer.getPageNumber(),SMALL),doc.right(),23,0);
            }
        });
        document.addTitle("Gym Management System - "+report.title);document.addAuthor("Gym Management System");document.open();
        Paragraph title=new Paragraph(report.title,FontFactory.getFont(FontFactory.HELVETICA,23,Font.BOLD,GREEN));title.setSpacingAfter(8);document.add(title);
        Paragraph meta=new Paragraph("Records: "+rows.size()+"    |    Generated: "+LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm",Locale.ENGLISH)),SMALL);meta.setSpacingAfter(15);document.add(meta);
        document.add(table(report.keys,report.labels,report.widths,rows));
        if(name.equals("payments")||name.equals("sales")) {
            String key=name.equals("payments")?"amount":"total";double total=rows.stream().mapToDouble(r->((Number)r.get(key)).doubleValue()).sum();
            Paragraph summary=new Paragraph("Total recorded amount: INR "+String.format(Locale.ENGLISH,"%,.2f",total),FontFactory.getFont(FontFactory.HELVETICA,11,Font.BOLD,GREEN));summary.setSpacingBefore(15);document.add(summary);
        }
        if(name.equals("equipment")) {
            document.newPage();
            Paragraph maintenance=new Paragraph("Maintenance history",FontFactory.getFont(FontFactory.HELVETICA,15,Font.BOLD,GREEN));maintenance.setSpacingBefore(22);maintenance.setSpacingAfter(12);document.add(maintenance);
            List<Map<String,Object>> history=query.run("SELECT e.name equipment,DATE_FORMAT(l.date,'%Y-%m-%d') date,l.description,l.cost,l.maintained_by provider FROM maintenance_log l JOIN equipment e ON e.equipment_id=l.equipment_equipment_id ORDER BY l.date DESC");
            document.add(table(new String[]{"equipment","date","description","cost","provider"},new String[]{"Equipment","Date","Description","Cost (INR)","Maintained by"},new float[]{1.5f,1f,2.4f,1f,1.6f},history));
        }
        document.close();return output.toByteArray();
    }
    private static PdfPTable table(String[] keys,String[] labels,float[] widths,List<Map<String,Object>> rows) {
        PdfPTable table=new PdfPTable(widths);table.setWidthPercentage(100);table.setHeaderRows(1);table.setSplitLate(false);
        for(String label:labels){PdfPCell cell=new PdfPCell(new Phrase(label,HEAD));cell.setBackgroundColor(GREEN);cell.setBorderColor(GREEN);cell.setPadding(8);table.addCell(cell);}
        int index=0;
        for(Map<String,Object> row:rows) {
            for(String key:keys) {
                Object value=row.get(key);String text=value==null||value.toString().isBlank()?"-":value.toString();
                if(List.of("amount","price","total","cost").contains(key)&&value instanceof Number)text=String.format(Locale.ENGLISH,"%,.2f",((Number)value).doubleValue());
                PdfPCell cell=new PdfPCell(new Phrase(text,BODY));cell.setBackgroundColor(index%2==0?Color.WHITE:PALE);cell.setBorderColor(LINE);cell.setPadding(7);cell.setVerticalAlignment(Element.ALIGN_MIDDLE);table.addCell(cell);
            }
            index++;
        }
        if(rows.isEmpty()){PdfPCell empty=new PdfPCell(new Phrase("No records available.",BODY));empty.setColspan(keys.length);empty.setPadding(14);empty.setBorderColor(LINE);table.addCell(empty);}
        return table;
    }
}
