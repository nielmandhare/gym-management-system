package panels.dialog;

import com.toedter.calendar.JCalendar;
import com.toedter.calendar.JDateChooser;
import connection.MySQL;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.sql.ResultSet;
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
import static validation.Regex.Validation.USERNAME;
import static validation.Regex.Validation.PASSWORD;
import static validation.Regex.Validation.NIC;
import java.util.logging.Logger;
import java.util.logging.Level;
import util.LoggerUtil;

/**
 *
 * @author mgssr
 */
public class ManageTrainerDialog extends javax.swing.JDialog {

    private static JDateChooser dateChooser;
    private static JFormattedTextField dateChooserTextField;
    
    private static final Logger logger = LoggerUtil.getLogger(ManageTrainerDialog.class);

    /**
     * Creates new form manageTrainerDialog
     * @param parent
     * @param modal
     */
    public ManageTrainerDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
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
        addTrainerPanel.setBorder(BorderFactory.createEmptyBorder(p1, p1, p1, p1));   //  top, left, bottom, right
        removeTrainerPanel.setBorder(BorderFactory.createEmptyBorder(p1, p1, p1, p1));   //  top, left, bottom, right
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
        RoundedUtil.roundedCorners(passwordField, radius);
        RoundedUtil.roundedCorners(usernameField, radius);
        RoundedUtil.roundedCorners(confirmPasswordField, radius);
        RoundedUtil.roundedCorners(datePanel, radius);
        RoundedUtil.roundedCorners(nicField, radius);

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
        dateChooser.setDateFormatString("yyyy-MM-dd");

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
        manageTrainerTab = new javax.swing.JTabbedPane();
        addTrainerPanel = new javax.swing.JPanel();
        jPanel19 = new javax.swing.JPanel();
        jPanel20 = new javax.swing.JPanel();
        jLabel16 = new javax.swing.JLabel();
        nameField = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        jPanel21 = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        mobileField = new javax.swing.JFormattedTextField();
        jPanel23 = new javax.swing.JPanel();
        jLabel19 = new javax.swing.JLabel();
        nicField = new javax.swing.JTextField();
        jPanel24 = new javax.swing.JPanel();
        jLabel20 = new javax.swing.JLabel();
        usernameField = new javax.swing.JTextField();
        jPanel22 = new javax.swing.JPanel();
        jLabel18 = new javax.swing.JLabel();
        datePanel = new javax.swing.JPanel();
        jPanel25 = new javax.swing.JPanel();
        jLabel21 = new javax.swing.JLabel();
        passwordField = new javax.swing.JTextField();
        jPanel26 = new javax.swing.JPanel();
        jLabel22 = new javax.swing.JLabel();
        confirmPasswordField = new javax.swing.JTextField();
        jPanel29 = new javax.swing.JPanel();
        newTrainerBtn = new javax.swing.JButton();
        removeTrainerPanel = new javax.swing.JPanel();
        jPanel30 = new javax.swing.JPanel();
        idRadio_2 = new javax.swing.JRadioButton();
        idField_2 = new javax.swing.JTextField();
        mobileRadio_2 = new javax.swing.JRadioButton();
        mobileField_2 = new javax.swing.JTextField();
        jPanel31 = new javax.swing.JPanel();
        jPanel32 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        idLabel_2 = new javax.swing.JLabel();
        nameLabel_2 = new javax.swing.JLabel();
        mobileLabel_2 = new javax.swing.JLabel();
        dateLabel_2 = new javax.swing.JLabel();
        jPanel33 = new javax.swing.JPanel();
        removeMemberBtn = new javax.swing.JButton();
        editMemberPanel = new javax.swing.JPanel();
        jPanel34 = new javax.swing.JPanel();
        mobileField_3 = new javax.swing.JTextField();
        statusCombo_3 = new javax.swing.JComboBox<>();
        editMemberBtn = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setPreferredSize(new java.awt.Dimension(600, 680));

        manageTrainerTab.setFocusable(false);
        manageTrainerTab.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        manageTrainerTab.setOpaque(true);

        addTrainerPanel.setPreferredSize(new java.awt.Dimension(600, 600));
        addTrainerPanel.setLayout(new java.awt.BorderLayout(0, 20));

        jPanel19.setLayout(new java.awt.GridLayout(5, 2, 25, 0));

        jPanel20.setLayout(new java.awt.GridLayout(2, 1));

