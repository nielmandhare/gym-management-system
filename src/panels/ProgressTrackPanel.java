package panels;

import connection.MySQL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.table.DefaultTableModel;
import util.RoundedUtil;
import util.SearchSuggestions;
import java.util.logging.Logger;
import java.util.logging.Level;
import util.LoggerUtil;

/**
 *
 * @author mgssr
 */
public class ProgressTrackPanel extends javax.swing.JPanel {

    public static String daysCount;
    public static String heightCount;
    public static String weightCount;
    public static String bmiCount;

    private static final Logger logger = LoggerUtil.getLogger(ProgressTrackPanel.class);

    /**
     * Creates new form ProgressTrackPanel
     */
    public ProgressTrackPanel() {
        security.Session.requireAdmin();
        initComponents();
        initCustomizations();
        SearchSuggestions.attach(searchTextField, searchBtn,
                "SELECT DISTINCT m.name FROM member m JOIN progress_track p ON p.member_member_id=m.member_id WHERE m.name LIKE ? ORDER BY m.name LIMIT 8");
        initPanelData();
    }

    private void initCustomizations() {
        setPaddings();
        setBorderRadius();
    }

    private void setPaddings() {
        statsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    }

    private void setBorderRadius() {
        int radius = 15;

        RoundedUtil.roundedCorners(searchTextField, radius);
//        RoundedUtil.roundedCorners(statsPanel, radius);
        RoundedUtil.roundedCorners(jPanel9, radius);
        RoundedUtil.roundedCorners(jPanel10, radius);
        RoundedUtil.roundedCorners(jPanel11, radius);
        RoundedUtil.roundedCorners(jPanel12, radius);
        RoundedUtil.roundedCorners(tableScrollPane, radius);
    }

//    ----------------------------------
    private void initPanelData() {
        setTableData();
        setStatsPanelData();
    }

    private void setTableData() {
        String query = "SELECT * "
                + "FROM member "
                + "LEFT JOIN "
                + "progress_track ON member_member_id = member_id "
                + "ORDER BY date DESC;";

        try {
            DefaultTableModel dtm = (DefaultTableModel) membersTable.getModel();
            dtm.setRowCount(0);
            ResultSet rs = MySQL.executeSearch(query);

            while (rs.next()) {
                Vector<String> dataRow = new Vector();
                dataRow.add(rs.getString("name"));
                dataRow.add(rs.getString("date"));
                dataRow.add(rs.getString("height_cm"));
                dataRow.add(rs.getString("weight_kg"));
                dataRow.add(rs.getString("body_fat"));
                dataRow.add(rs.getString("bmi"));

                dtm.addRow(dataRow);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "ProgressTrackPanel.setTableData(){} caused an error!", e);
        }
    }

