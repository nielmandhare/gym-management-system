package gui;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import security.*;

public class TrainerDashboard extends JFrame {
    private final JTabbedPane tabs=new JTabbedPane();
    private static final String[] VIEWS={"Profile","Assigned members","My attendance","Member progress","Sessions","Member attendance"};
    private static final String[][] HEADERS={
        {"Trainer ID","Name","Mobile","NIC","Hired"},
        {"Member ID","Name","Mobile","Start","End","Status"},
        {"Date","Check in","Check out"},
        {"Member","Date","Height (cm)","Weight (kg)","Body fat (%)","BMI"},
        {"Session record","Member ID","Start","End","Total sessions","Completed","Status"},
        {"Member","Date","Check in"}};
    public TrainerDashboard() {
        Session.requireTrainer();setTitle("Trainer Dashboard — "+Session.current().username());setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        util.AppIconUtil.applyIcon(this);
        add(tabs,BorderLayout.CENTER);JPanel actions=new JPanel();
        JButton refresh=new JButton("Refresh"),edit=new JButton("Edit My Profile"),change=new JButton("Change Password"),logout=new JButton("Log Out");
        JButton progress=new JButton("Record Progress");progress.addActionListener(e->recordProgress());
        actions.add(refresh);actions.add(edit);actions.add(change);actions.add(progress);actions.add(logout);add(actions,BorderLayout.SOUTH);
        refresh.addActionListener(e->refresh());edit.addActionListener(e->edit());change.addActionListener(e->change());logout.addActionListener(e->logOut());
        addWindowListener(new java.awt.event.WindowAdapter(){public void windowClosed(java.awt.event.WindowEvent e){if(Session.current()!=null && Session.current().role().equals("TRAINER")) Session.logout();}});
        refresh();setSize(950,620);setLocationRelativeTo(null);
    }
    public void refresh() {
        try {
            Session.requireTrainer();int selected=tabs.getSelectedIndex();tabs.removeAll();
            for(int i=0;i<VIEWS.length;i++) {
                DefaultTableModel model=new DefaultTableModel(HEADERS[i],0) { public boolean isCellEditable(int r,int c){return false;} };
                for(Object[] row:Accounts.trainerData(VIEWS[i])) model.addRow(row);
                tabs.addTab(VIEWS[i],new JScrollPane(new JTable(model)));
            }
            if(selected>=0) tabs.setSelectedIndex(selected);
        } catch(Exception e) { error(e); }
    }
    private record MemberChoice(int id,String name){public String toString(){return id+" — "+name;}}
    private void recordProgress() {
        try {
            JComboBox<MemberChoice> member=new JComboBox<>();
            for(Object[] row:Accounts.progressChoices())member.addItem(new MemberChoice(((Number)row[0]).intValue(),row[1].toString()));
            if(member.getItemCount()==0)throw new IllegalArgumentException("No active members are assigned to you.");
            JTextField date=new JTextField(java.time.LocalDate.now().toString()),height=new JTextField(),weight=new JTextField(),fat=new JTextField();
            JPanel form=new JPanel(new GridLayout(0,2,8,8));form.add(new JLabel("Assigned member"));form.add(member);form.add(new JLabel("Date (YYYY-MM-DD)"));form.add(date);
            form.add(new JLabel("Height (cm)"));form.add(height);form.add(new JLabel("Weight (kg)"));form.add(weight);form.add(new JLabel("Body fat (%)"));form.add(fat);
            if(JOptionPane.showConfirmDialog(this,form,"Record Member Progress",JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION){
                Accounts.recordProgress(((MemberChoice)member.getSelectedItem()).id(),date.getText().trim(),Double.parseDouble(height.getText().trim()),Double.parseDouble(weight.getText().trim()),Double.parseDouble(fat.getText().trim()));refresh();
            }
        }catch(Exception e){error(e);}
    }
    private void edit() {
        try {
            Object[] profile=Accounts.trainerData("Profile").get(0);
            JTextField name=new JTextField(profile[1].toString()),mobile=new JTextField(profile[2].toString());
            JPanel panel=new JPanel(new GridLayout(2,2,8,8));panel.add(new JLabel("Name"));panel.add(name);panel.add(new JLabel("Mobile"));panel.add(mobile);
            if(JOptionPane.showConfirmDialog(this,panel,"Edit My Profile",JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION) {
                Accounts.updateProfile(name.getText().trim(),mobile.getText().trim());refresh();
            }
        } catch(Exception e) { error(e); }
    }
    private void change() {
        JPasswordField old=new JPasswordField(),next=new JPasswordField(),confirm=new JPasswordField();
        JPanel panel=new JPanel(new GridLayout(3,2,8,8));panel.add(new JLabel("Current password"));panel.add(old);panel.add(new JLabel("New password"));panel.add(next);panel.add(new JLabel("Confirm password"));panel.add(confirm);
        try {
            if(JOptionPane.showConfirmDialog(this,panel,"Change My Password",JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION) {
                String value=new String(next.getPassword());
                if(!value.equals(new String(confirm.getPassword()))) throw new IllegalArgumentException("Passwords do not match.");
                Accounts.changePassword(new String(old.getPassword()),value);JOptionPane.showMessageDialog(this,"Password changed.");
            }
        } catch(Exception e) { error(e); }
        finally {old.setText("");next.setText("");confirm.setText("");}
    }
    public void logOut() { Session.logout();dispose();new LoginForm().setVisible(true); }
    private void error(Exception e) { JOptionPane.showMessageDialog(this,e.getMessage(),"Trainer Dashboard",JOptionPane.ERROR_MESSAGE); }
}

