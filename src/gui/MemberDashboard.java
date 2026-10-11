package gui;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import security.*;

public class MemberDashboard extends JFrame {
    private final JTabbedPane tabs=new JTabbedPane();
    private static final String[] VIEWS={"Profile / Membership","My attendance","My progress","My training"};
    private static final String[][] HEADERS={
        {"Member ID","Name","Mobile","Joined","Membership","Price","Paid","Active"},
        {"Date","Check in"},{"Date","Height (cm)","Weight (kg)","Body fat (%)","BMI"},
        {"Trainer","Start","End","Total sessions","Completed","Status"}};
    public MemberDashboard() {
        Session.requireMember();setTitle("Member Dashboard — "+Session.current().username());setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        util.AppIconUtil.applyIcon(this);add(tabs,BorderLayout.CENTER);
        JPanel actions=new JPanel();JButton refresh=new JButton("Refresh"),edit=new JButton("Edit My Profile"),change=new JButton("Change Password"),logout=new JButton("Log Out");
        actions.add(refresh);actions.add(edit);actions.add(change);actions.add(logout);add(actions,BorderLayout.SOUTH);
        refresh.addActionListener(e->refresh());edit.addActionListener(e->edit());change.addActionListener(e->change());logout.addActionListener(e->logOut());
        addWindowListener(new java.awt.event.WindowAdapter(){public void windowClosed(java.awt.event.WindowEvent e){if(Session.current()!=null && Session.current().role().equals("MEMBER")) Session.logout();}});
        refresh();setSize(950,620);setLocationRelativeTo(null);
    }
    public void refresh() {
        try {
            Session.requireMember();int selected=tabs.getSelectedIndex();tabs.removeAll();
            for(int i=0;i<VIEWS.length;i++) {
                DefaultTableModel model=new DefaultTableModel(HEADERS[i],0){public boolean isCellEditable(int r,int c){return false;}};
                for(Object[] row:Accounts.memberData(VIEWS[i])) model.addRow(row);
                tabs.addTab(VIEWS[i],new JScrollPane(new JTable(model)));
            }
            if(selected>=0) tabs.setSelectedIndex(selected);
        } catch(Exception e){error(e);}
    }
    private void edit() {
        try {
            Object[] profile=Accounts.memberData("Profile / Membership").get(0);
            JTextField name=new JTextField(profile[1].toString()),mobile=new JTextField(profile[2].toString());
            JPanel panel=new JPanel(new GridLayout(2,2,8,8));panel.add(new JLabel("Name"));panel.add(name);panel.add(new JLabel("Mobile"));panel.add(mobile);
            if(JOptionPane.showConfirmDialog(this,panel,"Edit My Profile",JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION){Accounts.updateMemberProfile(name.getText().trim(),mobile.getText().trim());refresh();}
        } catch(Exception e){error(e);}
    }
    private void change() {
        JPasswordField old=new JPasswordField(),next=new JPasswordField(),confirm=new JPasswordField();
        JPanel panel=new JPanel(new GridLayout(3,2,8,8));panel.add(new JLabel("Current password"));panel.add(old);panel.add(new JLabel("New password"));panel.add(next);panel.add(new JLabel("Confirm password"));panel.add(confirm);
        try {
            if(JOptionPane.showConfirmDialog(this,panel,"Change My Password",JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION){
                String value=new String(next.getPassword());if(!value.equals(new String(confirm.getPassword())))throw new IllegalArgumentException("Passwords do not match.");
                Accounts.changePassword(new String(old.getPassword()),value);JOptionPane.showMessageDialog(this,"Password changed.");
            }
        }catch(Exception e){error(e);}finally{old.setText("");next.setText("");confirm.setText("");}
    }
    public void logOut(){Session.logout();dispose();new LoginForm().setVisible(true);}
    private void error(Exception e){JOptionPane.showMessageDialog(this,e.getMessage(),"Member Dashboard",JOptionPane.ERROR_MESSAGE);}
}
