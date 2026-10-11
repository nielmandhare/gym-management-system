package panels;

import connection.MySQL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.table.DefaultTableModel;
import panels.dialog.ManageMemberDialog;
import util.RoundedUtil;
import util.SearchSuggestions;
import java.util.logging.Logger;
import java.util.logging.Level;
import util.LoggerUtil;

/**
 *
 * @author mgssr
 */
public class MembersPanel extends javax.swing.JPanel {

    public static String totalMemberCount;
    public static String activeMemberCount;
    public static String InactiveMemberCount;
    public static String annualMembershipCount;
    public static String monthlyMembershipCount;
    public static String dayPassCount;
    public static String duePaymentCount;

    private static final Logger logger = LoggerUtil.getLogger(MembersPanel.class);

    /**
     * Creates new form MembersPanel
     */
    public MembersPanel() {
        security.Session.requireAdmin();
        initComponents();
        initCustomizations();
        javax.swing.JButton membershipButton=new javax.swing.JButton("Membership");
        jPanel2.add(membershipButton);
        membershipButton.addActionListener(e->manageMembership());
        SearchSuggestions.attach(searchTextField, searchBtn,
                "SELECT name FROM member WHERE name LIKE ? ORDER BY name LIMIT 8");
        initPanelData();
    }

    private void initCustomizations() {
        setPaddings();
        setBorderRadius();
    }

    private void setPaddings() {
        statsPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
    }

    private void setBorderRadius() {
        int radius = 15;

        RoundedUtil.roundedCorners(searchTextField, radius);
        RoundedUtil.roundedCorners(statsPanel, radius);
        RoundedUtil.roundedCorners(tableScrollPane, radius);
    }

//    ----------------------------------
    private void initPanelData() {
        setTableData();
        setStatsPanelData();
    }

    private void setTableData() {
        String query = "SELECT * FROM member "
                + "LEFT JOIN "
                + "membership ON membership_type_membership_type_id = membership_type_id "
                + "LEFT JOIN "
                + "status ON status_status_id = status_id;";

        try {
            DefaultTableModel dtm = (DefaultTableModel) membersTable.getModel();
            dtm.setRowCount(0);
            ResultSet rs = MySQL.executeSearch(query);

            while (rs.next()) {
                Vector<String> dataRow = new Vector();
                dataRow.add(rs.getString("member_id"));
                dataRow.add(rs.getString("name"));
                dataRow.add(rs.getString("mobile"));
                dataRow.add(rs.getString("joined_date"));
                dataRow.add(rs.getString("membership_type"));
                dataRow.add(rs.getBoolean("payment_status") ? "PAID" : "PENDING");
                dataRow.add(rs.getBoolean("status_status") ? "ACTIVE" : "INACTIVE");

                dtm.addRow(dataRow);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "MembersPanel.setTableData(){} caused an error!", e);
        }
    }

