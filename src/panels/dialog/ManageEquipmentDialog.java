package panels.dialog;

import connection.MySQL;
import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import util.OptionPane;

/** Equipment CRUD using the existing equipment table and Swing interface. */
public class ManageEquipmentDialog extends JDialog {
    private final JTextField nameField = new JTextField();
    private final JTextField serialField = new JTextField();
    private final JTextField purchaseField = new JTextField();
    private final JTextField warrantyField = new JTextField();
    private final JComboBox<String> statusField = new JComboBox<>(new String[]{"GOOD","UNDER MAINTENANCE","DAMAGED","RETIRED"});
    private final DefaultTableModel model = new DefaultTableModel(new String[]{"ID","Name","Purchase Date","Status","Serial Number","Warranty Expiry"},0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(model);
    private Integer selectedId;

    public ManageEquipmentDialog(Frame parent, boolean modal) {
        super(parent,"Manage Equipment",modal);
        security.Session.requireAdmin();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        JPanel fields = new JPanel(new GridLayout(5,2,10,10));
        fields.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        fields.add(new JLabel("Name")); fields.add(nameField);
        fields.add(new JLabel("Serial number (optional)")); fields.add(serialField);
        fields.add(new JLabel("Purchase date (yyyy-MM-dd, optional)")); fields.add(purchaseField);
        fields.add(new JLabel("Warranty expiry (yyyy-MM-dd, optional)")); fields.add(warrantyField);
        fields.add(new JLabel("Status")); fields.add(statusField);
        add(fields,BorderLayout.NORTH); add(new JScrollPane(table),BorderLayout.CENTER);
        JPanel buttons = new JPanel();
        JButton add = new JButton("Add"), update = new JButton("Update Selected"), delete = new JButton("Delete Selected"), clear = new JButton("Clear");
        buttons.add(add); buttons.add(update); buttons.add(delete); buttons.add(clear); add(buttons,BorderLayout.SOUTH);
        add.addActionListener(e -> save(false)); update.addActionListener(e -> save(true));
        delete.addActionListener(e -> deleteSelected()); clear.addActionListener(e -> clear());
        table.getSelectionModel().addListSelectionListener(e -> {
            int row=table.getSelectedRow();
            if(row>=0) {
                selectedId=((Number)model.getValueAt(row,0)).intValue();
                nameField.setText(value(row,1)); purchaseField.setText(value(row,2));
                statusField.setSelectedItem(value(row,3)); serialField.setText(value(row,4)); warrantyField.setText(value(row,5));
            }
        });
        refresh(); setSize(900,500); setLocationRelativeTo(parent);
    }
    private String value(int row,int col) { Object value=model.getValueAt(row,col); return value==null?"":value.toString(); }
    private static Object optional(String value) { return value.isBlank()?null:value.trim(); }
    private static Date date(String value) { return value.isBlank()?null:Date.valueOf(java.time.LocalDate.parse(value.trim())); }
    private void clear() {
        selectedId=null; table.clearSelection(); nameField.setText(""); serialField.setText("");
        purchaseField.setText(""); warrantyField.setText(""); statusField.setSelectedIndex(0);
    }
    private void refresh() {
        model.setRowCount(0);
        try(ResultSet rs=MySQL.executeSearch("SELECT * FROM equipment ORDER BY equipment_id")) {
            while(rs.next()) model.addRow(new Object[]{rs.getInt("equipment_id"),rs.getString("name"),rs.getDate("purchase_date"),rs.getString("status"),rs.getString("serial_number"),rs.getDate("warranty_exp")});
        } catch(SQLException e) { OptionPane.showMessage(this,"Error","Could not load equipment: "+e.getMessage(),"ERROR"); }
    }
    private void save(boolean update) {
        if(nameField.getText().isBlank() || (update && selectedId==null)) {
            OptionPane.showMessage(this,"Warning","Enter a name and select a row when updating.","WARNING"); return;
        }
        try {
            Object purchase=date(purchaseField.getText()), warranty=date(warrantyField.getText());
            boolean ok=update
                    ? MySQL.executeIUD("UPDATE equipment SET name=?,purchase_date=?,status=?,serial_number=?,warranty_exp=? WHERE equipment_id=?",nameField.getText().trim(),purchase,statusField.getSelectedItem(),optional(serialField.getText()),warranty,selectedId)
                    : MySQL.executeIUD("INSERT INTO equipment (name,purchase_date,status,serial_number,warranty_exp) VALUES (?,?,?,?,?)",nameField.getText().trim(),purchase,statusField.getSelectedItem(),optional(serialField.getText()),warranty);
            if(ok) { clear(); refresh(); }
            else OptionPane.showMessage(this,"Error","Could not save equipment. Check for a duplicate serial number.","ERROR");
        } catch(RuntimeException e) { OptionPane.showMessage(this,"Warning","Dates must be valid and use yyyy-MM-dd.","WARNING"); }
    }
    private void deleteSelected() {
        if(selectedId==null) { OptionPane.showMessage(this,"Warning","Select an equipment row first.","WARNING"); return; }
        if(JOptionPane.showConfirmDialog(this,"Delete the selected equipment?","Delete Equipment",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION) return;
        if(MySQL.executeIUD("DELETE FROM equipment WHERE equipment_id=?",selectedId)) { clear(); refresh(); }
        else OptionPane.showMessage(this,"Error","Equipment with maintenance records cannot be deleted. Mark it RETIRED instead.","ERROR");
    }
}
