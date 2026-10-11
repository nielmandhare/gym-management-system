package panels.dialog;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import security.*;

public class ManageProductsDialog extends JDialog {
    private final DefaultTableModel model=new DefaultTableModel(new String[]{"ID","Name","Price","Stock","Description"},0){public boolean isCellEditable(int r,int c){return false;}};
    private final JTable table=new JTable(model);
    private final JTextField name=new JTextField(),price=new JTextField(),stock=new JTextField(),description=new JTextField();
    private Integer selectedId;
    public ManageProductsDialog(Frame owner,boolean modal){
        super(owner,modal);Session.requireAdmin();setTitle("Manage Products");setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(8,8));add(new JScrollPane(table),BorderLayout.CENTER);
        JPanel bottom=new JPanel(new BorderLayout()),form=new JPanel(new GridLayout(0,2,8,8));
        form.add(new JLabel("Name"));form.add(name);form.add(new JLabel("Price"));form.add(price);form.add(new JLabel("Stock"));form.add(stock);form.add(new JLabel("Description"));form.add(description);bottom.add(form,BorderLayout.CENTER);
        JPanel buttons=new JPanel();JButton clear=new JButton("New / Clear"),save=new JButton("Save Product"),delete=new JButton("Delete Product");
        buttons.add(clear);buttons.add(save);buttons.add(delete);bottom.add(buttons,BorderLayout.SOUTH);add(bottom,BorderLayout.SOUTH);
        clear.addActionListener(e->clear());save.addActionListener(e->save());delete.addActionListener(e->delete());
        table.getSelectionModel().addListSelectionListener(e->{if(!e.getValueIsAdjusting() && table.getSelectedRow()>=0){
            int row=table.getSelectedRow();selectedId=((Number)model.getValueAt(row,0)).intValue();name.setText(model.getValueAt(row,1).toString());price.setText(model.getValueAt(row,2).toString());stock.setText(model.getValueAt(row,3).toString());description.setText(java.util.Objects.toString(model.getValueAt(row,4),""));
        }});
        refresh();setSize(750,500);setLocationRelativeTo(owner);
    }
    public void refresh(){try{Session.requireAdmin();model.setRowCount(0);for(Object[] row:Accounts.products())model.addRow(row);clear();}catch(Exception e){error(e);}}
    private void clear(){selectedId=null;table.clearSelection();name.setText("");price.setText("");stock.setText("");description.setText("");}
    private void save(){try{Accounts.saveProduct(selectedId,name.getText().trim(),Double.parseDouble(price.getText().trim()),Integer.parseInt(stock.getText().trim()),description.getText().trim());refresh();}catch(Exception e){error(e);}}
    private void delete(){try{Session.requireAdmin();if(selectedId==null)throw new IllegalArgumentException("Select a product first.");
        if(JOptionPane.showConfirmDialog(this,"Delete selected product? Products with sales history are protected.","Delete Product",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION){Accounts.deleteProduct(selectedId);refresh();}
    }catch(Exception e){error(e);}}
    private void error(Exception e){JOptionPane.showMessageDialog(this,e instanceof java.sql.SQLIntegrityConstraintViolationException?"This product has related sales records and cannot be deleted.":e.getMessage(),"Manage Products",JOptionPane.ERROR_MESSAGE);}
}
