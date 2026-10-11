package panels.dialog;

import com.toedter.calendar.JCalendar;
import com.toedter.calendar.JDateChooser;
import connection.MySQL;
import java.sql.ResultSet;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.sql.SQLException;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.SwingUtilities;
import util.OptionPane;
import util.RoundedUtil;
import static validation.Regex.Validation.MOBILE;
import java.util.logging.Logger;
import java.util.logging.Level;
import util.LoggerUtil;

/**
 *
 * @author mgssr
 */
public class ManageMemberDialog extends javax.swing.JDialog {

    private static JDateChooser dateChooser;
    private static JFormattedTextField dateChooserTextField;
    
    private static final Logger logger = LoggerUtil.getLogger(ManageMemberDialog.class);

    /**
     * Creates new form ManageMemberDialog
     * @param parent
     * @param modal
     */
    public ManageMemberDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        security.Session.requireAdmin();
        initComponents();
        initCustomizations();
    }

    private void initCustomizations() {
        setPaddings();
        setBorderRadius();
        setDateChooser();
    }

    private void setPaddings() {
        int p1 = 20;
        addMemberPanel.setBorder(BorderFactory.createEmptyBorder(p1, p1, p1, p1));   //  top, left, bottom, right
        removeMemberPanel.setBorder(BorderFactory.createEmptyBorder(p1, p1, p1, p1));   //  top, left, bottom, right
        editMemberPanel.setBorder(BorderFactory.createEmptyBorder(p1, p1, p1, p1));   //  top, left, bottom, right

        idLabel_2.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));   //  top, left, bottom, right
        nameLabel_2.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));   //  top, left, bottom, right
        mobileLabel_2.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));   //  top, left, bottom, right
        dateLabel_2.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));   //  top, left, bottom, right
    }

    private void setBorderRadius() {
        int radius = 15;

//        addMemberPanel
        RoundedUtil.roundedCorners(nameField, radius);
        RoundedUtil.roundedCorners(mobileField, radius);
        RoundedUtil.roundedCorners(bodyfatField, radius);
        RoundedUtil.roundedCorners(heightField, radius);
        RoundedUtil.roundedCorners(weightField, radius);
        RoundedUtil.roundedCorners(bmiField, radius);
        RoundedUtil.roundedCorners(datePanel, radius);
        RoundedUtil.roundedCorners(jScrollPane1, radius);

//        removeMemberPanel
        RoundedUtil.roundedCorners(idField_2, radius);
        RoundedUtil.roundedCorners(mobileField_2, radius);
        RoundedUtil.roundedCorners(idLabel_2, radius);
        RoundedUtil.roundedCorners(nameLabel_2, radius);
        RoundedUtil.roundedCorners(mobileLabel_2, radius);
        RoundedUtil.roundedCorners(dateLabel_2, radius);

//        editMemberPanel
        RoundedUtil.roundedCorners(mobileField_3, radius);
    }

    private void setDateChooser() {
        Color foreground = new Color(221, 221, 221);
        Color background = new Color(40, 40, 40);
        Font font = new Font("Poppins", Font.PLAIN, 14);
        Font btnFont = new Font("Poppins SemiBold", Font.PLAIN, 14);

        dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("yyyy-MM-dd HH:mm:ss");

        JButton button = dateChooser.getCalendarButton();
        dateChooserTextField = (JFormattedTextField) dateChooser.getDateEditor().getUiComponent();
        JCalendar calendar = dateChooser.getJCalendar();

//        dateChooserButton
        button.setIcon(null);
        button.setText("Select");
        button.setFont(btnFont);
        button.setMargin(new Insets(0, 30, 0, 30));

//        dateChooserTextField
        dateChooserTextField.setFont(font);
        dateChooserTextField.setForeground(foreground);
        dateChooserTextField.setBackground(background);
        RoundedUtil.roundedCorners(dateChooserTextField, 15);

        dateChooserTextField.addPropertyChangeListener("foreground", (PropertyChangeEvent evt) -> {
            SwingUtilities.invokeLater(() -> dateChooserTextField.setForeground(new Color(221, 221, 221)));
        });

//        JCalendar
        calendar.setSundayForeground(Color.RED);
        calendar.setWeekdayForeground(foreground);
        calendar.setTodayButtonText("Today");
        calendar.setTodayButtonVisible(true);
        calendar.setDecorationBackgroundColor(background);
        calendar.setWeekOfYearVisible(false);
        calendar.setFont(font);
        calendar.getDayChooser().setForeground(foreground);
        calendar.getDayChooser().setBackground(background);
        calendar.getDayChooser().setFont(font);

        dateChooser.setDate(new Date());
        datePanel.add(dateChooser);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        radioGroup_2 = new javax.swing.ButtonGroup();
        manageMemberTab = new javax.swing.JTabbedPane();
        addMemberPanel = new javax.swing.JPanel();
        jPanel12 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        nameField = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        mobileField = new javax.swing.JFormattedTextField();
        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        datePanel = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        healthField = new javax.swing.JTextArea();
        jPanel5 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        membershipCombo = new javax.swing.JComboBox<>();
        jPanel6 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        bodyfatField = new javax.swing.JTextField();
        jPanel7 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        heightField = new javax.swing.JTextField();
        jPanel8 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        weightField = new javax.swing.JTextField();
        jPanel9 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        bmiField = new javax.swing.JTextField();
        jPanel13 = new javax.swing.JPanel();
        newMemberBtn = new javax.swing.JButton();
        removeMemberPanel = new javax.swing.JPanel();
        jPanel10 = new javax.swing.JPanel();
        idRadio_2 = new javax.swing.JRadioButton();
        idField_2 = new javax.swing.JTextField();
        mobileRadio_2 = new javax.swing.JRadioButton();
        mobileField_2 = new javax.swing.JTextField();
        jPanel15 = new javax.swing.JPanel();
        jPanel16 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        idLabel_2 = new javax.swing.JLabel();
        nameLabel_2 = new javax.swing.JLabel();
        mobileLabel_2 = new javax.swing.JLabel();
        dateLabel_2 = new javax.swing.JLabel();
        jPanel11 = new javax.swing.JPanel();
        removeMemberBtn = new javax.swing.JButton();
        editMemberPanel = new javax.swing.JPanel();
        jPanel14 = new javax.swing.JPanel();
        mobileField_3 = new javax.swing.JTextField();
        statusCombo_3 = new javax.swing.JComboBox<>();
        editMemberBtn = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
        });

        manageMemberTab.setFocusable(false);
        manageMemberTab.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        manageMemberTab.setOpaque(true);

        addMemberPanel.setPreferredSize(new java.awt.Dimension(600, 600));
        addMemberPanel.setLayout(new java.awt.BorderLayout(0, 20));

        jPanel12.setLayout(new java.awt.GridLayout(5, 2, 25, 0));

        jPanel1.setLayout(new java.awt.GridLayout(2, 1));

        jLabel1.setFont(new java.awt.Font("Poppins SemiBold", 0, 14)); // NOI18N
        jLabel1.setText("Name");
        jLabel1.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jLabel1.setPreferredSize(new java.awt.Dimension(350, 25));
        jPanel1.add(jLabel1);

        nameField.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        nameField.setMinimumSize(new java.awt.Dimension(200, 35));
        nameField.setPreferredSize(new java.awt.Dimension(350, 40));
        jPanel1.add(nameField);

        jPanel12.add(jPanel1);

        jPanel2.setLayout(new java.awt.GridLayout(2, 1));

        jLabel2.setFont(jLabel1.getFont());
        jLabel2.setText("Mobile");
        jLabel2.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jPanel2.add(jLabel2);

        try {
            mobileField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("### ### ####")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }
        mobileField.setText("");
        mobileField.setFont(nameField.getFont());
        jPanel2.add(mobileField);

        jPanel12.add(jPanel2);

        jPanel3.setLayout(new java.awt.GridLayout(2, 1));

        jLabel3.setFont(jLabel1.getFont());
        jLabel3.setText("Date");
        jLabel3.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jPanel3.add(jLabel3);

        datePanel.setBackground(new java.awt.Color(40, 40, 40));
        datePanel.setLayout(new java.awt.BorderLayout());
        jPanel3.add(datePanel);

        jPanel12.add(jPanel3);

        jPanel4.setLayout(new java.awt.BorderLayout());

        jLabel4.setFont(jLabel1.getFont());
        jLabel4.setText("Health Info");
        jPanel4.add(jLabel4, java.awt.BorderLayout.NORTH);

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setHorizontalScrollBar(null);

        healthField.setColumns(20);
        healthField.setFont(nameField.getFont());
        healthField.setLineWrap(true);
        healthField.setRows(5);
        jScrollPane1.setViewportView(healthField);

        jPanel4.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel12.add(jPanel4);

        jPanel5.setLayout(new java.awt.GridLayout(2, 1));

        jLabel5.setFont(jLabel1.getFont());
        jLabel5.setText("Membership Type");
        jLabel5.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jPanel5.add(jLabel5);

        membershipCombo.setBackground(new java.awt.Color(40, 40, 40));
        membershipCombo.setFont(nameField.getFont());
        membershipCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "DAYPASS", "MONTHLY", "ANNUAL" }));
        membershipCombo.setFocusable(false);
        membershipCombo.setName(""); // NOI18N
        membershipCombo.setOpaque(true);
        jPanel5.add(membershipCombo);

        jPanel12.add(jPanel5);

        jPanel6.setLayout(new java.awt.GridLayout(2, 1));

        jLabel6.setFont(jLabel1.getFont());
        jLabel6.setText("Body Fat (%)");
        jLabel6.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jPanel6.add(jLabel6);

        bodyfatField.setFont(nameField.getFont());
        bodyfatField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bodyfatFieldActionPerformed(evt);
            }
        });
        jPanel6.add(bodyfatField);

        jPanel12.add(jPanel6);

        jPanel7.setLayout(new java.awt.GridLayout(2, 1));

        jLabel7.setFont(jLabel1.getFont());
        jLabel7.setText("Height (cm)");
        jLabel7.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jPanel7.add(jLabel7);

        heightField.setFont(nameField.getFont());
        jPanel7.add(heightField);

        jPanel12.add(jPanel7);

        jPanel8.setLayout(new java.awt.GridLayout(2, 1));

        jLabel8.setFont(jLabel1.getFont());
        jLabel8.setText("Weight (kg)");
        jLabel8.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jPanel8.add(jLabel8);

        weightField.setFont(nameField.getFont());
        jPanel8.add(weightField);

        jPanel12.add(jPanel8);

        jPanel9.setLayout(new java.awt.GridLayout(2, 1));

        jLabel9.setFont(jLabel1.getFont());
        jLabel9.setText("BMI");
        jLabel9.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jPanel9.add(jLabel9);

        bmiField.setFont(nameField.getFont());
        jPanel9.add(bmiField);

        jPanel12.add(jPanel9);

        addMemberPanel.add(jPanel12, java.awt.BorderLayout.CENTER);

        jPanel13.setLayout(new java.awt.GridLayout(1, 1));

        newMemberBtn.setBackground(new java.awt.Color(221, 221, 221));
        newMemberBtn.setFont(new java.awt.Font("Poppins SemiBold", 0, 14)); // NOI18N
        newMemberBtn.setForeground(new java.awt.Color(40, 40, 40));
        newMemberBtn.setText("Confirm");
        newMemberBtn.setMaximumSize(new java.awt.Dimension(92, 55));
        newMemberBtn.setMinimumSize(new java.awt.Dimension(92, 55));
        newMemberBtn.setPreferredSize(new java.awt.Dimension(92, 55));
        newMemberBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newMemberBtnActionPerformed(evt);
            }
        });
        jPanel13.add(newMemberBtn);

        addMemberPanel.add(jPanel13, java.awt.BorderLayout.SOUTH);

        manageMemberTab.addTab("  Add New Meber  ", addMemberPanel);

        removeMemberPanel.setPreferredSize(new java.awt.Dimension(600, 600));
        removeMemberPanel.setLayout(new java.awt.BorderLayout(0, 20));

        jPanel10.setLayout(new java.awt.GridLayout(5, 2, 25, 15));

        radioGroup_2.add(idRadio_2);
        idRadio_2.setFont(new java.awt.Font("Poppins Medium", 0, 14)); // NOI18N
        idRadio_2.setSelected(true);
        idRadio_2.setText(" Select By ID: ");
        jPanel10.add(idRadio_2);

        idField_2.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        idField_2.setToolTipText("");
        idField_2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                idField_2KeyTyped(evt);
            }
        });
        jPanel10.add(idField_2);

        radioGroup_2.add(mobileRadio_2);
        mobileRadio_2.setFont(new java.awt.Font("Poppins Medium", 0, 14)); // NOI18N
        mobileRadio_2.setText(" Select By Mobile: ");
        jPanel10.add(mobileRadio_2);

        mobileField_2.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        mobileField_2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                mobileField_2KeyTyped(evt);
            }
        });
        jPanel10.add(mobileField_2);

        javax.swing.GroupLayout jPanel15Layout = new javax.swing.GroupLayout(jPanel15);
        jPanel15.setLayout(jPanel15Layout);
        jPanel15Layout.setHorizontalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 287, Short.MAX_VALUE)
        );
        jPanel15Layout.setVerticalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 102, Short.MAX_VALUE)
        );

        jPanel10.add(jPanel15);

        jButton1.setBackground(new java.awt.Color(221, 221, 221));
        jButton1.setFont(new java.awt.Font("Poppins SemiBold", 0, 14)); // NOI18N
        jButton1.setForeground(new java.awt.Color(40, 40, 40));
        jButton1.setText("Search Selection");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel16Layout = new javax.swing.GroupLayout(jPanel16);
        jPanel16.setLayout(jPanel16Layout);
        jPanel16Layout.setHorizontalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, 287, Short.MAX_VALUE)
        );
        jPanel16Layout.setVerticalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel16Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(51, Short.MAX_VALUE))
        );

        jPanel10.add(jPanel16);

        idLabel_2.setBackground(new java.awt.Color(40, 40, 40));
        idLabel_2.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        idLabel_2.setFocusable(false);
        idLabel_2.setOpaque(true);
        jPanel10.add(idLabel_2);

        nameLabel_2.setBackground(idLabel_2.getBackground());
        nameLabel_2.setFont(idLabel_2.getFont());
        nameLabel_2.setFocusable(false);
        nameLabel_2.setOpaque(true);
        jPanel10.add(nameLabel_2);

        mobileLabel_2.setBackground(idLabel_2.getBackground());
        mobileLabel_2.setFont(idLabel_2.getFont());
        mobileLabel_2.setFocusable(false);
        mobileLabel_2.setOpaque(true);
        jPanel10.add(mobileLabel_2);

        dateLabel_2.setBackground(idLabel_2.getBackground());
        dateLabel_2.setFont(idLabel_2.getFont());
        dateLabel_2.setFocusable(false);
        dateLabel_2.setOpaque(true);
        jPanel10.add(dateLabel_2);

        removeMemberPanel.add(jPanel10, java.awt.BorderLayout.CENTER);

        jPanel11.setLayout(new java.awt.GridLayout(1, 0));

        removeMemberBtn.setBackground(new java.awt.Color(221, 221, 221));
        removeMemberBtn.setFont(new java.awt.Font("Poppins SemiBold", 0, 14)); // NOI18N
        removeMemberBtn.setForeground(new java.awt.Color(40, 40, 40));
        removeMemberBtn.setText("Confirm");
        removeMemberBtn.setEnabled(false);
        removeMemberBtn.setMaximumSize(new java.awt.Dimension(92, 55));
        removeMemberBtn.setMinimumSize(new java.awt.Dimension(92, 55));
        removeMemberBtn.setPreferredSize(new java.awt.Dimension(92, 55));
        removeMemberBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeMemberBtnActionPerformed(evt);
            }
        });
        jPanel11.add(removeMemberBtn);

        removeMemberPanel.add(jPanel11, java.awt.BorderLayout.SOUTH);

        manageMemberTab.addTab("  Remove Member  ", removeMemberPanel);

        editMemberPanel.setPreferredSize(new java.awt.Dimension(600, 600));
        editMemberPanel.setLayout(new java.awt.BorderLayout(0, 20));

        mobileField_3.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        mobileField_3.setText("Enter Mobile");
        mobileField_3.setPreferredSize(new java.awt.Dimension(77, 35));

        statusCombo_3.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        statusCombo_3.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Set Active", "Set Inactive" }));
        statusCombo_3.setPreferredSize(new java.awt.Dimension(76, 35));

        javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
        jPanel14.setLayout(jPanel14Layout);
        jPanel14Layout.setHorizontalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(mobileField_3, javax.swing.GroupLayout.DEFAULT_SIZE, 377, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusCombo_3, 0, 205, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel14Layout.setVerticalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addGap(103, 103, 103)
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(mobileField_3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(statusCombo_3, javax.swing.GroupLayout.DEFAULT_SIZE, 55, Short.MAX_VALUE))
                .addContainerGap(412, Short.MAX_VALUE))
        );

        editMemberPanel.add(jPanel14, java.awt.BorderLayout.CENTER);

        editMemberBtn.setBackground(new java.awt.Color(221, 221, 221));
        editMemberBtn.setFont(new java.awt.Font("Poppins SemiBold", 0, 14)); // NOI18N
        editMemberBtn.setForeground(new java.awt.Color(40, 40, 40));
        editMemberBtn.setText("Confirm");
        editMemberBtn.setMaximumSize(new java.awt.Dimension(92, 55));
        editMemberBtn.setMinimumSize(new java.awt.Dimension(92, 55));
        editMemberBtn.setPreferredSize(new java.awt.Dimension(92, 55));
        editMemberBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editMemberBtnActionPerformed(evt);
            }
        });
        editMemberPanel.add(editMemberBtn, java.awt.BorderLayout.SOUTH);

        manageMemberTab.addTab("  Edit Member Details  ", editMemberPanel);

        getContentPane().add(manageMemberTab, java.awt.BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void newMemberBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newMemberBtnActionPerformed
        security.Session.requireAdmin();
        addNewMember();
    }//GEN-LAST:event_newMemberBtnActionPerformed

    private void bodyfatFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bodyfatFieldActionPerformed
        security.Session.requireAdmin();
        // TODO add your handling code here:
    }//GEN-LAST:event_bodyfatFieldActionPerformed

    private void removeMemberBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeMemberBtnActionPerformed
        security.Session.requireAdmin();
        removeMember();
    }//GEN-LAST:event_removeMemberBtnActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        security.Session.requireAdmin();
        searchRemoveMember(getRemoveMemberData());
    }//GEN-LAST:event_jButton1ActionPerformed

    private void idField_2KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_idField_2KeyTyped
        removeMemberBtn.setEnabled(false);
    }//GEN-LAST:event_idField_2KeyTyped

    private void editMemberBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editMemberBtnActionPerformed
        security.Session.requireAdmin();
        editMember();
    }//GEN-LAST:event_editMemberBtnActionPerformed

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        // TODO add your handling code here:
    }//GEN-LAST:event_formWindowClosed

    private void mobileField_2KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_mobileField_2KeyTyped
        removeMemberBtn.setEnabled(false);
    }//GEN-LAST:event_mobileField_2KeyTyped

    private void addNewMember() {
        getNewMemberData();
    }

    private void getNewMemberData() {
        String name = nameField.getText();
        String mobile = mobileField.getText().replaceAll("[^0-9]", "");
        String joinedDate = dateChooserTextField.getText();
        String healthInfo = healthField.getText();
        String membershipType = String.valueOf(membershipCombo.getSelectedIndex() + 1);
        String bodyFat = bodyfatField.getText();
        String height = heightField.getText();
        String weight = weightField.getText();
        String bmi = bmiField.getText();

        String memberData[] = {name, mobile, joinedDate, healthInfo, membershipType, height, weight, bodyFat, bmi};

        for (int i = 0; i < memberData.length; i++) {
            if (memberData[i].isBlank()) {
                memberData[i] = null;
            }
        }

        if (name.isBlank()) {
            OptionPane.showMessage(this, "Warning Message", "Name And Mobile Sections Cant Be Blank!   ", "WARNING");
        } else if (!mobile.matches(MOBILE.validate())) {
            OptionPane.showMessage(this, "Warning Message", "Mobile Number Does Not Appear To Be Valid!   ", "WARNING");
        } else if (joinedDate.isBlank()) {
            OptionPane.showMessage(this, "Warning Message", "Select a joining date.    ", "WARNING");
        } else {
            executeAddMemberQuery(memberData);
        }
    }

    private void executeAddMemberQuery(String... data) {
        java.sql.Connection conn = MySQL.getConnection();
        boolean success = false;
        try {
            Object[] measurements = new Object[4];
            for (int i = 0; i < 4; i++) {
                measurements[i] = data[i + 5] == null ? null : new java.math.BigDecimal(data[i + 5]);
                if (measurements[i] != null && ((java.math.BigDecimal) measurements[i]).signum() < 0)
                    throw new NumberFormatException("Negative measurement");
            }
            java.sql.Date joined = java.sql.Date.valueOf(data[2]);
            conn.setAutoCommit(false);
            boolean member = MySQL.executeIUD("INSERT INTO member (name,mobile,joined_date,health_info,"
                    + "membership_type_membership_type_id,payment_status,status_status_id) VALUES (?,?,?,?,?,0,1)",
                    data[0], data[1], joined, data[3], data[4]);
            boolean progress = member && MySQL.executeIUD("INSERT INTO progress_track "
                    + "(height_cm,weight_kg,body_fat,bmi,member_member_id) "
                    + "VALUES (?,?,?,?,(SELECT member_id FROM member WHERE mobile=?))",
                    measurements[0], measurements[1], measurements[2], measurements[3], data[1]);
            if (progress) { conn.commit(); success = true; } else conn.rollback();
        } catch (IllegalArgumentException e) {
            OptionPane.showMessage(this, "Warning", "Use a valid joining date and numeric measurements.    ", "WARNING");
            return;
        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ignored) { }
            logger.log(Level.SEVERE, "Adding member failed", e);
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException ignored) { }
        }
        OptionPane.showMessage(this, success ? "Message" : "Error",
                success ? "Member Added Successfully    " : "Member Already Exists or Invalid Data!    ",
                success ? "INFORMATION" : "ERROR");
    }

    private void removeMember() {
        executeRemoveMemberQuery();
    }

    private void executeRemoveMemberQuery() {
        String searchedID = idLabel_2.getText();

        String query = "DELETE FROM `gms`.`member` WHERE `member_id` = ?;";

        if (!searchedID.isBlank()) {
            boolean executeQuery = MySQL.executeIUD(query, searchedID);
            if (executeQuery && MySQL.getAffectedRows() != 0) {
                OptionPane.showMessage(this, "Message", "Member Records Successfully Deleted!    ", "INFORMATION");
            } else {
                OptionPane.showMessage(this, "Error Message", "Already Deleted Or Invalid ID!    ", "ERROR");
            }
        } else {
            OptionPane.showMessage(this, "Error Message", "Invalid Selection!    ", "ERROR");
        }
    }

    private String getRemoveMemberData() {
        ButtonModel selectedRadio = radioGroup_2.getSelection();

        String selectedID = idField_2.getText().trim();
        String selectedMobile = mobileField_2.getText().trim();

        if (selectedRadio == idRadio_2.getModel() && !selectedID.isBlank()) {
            return "`member_id` = " + selectedID;
        } else if (selectedRadio == mobileRadio_2.getModel() && !selectedMobile.isBlank()) {
            return "`mobile` = " + selectedMobile;
        }
        return null;
    }

    private void searchRemoveMember(String location) {
        String query = "SELECT `member_id`, `name`, `mobile`, `joined_date` FROM `member` WHERE " + location + ";";

        String id;
        String name;
        String mobile;
        String date;

        try {
            ResultSet rs = MySQL.executeSearch(query);

            if (rs.next()) {
                id = rs.getString("member_id");
                name = rs.getString("name");
                mobile = rs.getString("mobile");
                date = rs.getString("joined_date");

                idLabel_2.setText(id);
                nameLabel_2.setText(name);
                mobileLabel_2.setText(mobile);
                dateLabel_2.setText(date);

                removeMemberBtn.setEnabled(true);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "ManageMemberDialog.searchRemoveMember(){} caused an error!", e);
        }
    }

    private void editMember() {
        getEditMemberData();
    }

    private void getEditMemberData() {
        int status = statusCombo_3.getSelectedIndex();
        executeEditMemberQuery(status + 1);
    }

    private void executeEditMemberQuery(int status) {
        String query = "UPDATE `gms`.`member` SET `status_status_id`=? WHERE `mobile`=?;";

        boolean executeQuery = MySQL.executeIUD(query, status, mobileField_3.getText().trim());
        if (executeQuery && MySQL.getAffectedRows() != 0) {
            OptionPane.showMessage(this, "Message", "Member Records Successfully Updated!    ", "INFORMATION");
        } else {
            OptionPane.showMessage(this, "Error Message", "Already Updated Or Invalid Number!    ", "ERROR");
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(() -> {
            ManageMemberDialog dialog = new ManageMemberDialog(new javax.swing.JFrame(), true);
            dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    System.exit(0);
                }
            });
            dialog.setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel addMemberPanel;
    private javax.swing.JTextField bmiField;
    private javax.swing.JTextField bodyfatField;
    private javax.swing.JLabel dateLabel_2;
    private javax.swing.JPanel datePanel;
    private javax.swing.JButton editMemberBtn;
    private javax.swing.JPanel editMemberPanel;
    private javax.swing.JTextArea healthField;
    private javax.swing.JTextField heightField;
    private javax.swing.JTextField idField_2;
    private javax.swing.JLabel idLabel_2;
    private javax.swing.JRadioButton idRadio_2;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane manageMemberTab;
    private javax.swing.JComboBox<String> membershipCombo;
    private javax.swing.JFormattedTextField mobileField;
    private javax.swing.JTextField mobileField_2;
    private javax.swing.JTextField mobileField_3;
    private javax.swing.JLabel mobileLabel_2;
    private javax.swing.JRadioButton mobileRadio_2;
    private javax.swing.JTextField nameField;
    private javax.swing.JLabel nameLabel_2;
    private javax.swing.JButton newMemberBtn;
    private javax.swing.ButtonGroup radioGroup_2;
    private javax.swing.JButton removeMemberBtn;
    private javax.swing.JPanel removeMemberPanel;
    private javax.swing.JComboBox<String> statusCombo_3;
    private javax.swing.JTextField weightField;
    // End of variables declaration//GEN-END:variables
}
