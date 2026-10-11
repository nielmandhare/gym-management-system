package panels;

import connection.MySQL;
import java.awt.event.ActionEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import util.RoundedUtil;
import util.SearchSuggestions;
import util.TimeUtil;
import java.util.logging.Logger;
import java.util.logging.Level;
import util.LoggerUtil;

/**
 *
 * @author mgssr
 */
public class AttendancePanel extends javax.swing.JPanel {

    public static String dailyLogginsCount;
    public static String monthlyLogginCount;
    public static String bussiestDay;
    public static String avgLogginTime;
    public static String date;
    public static String time;

    public static DefaultTableModel dtm;

    private static final Logger logger = LoggerUtil.getLogger(AttendancePanel.class);

    /**
     * Creates new form AttendancePanel
     */
    public AttendancePanel() {
        security.Session.requireAdmin();
        initComponents();
        initCustomizations();
        SearchSuggestions.attach(searchTextField, searchBtn,
                "SELECT DISTINCT m.name FROM member m JOIN member_attendance a ON a.member_member_id=m.member_id WHERE m.name LIKE ? ORDER BY m.name LIMIT 8");
        initPanelData();
        setDateTime();
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

//    ------------------------
    private void initPanelData() {
        setTableData();
        setStatsPanelData();
    }

    private void setDateTime() {
        date = TimeUtil.getCurrentDate();
        time = TimeUtil.getCurrentTime();

        dateLabel.setText("Date:  " + date);
        Timer timer = new Timer(1000, (ActionEvent e) -> {
            time = TimeUtil.getCurrentTime();
            timeLabel.setText("Time:  " + time);
        });
        timer.start();
    }

    private void setTableData() {
        String query = "SELECT * "
                + "FROM member_attendance "
                + "LEFT JOIN "
                + "member ON member_member_id = member_id "
                + "LEFT JOIN "
                + "membership ON membership_type_membership_type_id = membership_type_id "
                + "ORDER BY date DESC, check_in_time DESC;";

        try {
            dtm = (DefaultTableModel) membersTable.getModel();
            dtm.setRowCount(0);
            ResultSet rs = MySQL.executeSearch(query);

            while (rs.next()) {
                Vector<String> dataRow = new Vector();
                dataRow.add(rs.getString("name"));
                dataRow.add(rs.getString("date"));
                dataRow.add(rs.getString("check_in_time"));
                dataRow.add(rs.getBoolean("payment_status") ? "PAID" : "PENDING");
                dataRow.add(rs.getString("membership_type"));

                dtm.addRow(dataRow);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "AttendancePanel.setTableData(){} caused an error!", e);
        }
    }

    private void setStatsPanelData() {
        String query = "SELECT "
                + "(SELECT COUNT(*) FROM member_attendance WHERE date = CURRENT_DATE()) AS daily_loggins, "
                + "(SELECT COUNT(*) FROM member_attendance WHERE date >= DATE_SUB(CURRENT_DATE(), INTERVAL DAYOFMONTH(CURRENT_DATE()) - 1 DAY)) AS monthly_loggins, "
                + "(SELECT date FROM member_attendance GROUP BY date ORDER BY COUNT(*) DESC LIMIT 1) AS busiest_date, "
                + "(SELECT SEC_TO_TIME(AVG(TIME_TO_SEC(check_in_time))) FROM member_attendance) AS average_logging_time;";

        try {
            ResultSet rs = MySQL.executeSearch(query);

            if (rs.next()) {
                dailyLogginsCount = String.format("%02d", rs.getInt("daily_loggins"));
                monthlyLogginCount = String.format("%02d", rs.getInt("monthly_loggins"));
                bussiestDay = rs.getString("busiest_date");
                avgLogginTime = rs.getString("average_logging_time");
            }
            dailyLogginsLabel.setText("Daily Loggins:  " + dailyLogginsCount);
            monthlyLogginsLabel.setText("Monthly Loggins:  " + monthlyLogginCount);
            busiestDateLabel.setText("Busiest Date:  " + bussiestDay);
            avgTimeLabel.setText("Average Logging Time:  " + avgLogginTime);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "AttendancePanel.searchTableData(){} caused an error!", e);
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
        statsPanel = new javax.swing.JPanel();
        dailyLogginsLabel = new javax.swing.JLabel();
        dateLabel = new javax.swing.JLabel();
        monthlyLogginsLabel = new javax.swing.JLabel();
        timeLabel = new javax.swing.JLabel();
        busiestDateLabel = new javax.swing.JLabel();
        psudoLabbel = new javax.swing.JLabel();
        avgTimeLabel = new javax.swing.JLabel();
        tablePanel = new javax.swing.JPanel();
        tableScrollPane = new javax.swing.JScrollPane();
        membersTable = new javax.swing.JTable();

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

        jPanel2.setPreferredSize(new java.awt.Dimension(80, 35));
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

        topPanel.add(jPanel2);

        statsPanel.setBackground(new java.awt.Color(51, 51, 51));
        statsPanel.setLayout(new java.awt.GridLayout(4, 2, 0, 5));

        dailyLogginsLabel.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        dailyLogginsLabel.setText("Daily Loggins:");
        statsPanel.add(dailyLogginsLabel);

        dateLabel.setFont(dailyLogginsLabel.getFont());
        dateLabel.setText("Date:");
        statsPanel.add(dateLabel);

        monthlyLogginsLabel.setFont(dailyLogginsLabel.getFont());
        monthlyLogginsLabel.setText("Monthly Loggins:");
        statsPanel.add(monthlyLogginsLabel);

        timeLabel.setFont(dailyLogginsLabel.getFont());
        timeLabel.setText("Time:");
        statsPanel.add(timeLabel);

        busiestDateLabel.setFont(dailyLogginsLabel.getFont());
        busiestDateLabel.setText("Busiest Date:");
        statsPanel.add(busiestDateLabel);

        psudoLabbel.setFont(dailyLogginsLabel.getFont());
        statsPanel.add(psudoLabbel);

        avgTimeLabel.setFont(dailyLogginsLabel.getFont());
        avgTimeLabel.setText("Average Logging Time:");
        statsPanel.add(avgTimeLabel);

        membersTable.setAutoCreateRowSorter(true);
        membersTable.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        membersTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "NAME", "DATE", "TIME", "PAYMENT", "MEMBERSHIP"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

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
            .addComponent(tableScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 774, Short.MAX_VALUE)
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
        String query = "SELECT * "
                + "FROM member_attendance "
                + "LEFT JOIN "
                + "member ON member_member_id = member_id "
                + "LEFT JOIN "
                + "membership ON membership_type_membership_type_id = membership_type_id "
                + "WHERE "
                + "name LIKE ? OR "
                + "attendance_id LIKE ? OR "
                + "membership_type LIKE ?;";

        try {
            dtm = (DefaultTableModel) membersTable.getModel();
            dtm.setRowCount(0);
            ResultSet rs = MySQL.executeSearch(query, searchText, searchText, searchText);

            while (rs.next()) {
                Vector<String> dataRow = new Vector();
                dataRow.add(rs.getString("name"));
                dataRow.add(rs.getString("date"));
                dataRow.add(rs.getString("check_in_time"));
                dataRow.add(rs.getBoolean("payment_status") ? "PAID" : "PENDING");
                dataRow.add(rs.getString("membership_type"));

                dtm.addRow(dataRow);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "AttendancePanel.searchTableData(){} caused an error!", e);
        }
    }

    public void refreshData() {
        setTableData();
        setStatsPanelData();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel avgTimeLabel;
    private javax.swing.JLabel busiestDateLabel;
    private javax.swing.JLabel dailyLogginsLabel;
    private javax.swing.JLabel dateLabel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JTable membersTable;
    private javax.swing.JLabel monthlyLogginsLabel;
    private javax.swing.JLabel psudoLabbel;
    private javax.swing.JButton searchBtn;
    private javax.swing.JTextField searchTextField;
    private javax.swing.JPanel statsPanel;
    private javax.swing.JPanel tablePanel;
    private javax.swing.JScrollPane tableScrollPane;
    private javax.swing.JLabel timeLabel;
    private javax.swing.JPanel topPanel;
    // End of variables declaration//GEN-END:variables
}