    private void setStatsPanelData() {
        String query = "SELECT "
                + "COUNT(member_id) AS total_members, "
                + "SUM(CASE WHEN status_status = 1 THEN 1 ELSE 0 END) AS active_members, "
                + "SUM(CASE WHEN status_status = 0 THEN 1 ELSE 0 END) AS inactive_members, "
                + "SUM(CASE WHEN membership_type = 'ANNUAL' THEN 1 ELSE 0 END) AS annual, "
                + "SUM(CASE WHEN membership_type = 'MONTHLY' THEN 1 ELSE 0 END) AS monthly, "
                + "SUM(CASE WHEN membership_type = 'DAYPASS' THEN 1 ELSE 0 END) AS daypass, "
                + "SUM(CASE WHEN payment_status = 0 THEN 1 ELSE 0 END) AS due_payments "
                + "FROM "
                + "member "
                + "LEFT JOIN "
                + "membership ON membership_type_membership_type_id = membership_type_id "
                + "LEFT JOIN "
                + "status ON status_status_id = status_id;";

        try {
            ResultSet rs = MySQL.executeSearch(query);

            if (rs.next()) {
                totalMemberCount = String.format("%02d", rs.getInt("total_members"));
                activeMemberCount = String.format("%02d", rs.getInt("active_members"));
                InactiveMemberCount = String.format("%02d", rs.getInt("inactive_members"));
                annualMembershipCount = String.format("%02d", rs.getInt("annual"));
                monthlyMembershipCount = String.format("%02d", rs.getInt("monthly"));
                dayPassCount = String.format("%02d", rs.getInt("daypass"));
                duePaymentCount = String.format("%02d", rs.getInt("due_payments"));
            }
            totalLabel.setText("Total Members:  " + totalMemberCount);
            activeLabel.setText("Active Members:  " + activeMemberCount);
            inactiveLabel.setText("Inactive Members:  " + InactiveMemberCount);
            annualLabel.setText("Annual Memberships:  " + annualMembershipCount);
            monthlyLabel.setText("Monthly Memberships:  " + monthlyMembershipCount);
            dailyLabel.setText("Day Passes:  " + dayPassCount);
            paymentDueLabel.setText("No of Payments Due:  " + duePaymentCount);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "MembersPanel.setStatsPanelData(){} caused an error!", e);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        topPanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        searchTextField = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        searchBtn = new javax.swing.JButton();
        manageBtn = new javax.swing.JButton();
        statsPanel = new javax.swing.JPanel();
        totalLabel = new javax.swing.JLabel();
        monthlyLabel = new javax.swing.JLabel();
        activeLabel = new javax.swing.JLabel();
        dailyLabel = new javax.swing.JLabel();
        inactiveLabel = new javax.swing.JLabel();
        paymentDueLabel = new javax.swing.JLabel();
        annualLabel = new javax.swing.JLabel();
        tablePanel = new javax.swing.JPanel();
        tableScrollPane = new javax.swing.JScrollPane();
        membersTable = new javax.swing.JTable();

        setPreferredSize(new java.awt.Dimension(780, 545));

        topPanel.setBackground(new java.awt.Color(153, 255, 255));
        topPanel.setOpaque(false);
        topPanel.setLayout(new javax.swing.BoxLayout(topPanel, javax.swing.BoxLayout.LINE_AXIS));

        jPanel1.setPreferredSize(new java.awt.Dimension(450, 45));
        jPanel1.setLayout(new java.awt.GridLayout(1, 1));

        searchTextField.setFont(new java.awt.Font("Poppins Light", 0, 14)); // NOI18N
        searchTextField.setForeground(new java.awt.Color(204, 204, 204));
        searchTextField.setToolTipText("");
        jPanel1.add(searchTextField);

        topPanel.add(jPanel1);

        jPanel2.setLayout(new java.awt.GridLayout(1, 0));

        searchBtn.setFont(new java.awt.Font("Poppins SemiBold", 0, 14)); // NOI18N
        searchBtn.setText("Search");
        searchBtn.setPreferredSize(new java.awt.Dimension(100, 35));
        searchBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchBtnActionPerformed(evt);
            }
        });
        jPanel2.add(searchBtn);

        manageBtn.setBackground(new java.awt.Color(255, 100, 0));
        manageBtn.setFont(new java.awt.Font("Poppins SemiBold", 0, 14)); // NOI18N
        manageBtn.setText("Manage");
        manageBtn.setPreferredSize(new java.awt.Dimension(100, 35));
        manageBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manageBtnActionPerformed(evt);
            }
        });
        jPanel2.add(manageBtn);

        topPanel.add(jPanel2);

        statsPanel.setBackground(new java.awt.Color(51, 51, 51));
        statsPanel.setLayout(new java.awt.GridLayout(4, 2, 0, 5));

        totalLabel.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        totalLabel.setText("Total Members:");
        statsPanel.add(totalLabel);

        monthlyLabel.setFont(totalLabel.getFont());
        monthlyLabel.setText("Monthly Memberships:");
        statsPanel.add(monthlyLabel);

        activeLabel.setFont(totalLabel.getFont());
        activeLabel.setText("Active Members:");
        statsPanel.add(activeLabel);

        dailyLabel.setFont(totalLabel.getFont());
        dailyLabel.setText("Daily Memberships:");
        statsPanel.add(dailyLabel);

        inactiveLabel.setFont(totalLabel.getFont());
        inactiveLabel.setText("Inactive Members:");
        statsPanel.add(inactiveLabel);

        paymentDueLabel.setFont(totalLabel.getFont());
        paymentDueLabel.setText("No of Payments Due:");
        statsPanel.add(paymentDueLabel);

        annualLabel.setFont(totalLabel.getFont());
        annualLabel.setText("Annual Memberships:");
        statsPanel.add(annualLabel);

        membersTable.setAutoCreateRowSorter(true);
        membersTable.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        membersTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "NAME", "MOBILE", "JOINED", "MEMBERSHIP", "PAYMENT", "STATUS"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        membersTable.setColumnSelectionAllowed(false);
        membersTable.setShowGrid(true);
        tableScrollPane.setViewportView(membersTable);

        javax.swing.GroupLayout tablePanelLayout = new javax.swing.GroupLayout(tablePanel);
        tablePanel.setLayout(tablePanelLayout);
        tablePanelLayout.setHorizontalGroup(
            tablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tableScrollPane)
        );
        tablePanelLayout.setVerticalGroup(
            tablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tablePanelLayout.createSequentialGroup()
                .addComponent(tableScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 361, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(topPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 774, Short.MAX_VALUE)
                    .addComponent(tablePanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(statsPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(topPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void manageMembership() {
        security.Session.requireAdmin();
        try {
            int row=membersTable.getSelectedRow();
            if(row<0)throw new IllegalArgumentException("Select a member first.");
            int memberId=Integer.parseInt(membersTable.getValueAt(row,0).toString());
            try(ResultSet data=MySQL.executeSearch("SELECT membership_type_membership_type_id,payment_status FROM member WHERE member_id=?",memberId)){
                if(!data.next())throw new IllegalArgumentException("Member no longer exists.");
                new panels.dialog.MembershipDialog(null,memberId,data.getInt(1),data.getBoolean(2)).setVisible(true);
            }
            refreshData();
        }catch(Exception e){javax.swing.JOptionPane.showMessageDialog(this,e.getMessage(),"Membership",javax.swing.JOptionPane.ERROR_MESSAGE);}
    }

    private void manageBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manageBtnActionPerformed
        security.Session.requireAdmin();
        ManageMemberDialog manageMemberDialog = new ManageMemberDialog(null, true);
        manageMemberDialog.setLocationRelativeTo(null);
        manageMemberDialog.setVisible(true);
        refreshData();
    }//GEN-LAST:event_manageBtnActionPerformed

    private void searchBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchBtnActionPerformed
        security.Session.requireAdmin();
        getSearchResult();
    }//GEN-LAST:event_searchBtnActionPerformed

    private void getSearchResult() {
        String searchText = searchTextField.getText();

        if (!searchText.isBlank()) {
            searchTableData("%" + searchText + "%");
        } else {
            setTableData();
        }
    }

    private void searchTableData(String searchText) {
        String query = "SELECT * FROM member "
                + "LEFT JOIN "
                + "membership ON membership_type_membership_type_id = membership_type_id "
                + "LEFT JOIN "
                + "status ON status_status_id = status_id "
                + "WHERE "
                + "name LIKE ? OR "
                + "mobile LIKE ? OR "
                + "member_id LIKE ? OR "
                + "membership_type LIKE ?;";

        try {
            DefaultTableModel dtm = (DefaultTableModel) membersTable.getModel();
            dtm.setRowCount(0);
            ResultSet rs = MySQL.executeSearch(query, searchText, searchText, searchText, searchText);

            while (rs.next()) {
                Vector<String> dataRow = new Vector();
                dataRow.add(rs.getString("member_id"));
                dataRow.add(rs.getString("name"));
                dataRow.add(rs.getString("mobile"));
                dataRow.add(rs.getString("joined_date"));
                dataRow.add(rs.getString("membership_type"));
                dataRow.add(rs.getBoolean("payment_status") ? "PAID" : "PENDING");
                dataRow.add(rs.getBoolean("status_status") ? "ACTIVE" : "INACTIVE");

                dtm.addRow(dataRow);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "MembersPanel.searchTableData(){} caused an error!", e);
        }
    }

    public void refreshData() {
        setTableData();
        setStatsPanelData();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel activeLabel;
    private javax.swing.JLabel annualLabel;
    private javax.swing.JLabel dailyLabel;
    private javax.swing.JLabel inactiveLabel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JButton manageBtn;
    private javax.swing.JTable membersTable;
    private javax.swing.JLabel monthlyLabel;
    private javax.swing.JLabel paymentDueLabel;
    private javax.swing.JButton searchBtn;
    private javax.swing.JTextField searchTextField;
    private javax.swing.JPanel statsPanel;
    private javax.swing.JPanel tablePanel;
    private javax.swing.JScrollPane tableScrollPane;
    private javax.swing.JPanel topPanel;
    private javax.swing.JLabel totalLabel;
    // End of variables declaration//GEN-END:variables
}