        jLabel16.setFont(new java.awt.Font("Poppins SemiBold", 0, 14)); // NOI18N
        jLabel16.setText("Name");
        jLabel16.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jLabel16.setPreferredSize(new java.awt.Dimension(350, 25));
        jPanel20.add(jLabel16);

        nameField.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        nameField.setMinimumSize(new java.awt.Dimension(200, 35));
        nameField.setPreferredSize(new java.awt.Dimension(350, 40));
        jPanel20.add(nameField);

        jPanel19.add(jPanel20);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 287, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 114, Short.MAX_VALUE)
        );

        jPanel19.add(jPanel1);

        jPanel21.setLayout(new java.awt.GridLayout(2, 1));

        jLabel17.setFont(jLabel16.getFont());
        jLabel17.setText("Mobile");
        jLabel17.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jPanel21.add(jLabel17);

        try {
            mobileField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("### ### ####")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }
        mobileField.setText("");
        mobileField.setFont(nameField.getFont());
        jPanel21.add(mobileField);

        jPanel19.add(jPanel21);

        jPanel23.setLayout(new java.awt.GridLayout(2, 1));

        jLabel19.setFont(jLabel16.getFont());
        jLabel19.setText("NIC");
        jLabel19.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jPanel23.add(jLabel19);

        nicField.setFont(nameField.getFont());
        jPanel23.add(nicField);

        jPanel19.add(jPanel23);

        jPanel24.setLayout(new java.awt.GridLayout(2, 1));

        jLabel20.setFont(jLabel16.getFont());
        jLabel20.setText("Username");
        jLabel20.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jPanel24.add(jLabel20);

        usernameField.setFont(nameField.getFont());
        jPanel24.add(usernameField);

        jPanel19.add(jPanel24);

        jPanel22.setLayout(new java.awt.GridLayout(2, 1));

        jLabel18.setFont(jLabel16.getFont());
        jLabel18.setText("Date");
        jLabel18.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jPanel22.add(jLabel18);

        datePanel.setBackground(new java.awt.Color(40, 40, 40));
        datePanel.setLayout(new java.awt.BorderLayout());
        jPanel22.add(datePanel);

        jPanel19.add(jPanel22);

        jPanel25.setLayout(new java.awt.GridLayout(2, 1));

        jLabel21.setFont(jLabel16.getFont());
        jLabel21.setText("Password");
        jLabel21.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jPanel25.add(jLabel21);

        passwordField.setFont(nameField.getFont());
        passwordField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                passwordFieldActionPerformed(evt);
            }
        });
        jPanel25.add(passwordField);

        jPanel19.add(jPanel25);

        jPanel26.setLayout(new java.awt.GridLayout(2, 1));

        jLabel22.setFont(jLabel16.getFont());
        jLabel22.setText("Confirm Password");
        jLabel22.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jPanel26.add(jLabel22);

        confirmPasswordField.setFont(nameField.getFont());
        jPanel26.add(confirmPasswordField);

        jPanel19.add(jPanel26);

        addTrainerPanel.add(jPanel19, java.awt.BorderLayout.CENTER);

        jPanel29.setLayout(new java.awt.GridLayout(1, 1));

        newTrainerBtn.setBackground(new java.awt.Color(221, 221, 221));
        newTrainerBtn.setFont(new java.awt.Font("Poppins SemiBold", 0, 14)); // NOI18N
        newTrainerBtn.setForeground(new java.awt.Color(40, 40, 40));
        newTrainerBtn.setText("Confirm");
        newTrainerBtn.setMaximumSize(new java.awt.Dimension(92, 55));
        newTrainerBtn.setMinimumSize(new java.awt.Dimension(92, 55));
        newTrainerBtn.setPreferredSize(new java.awt.Dimension(92, 55));
        newTrainerBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newTrainerBtnActionPerformed(evt);
            }
        });
        jPanel29.add(newTrainerBtn);

        addTrainerPanel.add(jPanel29, java.awt.BorderLayout.SOUTH);

        manageTrainerTab.addTab("  Add New Trainer  ", addTrainerPanel);

        removeTrainerPanel.setPreferredSize(new java.awt.Dimension(600, 600));
        removeTrainerPanel.setLayout(new java.awt.BorderLayout(0, 20));

        jPanel30.setLayout(new java.awt.GridLayout(5, 2, 25, 15));

        radioGroup_2.add(idRadio_2);
        idRadio_2.setFont(new java.awt.Font("Poppins Medium", 0, 14)); // NOI18N
        idRadio_2.setSelected(true);
        idRadio_2.setText(" Select By ID: ");
        jPanel30.add(idRadio_2);

        idField_2.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        idField_2.setToolTipText("");
        idField_2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                idField_2KeyTyped(evt);
            }
        });
        jPanel30.add(idField_2);

        radioGroup_2.add(mobileRadio_2);
        mobileRadio_2.setFont(new java.awt.Font("Poppins Medium", 0, 14)); // NOI18N
        mobileRadio_2.setText(" Select By Mobile: ");
        jPanel30.add(mobileRadio_2);

        mobileField_2.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        mobileField_2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                mobileField_2KeyTyped(evt);
            }
        });
        jPanel30.add(mobileField_2);

        javax.swing.GroupLayout jPanel31Layout = new javax.swing.GroupLayout(jPanel31);
        jPanel31.setLayout(jPanel31Layout);
        jPanel31Layout.setHorizontalGroup(
            jPanel31Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 287, Short.MAX_VALUE)
        );
        jPanel31Layout.setVerticalGroup(
            jPanel31Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 102, Short.MAX_VALUE)
        );

        jPanel30.add(jPanel31);

        jButton1.setBackground(new java.awt.Color(221, 221, 221));
        jButton1.setFont(new java.awt.Font("Poppins SemiBold", 0, 14)); // NOI18N
        jButton1.setForeground(new java.awt.Color(40, 40, 40));
        jButton1.setText("Search Selection");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel32Layout = new javax.swing.GroupLayout(jPanel32);
        jPanel32.setLayout(jPanel32Layout);
        jPanel32Layout.setHorizontalGroup(
            jPanel32Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, 287, Short.MAX_VALUE)
        );
        jPanel32Layout.setVerticalGroup(
            jPanel32Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel32Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel30.add(jPanel32);

        idLabel_2.setBackground(new java.awt.Color(40, 40, 40));
        idLabel_2.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        idLabel_2.setFocusable(false);
        idLabel_2.setOpaque(true);
        jPanel30.add(idLabel_2);

        nameLabel_2.setBackground(new java.awt.Color(40, 40, 40));
        nameLabel_2.setFont(idLabel_2.getFont());
        nameLabel_2.setFocusable(false);
        nameLabel_2.setOpaque(true);
        jPanel30.add(nameLabel_2);

        mobileLabel_2.setBackground(new java.awt.Color(40, 40, 40));
        mobileLabel_2.setFont(idLabel_2.getFont());
        mobileLabel_2.setFocusable(false);
        mobileLabel_2.setOpaque(true);
        jPanel30.add(mobileLabel_2);

        dateLabel_2.setBackground(new java.awt.Color(40, 40, 40));
        dateLabel_2.setFont(idLabel_2.getFont());
        dateLabel_2.setFocusable(false);
        dateLabel_2.setOpaque(true);
        jPanel30.add(dateLabel_2);

        removeTrainerPanel.add(jPanel30, java.awt.BorderLayout.CENTER);

        jPanel33.setLayout(new java.awt.GridLayout(1, 0));

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
        jPanel33.add(removeMemberBtn);

        removeTrainerPanel.add(jPanel33, java.awt.BorderLayout.SOUTH);

        manageTrainerTab.addTab("  Remove Trainer  ", removeTrainerPanel);

        editMemberPanel.setPreferredSize(new java.awt.Dimension(600, 600));
        editMemberPanel.setLayout(new java.awt.BorderLayout(0, 20));

        mobileField_3.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        mobileField_3.setText("Enter Mobile");
        mobileField_3.setPreferredSize(new java.awt.Dimension(77, 35));

        statusCombo_3.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        statusCombo_3.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Set Active", "Set Inactive" }));
        statusCombo_3.setPreferredSize(new java.awt.Dimension(76, 35));

        javax.swing.GroupLayout jPanel34Layout = new javax.swing.GroupLayout(jPanel34);
        jPanel34.setLayout(jPanel34Layout);
        jPanel34Layout.setHorizontalGroup(
            jPanel34Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel34Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(mobileField_3, javax.swing.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusCombo_3, 0, 206, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel34Layout.setVerticalGroup(
            jPanel34Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel34Layout.createSequentialGroup()
                .addGap(103, 103, 103)
                .addGroup(jPanel34Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(mobileField_3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(statusCombo_3, javax.swing.GroupLayout.DEFAULT_SIZE, 55, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        editMemberPanel.add(jPanel34, java.awt.BorderLayout.CENTER);

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

        manageTrainerTab.addTab("  Edit Trainer Details  ", editMemberPanel);

        getContentPane().add(manageTrainerTab, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void passwordFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_passwordFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_passwordFieldActionPerformed

    private void newTrainerBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newTrainerBtnActionPerformed
        addNewTranier();
    }//GEN-LAST:event_newTrainerBtnActionPerformed

    private void idField_2KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_idField_2KeyTyped
        removeMemberBtn.setEnabled(false);
    }//GEN-LAST:event_idField_2KeyTyped

    private void mobileField_2KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_mobileField_2KeyTyped
        removeMemberBtn.setEnabled(false);
    }//GEN-LAST:event_mobileField_2KeyTyped

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        searchRemoveTrainer(getRemoveTrainerData());
    }//GEN-LAST:event_jButton1ActionPerformed

    private void removeMemberBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeMemberBtnActionPerformed
        removeTrainer();
    }//GEN-LAST:event_removeMemberBtnActionPerformed

    private void editMemberBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editMemberBtnActionPerformed
        editTrainer();
    }//GEN-LAST:event_editMemberBtnActionPerformed
    private void addNewTranier() {
        getNewTrainerData();
    }

    private void getNewTrainerData() {
        String name = nameField.getText();
        String mobile = mobileField.getText().replaceAll("[^0-9]", "");
        String hiredDate = dateChooserTextField.getText();
        String nic = nicField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        String trainerData[] = {name, mobile, nic, hiredDate, username, password, confirmPassword};

        for (String trainer : trainerData) {
            if (trainer.isBlank()) {
                OptionPane.showMessage(this, "Warning Message", "All Fields Are Required. Cannot Be Empty!   ", "WARNING");
                return;
            }
        }

        if (!mobile.matches(MOBILE.validate())) {
            OptionPane.showMessage(this, "Warning Message", "Mobile Number Does Not Appear To Be Valid!   ", "WARNING");
        } else if (!nic.matches(NIC.validate())) {
            OptionPane.showMessage(this, "Warning Message", "Invalid NIC!   ", "WARNING");
        } else if (!username.matches(USERNAME.validate())) {
            OptionPane.showMessage(this, "Warning Message", "Invalid Username. Only letters, digits, underscores, Or periods. Length Should Be Between 3-20 Characters!   ", "WARNING");
        } else if (!password.matches(PASSWORD.validate())) {
            OptionPane.showMessage(this, "Warning Message", "Enter A Stronger Password!   ", "WARNING");
        } else if (!confirmPassword.equals(password)) {
            OptionPane.showMessage(this, "Warning Message", "Passwords Does Not Match!   ", "WARNING");

        } else if (checkIfExist(mobile, nic, username)) {
            OptionPane.showMessage(this, "Warning Message", "Mobile, NIC Or Username Already Exist!   ", "WARNING");
        } else {
            executeAddTrainerQuery(trainerData);
        }
    }

    private boolean checkIfExist(String... data) {
        String query = "SELECT * FROM trainer "
                + "WHERE "
                + "mobile = ? OR nic = ? OR username = ?;";

        try {
            try (ResultSet rs = MySQL.executeSearch(query, data[0], data[1], data[2])) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "ManageTrainerDialog.checkIfExist(){} caused an error!", e);
            return true;
        }
    }

    private void executeAddTrainerQuery(String... data) {
        String query = "INSERT INTO trainer (name,mobile,nic,hired_date,username,password,status_status_id) "
                + "VALUES (?,?,?,?,?,?,1)";
        boolean executeQuery = MySQL.executeIUD(query, data[0], data[1], data[2], data[3], data[4], data[5]);

        if (executeQuery) {
            OptionPane.showMessage(this, "Message", "Trainer Added Successfully    ", "INFORMATION");
        } else {
            OptionPane.showMessage(this, "Error", "Database Error. Try Again!    ", "ERROR");
        }
    }

    private void removeTrainer() {
        executeRemoveTrainerQuery();
    }

    private void executeRemoveTrainerQuery() {
        String searchedID = idLabel_2.getText();

        String query = "DELETE FROM `gms`.`trainer` WHERE `trainer_id` = ?;";

        if (!searchedID.isBlank()) {
            boolean executeQuery = MySQL.executeIUD(query, searchedID);
            if (executeQuery && MySQL.getAffectedRows() != 0) {
                OptionPane.showMessage(this, "Message", "Trainer Records Successfully Deleted!    ", "INFORMATION");
            } else {
                OptionPane.showMessage(this, "Error Message", "Already Deleted Or Invalid ID!    ", "ERROR");
            }
        } else {
            OptionPane.showMessage(this, "Error Message", "Invalid Selection!    ", "ERROR");
        }
    }

    private String getRemoveTrainerData() {
        ButtonModel selectedRadio = radioGroup_2.getSelection();

        String selectedID = idField_2.getText().trim();
        String selectedMobile = mobileField_2.getText().trim();

        if (selectedRadio == idRadio_2.getModel() && !selectedID.isBlank()) {
            return "`trainer_id` = " + selectedID;
        } else if (selectedRadio == mobileRadio_2.getModel() && !selectedMobile.isBlank()) {
            return "`mobile` = " + selectedMobile;
        }
        return null;
    }

    private void searchRemoveTrainer(String location) {
        String query = "SELECT `trainer_id`, `name`, `mobile`, `username` FROM `trainer` WHERE " + location + ";";

        String id;
        String name;
        String mobile;
        String username;

        try {
            ResultSet rs = MySQL.executeSearch(query);

            if (rs.next()) {
                id = rs.getString("trainer_id");
                name = rs.getString("name");
                mobile = rs.getString("mobile");
                username = rs.getString("username");

                idLabel_2.setText(id);
                nameLabel_2.setText(name);
                mobileLabel_2.setText(mobile);
                dateLabel_2.setText(username);

                removeMemberBtn.setEnabled(true);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "ManageTrainerDialog.checkIfExist(){} caused an error!", e);
        }
    }

    private void editTrainer() {
        getEditTrainerData();
    }

    private void getEditTrainerData() {
        int status = statusCombo_3.getSelectedIndex();
        executeEditTrainerQuery(status + 1);
    }

    private void executeEditTrainerQuery(int status) {
        String query = "UPDATE `gms`.`trainer` SET `status_status_id`=? WHERE `mobile`=?;";

        boolean executeQuery = MySQL.executeIUD(query, status, mobileField_3.getText().trim());
        if (executeQuery && MySQL.getAffectedRows() != 0) {
            OptionPane.showMessage(this, "Message", "Trainer Records Successfully Updated!    ", "INFORMATION");
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
            ManageTrainerDialog dialog = new ManageTrainerDialog(new javax.swing.JFrame(), true);
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
    private javax.swing.JPanel addTrainerPanel;
    private javax.swing.JTextField confirmPasswordField;
    private javax.swing.JLabel dateLabel_2;
    private javax.swing.JPanel datePanel;
    private javax.swing.JButton editMemberBtn;
    private javax.swing.JPanel editMemberPanel;
    private javax.swing.JTextField idField_2;
    private javax.swing.JLabel idLabel_2;
    private javax.swing.JRadioButton idRadio_2;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel19;
    private javax.swing.JPanel jPanel20;
    private javax.swing.JPanel jPanel21;
    private javax.swing.JPanel jPanel22;
    private javax.swing.JPanel jPanel23;
    private javax.swing.JPanel jPanel24;
    private javax.swing.JPanel jPanel25;
    private javax.swing.JPanel jPanel26;
    private javax.swing.JPanel jPanel29;
    private javax.swing.JPanel jPanel30;
    private javax.swing.JPanel jPanel31;
    private javax.swing.JPanel jPanel32;
    private javax.swing.JPanel jPanel33;
    private javax.swing.JPanel jPanel34;
    private javax.swing.JTabbedPane manageTrainerTab;
    private javax.swing.JFormattedTextField mobileField;
    private javax.swing.JTextField mobileField_2;
    private javax.swing.JTextField mobileField_3;
    private javax.swing.JLabel mobileLabel_2;
    private javax.swing.JRadioButton mobileRadio_2;
    private javax.swing.JTextField nameField;
    private javax.swing.JLabel nameLabel_2;
    private javax.swing.JButton newTrainerBtn;
    private javax.swing.JTextField nicField;
    private javax.swing.JTextField passwordField;
    private javax.swing.ButtonGroup radioGroup_2;
    private javax.swing.JButton removeMemberBtn;
    private javax.swing.JPanel removeTrainerPanel;
    private javax.swing.JComboBox<String> statusCombo_3;
    private javax.swing.JTextField usernameField;
    // End of variables declaration//GEN-END:variables
}
