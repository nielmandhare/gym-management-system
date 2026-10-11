package panels;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import security.*;

public class UserManagementPanel extends JPanel {
    private final JTextField search=new JTextField(18), username=new JTextField(), name=new JTextField(), mobile=new JTextField();
    private final JPasswordField password=new JPasswordField();
    private final JComboBox<String> role=new JComboBox<>(new String[]{"ADMIN","TRAINER","MEMBER"});
    private final JCheckBox active=new JCheckBox("Active",true);
    private final JComboBox<TrainerChoice> trainer=new JComboBox<>();
    private final JComboBox<TrainerChoice> member=new JComboBox<>();
    private final DefaultTableModel model=new DefaultTableModel(new String[]{"ID","Username","Name","Mobile","Role","Active","Trainer ID","Member ID"},0) {
        public boolean isCellEditable(int r,int c) { return false; }
    };
    private final JTable table=new JTable(model);
    private Integer selectedId;
    private record TrainerChoice(Integer id,String name) { public String toString() { return id==null?"No profile":id+" — "+name; } }
    public UserManagementPanel() {
        Session.requireAdmin(); setLayout(new BorderLayout(8,8));setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        JPanel top=new JPanel();top.add(new JLabel("User Management"));top.add(search);
        JButton find=new JButton("Search");top.add(find);find.addActionListener(e->refresh());search.addActionListener(e->refresh());add(top,BorderLayout.NORTH);
        add(new JScrollPane(table),BorderLayout.CENTER);
        JPanel bottom=new JPanel(new BorderLayout());JPanel form=new JPanel(new GridLayout(0,2,8,8));
        form.add(new JLabel("Username"));form.add(username);form.add(new JLabel("Name"));form.add(name);
        form.add(new JLabel("Mobile"));form.add(mobile);form.add(new JLabel("Role"));form.add(role);
        form.add(new JLabel("Trainer profile (trainer role)"));form.add(trainer);
        form.add(new JLabel("Member profile (member role)"));form.add(member);
        form.add(new JLabel("Password (new account only)"));form.add(password);
        form.add(new JLabel("Account status"));form.add(active);bottom.add(form,BorderLayout.CENTER);
        JPanel buttons=new JPanel();JButton clear=new JButton("New / Clear"), save=new JButton("Save User"), reset=new JButton("Reset Password");
        buttons.add(clear);buttons.add(save);buttons.add(reset);bottom.add(buttons,BorderLayout.SOUTH);add(bottom,BorderLayout.SOUTH);
        clear.addActionListener(e->clear());save.addActionListener(e->save());reset.addActionListener(e->reset());
        table.getSelectionModel().addListSelectionListener(e->{
            if(!e.getValueIsAdjusting() && table.getSelectedRow()>=0) {
                int r=table.convertRowIndexToModel(table.getSelectedRow());selectedId=((Number)model.getValueAt(r,0)).intValue();
                username.setText(model.getValueAt(r,1).toString());name.setText(model.getValueAt(r,2).toString());mobile.setText(model.getValueAt(r,3).toString());
                role.setSelectedItem(model.getValueAt(r,4));active.setSelected(Boolean.TRUE.equals(model.getValueAt(r,5)));password.setText("");
                trainer.setSelectedIndex(0);
                for(int i=0;i<trainer.getItemCount();i++) if(Objects.equals(trainer.getItemAt(i).id(),model.getValueAt(r,6)==null?null:((Number)model.getValueAt(r,6)).intValue())) trainer.setSelectedIndex(i);
                member.setSelectedIndex(0);
                for(int i=0;i<member.getItemCount();i++) if(Objects.equals(member.getItemAt(i).id(),model.getValueAt(r,7)==null?null:((Number)model.getValueAt(r,7)).intValue())) member.setSelectedIndex(i);
            }
        });
        refresh();
    }
    public void refresh() {
        try {
            Session.requireAdmin();model.setRowCount(0);
            for(Object[] row:Accounts.users(search.getText().trim())) model.addRow(row);
            trainer.removeAllItems();trainer.addItem(new TrainerChoice(null,""));
            for(Object[] row:Accounts.trainerChoices()) trainer.addItem(new TrainerChoice(((Number)row[0]).intValue(),row[1].toString()));
            member.removeAllItems();member.addItem(new TrainerChoice(null,""));
            for(Object[] row:Accounts.memberChoices()) member.addItem(new TrainerChoice(((Number)row[0]).intValue(),row[1].toString()));
            clear();
        } catch(Exception e) { error(e); }
    }
    private void clear() {
        selectedId=null;table.clearSelection();username.setText("");name.setText("");mobile.setText("");password.setText("");role.setSelectedIndex(0);active.setSelected(true);
        if(trainer.getItemCount()>0) trainer.setSelectedIndex(0);
        if(member.getItemCount()>0) member.setSelectedIndex(0);
    }
    private void save() {
        try {
            Accounts.save(selectedId,username.getText().trim(),name.getText().trim(),mobile.getText().trim(),role.getSelectedItem().toString(),active.isSelected(),((TrainerChoice)trainer.getSelectedItem()).id(),((TrainerChoice)member.getSelectedItem()).id(),new String(password.getPassword()));
            password.setText("");refresh();JOptionPane.showMessageDialog(this,"User saved.");
        } catch(Exception e) { error(e); }
    }
    private void reset() {
        try {
            Session.requireAdmin();if(selectedId==null) throw new IllegalArgumentException("Select a user first.");
            JPasswordField next=new JPasswordField();
            if(JOptionPane.showConfirmDialog(this,next,"New password (3–128 characters)",JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION) {
                Accounts.resetPassword(selectedId,new String(next.getPassword()));next.setText("");JOptionPane.showMessageDialog(this,"Password reset.");
            }
        } catch(Exception e) { error(e); }
    }
    private void error(Exception e) {
        String message=e instanceof java.sql.SQLIntegrityConstraintViolationException ? "Username, mobile or profile is already linked to an account." : e.getMessage();
        JOptionPane.showMessageDialog(this,message,"User Management",JOptionPane.ERROR_MESSAGE);
    }
}

