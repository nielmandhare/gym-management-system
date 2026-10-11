package panels.dialog;

import java.awt.*;
import javax.swing.*;
import security.*;

public class MembershipDialog extends JDialog {
    private record Choice(int id,String name,double price){public String toString(){return name+" — "+price;}}
    private final JComboBox<Choice> membership=new JComboBox<>();
    private final JCheckBox paid=new JCheckBox("Membership paid");
    private final JTextField price=new JTextField();
    private final int memberId;
    public MembershipDialog(Frame owner,int memberId,int currentMembership,boolean currentPaid){
        super(owner,true);Session.requireAdmin();this.memberId=memberId;setTitle("Membership — Member "+memberId);setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        JPanel form=new JPanel(new GridLayout(0,2,8,8));form.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        form.add(new JLabel("Membership"));form.add(membership);form.add(new JLabel("Payment status"));form.add(paid);paid.setSelected(currentPaid);
        form.add(new JLabel("Plan price (all members)"));form.add(price);
        JButton save=new JButton("Save Member Membership"),rate=new JButton("Update Plan Price");form.add(save);form.add(rate);add(form);
        membership.addActionListener(e->{Choice c=(Choice)membership.getSelectedItem();if(c!=null)price.setText(Double.toString(c.price()));});
        try{for(Object[] row:Accounts.memberships()){Choice c=new Choice(((Number)row[0]).intValue(),row[1].toString(),((Number)row[2]).doubleValue());membership.addItem(c);if(c.id()==currentMembership)membership.setSelectedItem(c);}}catch(Exception e){error(e);}
        save.addActionListener(e->save());rate.addActionListener(e->rate());pack();setLocationRelativeTo(owner);
    }
    private void save(){try{Choice c=(Choice)membership.getSelectedItem();if(c==null)throw new IllegalArgumentException("Select a membership.");Accounts.setMembership(memberId,c.id(),paid.isSelected());dispose();}catch(Exception e){error(e);}}
    private void rate(){try{Choice c=(Choice)membership.getSelectedItem();if(c==null)throw new IllegalArgumentException("Select a membership.");Accounts.setMembershipPrice(c.id(),Double.parseDouble(price.getText().trim()));JOptionPane.showMessageDialog(this,"Plan price updated for all members on this plan.");}catch(Exception e){error(e);}}
    private void error(Exception e){JOptionPane.showMessageDialog(this,e.getMessage(),"Membership",JOptionPane.ERROR_MESSAGE);}
}