    private void setStatsPanelData() {
        String search = searchTextField.getText().trim();
        String query = """
                       WITH ranked AS (
                           SELECT p.*, m.name, m.mobile, m.member_id,
                                  ROW_NUMBER() OVER (PARTITION BY p.member_member_id ORDER BY p.date DESC, p.progress_id DESC) AS row_num,
                                  COUNT(*) OVER (PARTITION BY p.member_member_id) AS entry_count
                           FROM progress_track p
                           JOIN member m ON m.member_id = p.member_member_id
                           WHERE (? = '' OR m.name LIKE ? OR m.mobile LIKE ? OR CAST(m.member_id AS CHAR) LIKE ?)
                       )
                       SELECT COUNT(*) AS members, COALESCE(SUM(entry_count), 0) AS entries,
                              ROUND(AVG(height_cm)) AS height, ROUND(AVG(weight_kg)) AS weight,
                              ROUND(AVG(bmi), 1) AS bmi
                       FROM ranked WHERE row_num = 1
                       """;

        try {
            ResultSet rs = MySQL.executeSearch(query, search, "%" + search + "%", "%" + search + "%", "%" + search + "%");

            if (rs.next()) {
                daysCount = String.format("%02d", rs.getInt("members"));
                heightCount = String.format("%02d", rs.getInt("height"));
                weightCount = String.format("%02d", rs.getInt("weight"));
                bmiCount = String.format("%.1f", rs.getDouble("bmi"));
            }
            daysLabel.setText(daysCount);
            heightLabel.setText(heightCount);
            weightLabel.setText(weightCount);
            bmiLabel.setText(bmiCount);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "ProgressTrackPanel.setStatsPanelData(){} caused an error!", e);
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
        jPanel9 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        daysLabel = new javax.swing.JLabel();
        jPanel10 = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        heightLabel = new javax.swing.JLabel();
        jPanel11 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        weightLabel = new javax.swing.JLabel();
        jPanel12 = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        bmiLabel = new javax.swing.JLabel();
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
        statsPanel.setOpaque(false);
        statsPanel.setLayout(new java.awt.GridLayout(2, 2, 5, 5));

        jPanel9.setBackground(new java.awt.Color(51, 51, 51));
        jPanel9.setLayout(new java.awt.GridLayout(1, 2));

        jLabel9.setFont(new java.awt.Font("Poppins", 0, 12)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(204, 204, 204));
        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel9.setText("Members ");
        jPanel9.add(jLabel9);

        daysLabel.setFont(new java.awt.Font("Poppins SemiBold", 0, 24)); // NOI18N
        daysLabel.setText(" 00");
        jPanel9.add(daysLabel);

        statsPanel.add(jPanel9);

        jPanel10.setBackground(jPanel9.getBackground());
        jPanel10.setLayout(new java.awt.GridLayout(1, 2));

        jLabel13.setFont(jLabel9.getFont());
        jLabel13.setForeground(jLabel9.getForeground());
        jLabel13.setHorizontalAlignment(jLabel9.getHorizontalAlignment());
        jLabel13.setText("Avg height (cm) ");
        jLabel13.setVerticalAlignment(jLabel9.getVerticalAlignment());
        jPanel10.add(jLabel13);

        heightLabel.setFont(daysLabel.getFont());
        heightLabel.setForeground(daysLabel.getForeground());
        heightLabel.setText(" 00");
        heightLabel.setVerticalAlignment(daysLabel.getVerticalAlignment());
        jPanel10.add(heightLabel);

        statsPanel.add(jPanel10);

        jPanel11.setBackground(jPanel9.getBackground());
        jPanel11.setLayout(new java.awt.GridLayout(1, 2));

        jLabel11.setFont(jLabel9.getFont());
        jLabel11.setForeground(jLabel9.getForeground());
        jLabel11.setHorizontalAlignment(jLabel9.getHorizontalAlignment());
        jLabel11.setText("Avg weight (kg) ");
        jLabel11.setVerticalAlignment(jLabel9.getVerticalAlignment());
        jPanel11.add(jLabel11);

        weightLabel.setFont(daysLabel.getFont());
        weightLabel.setForeground(daysLabel.getForeground());
        weightLabel.setText(" 00");
        weightLabel.setVerticalAlignment(daysLabel.getVerticalAlignment());
        jPanel11.add(weightLabel);

        statsPanel.add(jPanel11);

        jPanel12.setBackground(jPanel9.getBackground());
        jPanel12.setLayout(new java.awt.GridLayout(1, 2));

        jLabel15.setFont(jLabel9.getFont());
        jLabel15.setForeground(jLabel9.getForeground());
        jLabel15.setHorizontalAlignment(jLabel9.getHorizontalAlignment());
        jLabel15.setText("Avg BMI ");
        jLabel15.setVerticalAlignment(jLabel9.getVerticalAlignment());
        jPanel12.add(jLabel15);

        bmiLabel.setFont(daysLabel.getFont());
        bmiLabel.setForeground(daysLabel.getForeground());
        bmiLabel.setText(" 00");
        bmiLabel.setVerticalAlignment(daysLabel.getVerticalAlignment());
        jPanel12.add(bmiLabel);

        statsPanel.add(jPanel12);

        membersTable.setAutoCreateRowSorter(true);
        membersTable.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        membersTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "NAME", "DATE", "HEIGHT", "WEGHT", "BODY FAT %", "BMI"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        membersTable.setCellSelectionEnabled(true);
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
                .addComponent(tableScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 342, Short.MAX_VALUE)
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
                    .addComponent(statsPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 774, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(topPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
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
        setStatsPanelData();
    }

    private void searchTableData(String searchText) {
        String query = "SELECT * "
                + "FROM member "
                + "LEFT JOIN "
                + "progress_track ON member_member_id = member_id "
                + "WHERE "
                + "name LIKE ? OR "
                + "progress_id LIKE ? OR "
                + "member_id LIKE ? OR "
                + "mobile LIKE ?;";

        try {
            DefaultTableModel dtm = (DefaultTableModel) membersTable.getModel();
            dtm.setRowCount(0);
            ResultSet rs = MySQL.executeSearch(query, searchText, searchText, searchText, searchText);

            while (rs.next()) {
                Vector<String> dataRow = new Vector();
                dataRow.add(rs.getString("name"));
                dataRow.add(rs.getString("date"));
                dataRow.add(rs.getString("height_cm"));
                dataRow.add(rs.getString("weight_kg"));
                dataRow.add(rs.getString("body_fat"));
                dataRow.add(rs.getString("bmi"));

                dtm.addRow(dataRow);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "ProgressTrackPanel.searchTableData(){} caused an error!", e);
        }
    }

    public void refreshData() {
        setTableData();
        setStatsPanelData();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel bmiLabel;
    private javax.swing.JLabel daysLabel;
    private javax.swing.JLabel heightLabel;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JTable membersTable;
    private javax.swing.JButton searchBtn;
    private javax.swing.JTextField searchTextField;
    private javax.swing.JPanel statsPanel;
    private javax.swing.JPanel tablePanel;
    private javax.swing.JScrollPane tableScrollPane;
    private javax.swing.JPanel topPanel;
    private javax.swing.JLabel weightLabel;
    // End of variables declaration//GEN-END:variables
}

