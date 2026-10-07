package gui;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import connection.MySQL;
import java.awt.CardLayout;
import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import panels.AttendancePanel;
import panels.EquipmentPanel;
import panels.GenerateReportsPanel;
import panels.MembersPanel;
import panels.OtherProductsPanel;
import panels.PaymentRecordsPanel;
import panels.ProgressTrackPanel;
import panels.TrainersPanel;
import panels.WorkoutPlansPanel;
import util.RoundedUtil;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.logging.Level;
import panels.dialog.ManageMemberDialog;
import util.TimeUtil;
import java.util.logging.Logger;
import util.AppIconUtil;
import util.LoggerUtil;

/**
 *
 * @author mgssr
 */
public class MainInterface extends javax.swing.JFrame {

    public static String monthStart;
    public static String monthEnd;
    public static String dayStart;
    public static String dayEnd;

    public static String memberCount;
    public static String trainerCount;
    public static String dailyLogginCount;
    public static String monthlyPaymentTotal;
    public static String productSalesTotal;

    private static MainInterface mainInterface;

    private List<JButton> sidePanelBtns;

    private MembersPanel membersPanel;
    private WorkoutPlansPanel workoutPlansPanel;
    private AttendancePanel attendancePanel;
    private ProgressTrackPanel progressTrackPanel;
    private TrainersPanel trainersPanel;
    private EquipmentPanel equipmentPanel;
    private OtherProductsPanel otherProductsPanel;
    private PaymentRecordsPanel paymentRecordsPanel;
    private GenerateReportsPanel generateReportsPanel;

    private static final Logger logger = LoggerUtil.getLogger(MainInterface.class);

    private MainInterface() {
        initComponents();
        initCustomizations();
        initDashboardData();
    }

    public static MainInterface getInstance() {
        if (mainInterface == null) {
            mainInterface = new MainInterface();
        }
        return mainInterface;
    }

    private void initCustomizations() {
        AppIconUtil.applyIcon(this);
        setBanner();
        setPaddings();
        setBorderRadius();
        addPanelCards();
        createSidePanelBtnList();
        JButton logoutBtn = new JButton("Log Out");
        sidePanel.setLayout(new java.awt.GridLayout(11, 1, 0, 3));
        sidePanel.add(logoutBtn);
        logoutBtn.addActionListener(e -> logOut());
    }

    private void addPanelCards() {
        membersPanel = new MembersPanel();
        workoutPlansPanel = new WorkoutPlansPanel();
        attendancePanel = new AttendancePanel();
        progressTrackPanel = new ProgressTrackPanel();
        trainersPanel = new TrainersPanel();
        equipmentPanel = new EquipmentPanel();
        otherProductsPanel = new OtherProductsPanel();
        paymentRecordsPanel = new PaymentRecordsPanel();
        generateReportsPanel = new GenerateReportsPanel();

        mainPanel.add(membersPanel, "membersPanel");
        mainPanel.add(workoutPlansPanel, "workoutPlansPanel");
        mainPanel.add(attendancePanel, "attendancePanel");
        mainPanel.add(progressTrackPanel, "progressTrackPanel");
        mainPanel.add(trainersPanel, "trainersPanel");
        mainPanel.add(equipmentPanel, "equipmentPanel");
        mainPanel.add(otherProductsPanel, "otherProductsPanel");
        mainPanel.add(paymentRecordsPanel, "paymentRecordsPanel");
        mainPanel.add(generateReportsPanel, "generateReportsPanel");
    }

    private void createSidePanelBtnList() {
        sidePanelBtns = Arrays.asList(
                dashboardBtn,
                membersBtn,
                workoutPlansBtn,
                attendanceBtn,
                progressTrackBtn,
                trainersBtn,
                equipmentBtn,
                otherProductsBtn,
                paymentRecordsBtn,
                generateReportsBtn
        );
    }

    private void setBanner() {
//        if using png/jpg type image
//        ImageIcon bannerIcon = new ImageIcon(getClass().getResource("/img/banner.jpg"));
//
//        int orgHeight = bannerIcon.getIconHeight();
//        int orgWidth = bannerIcon.getIconWidth();
//
//        double orgRatio = (double) orgHeight / orgWidth;
//
//        int newWidth = tImgContainer.getWidth();
//        int newHeight = (int) (newWidth * orgRatio);
//
//        Image bannerImage = bannerIcon.getImage().getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
//        ImageIcon scaledBannerIcon = new ImageIcon(bannerImage);
//        dashboardImgLabel.setIcon(scaledBannerIcon);
//
//        ------------------------------------------------
//        if using svg svg type image
        FlatSVGIcon svg = new FlatSVGIcon(getClass().getResource("/img/banner.svg"));

        int orgHeight2 = svg.getIconHeight();
        int orgWidth2 = svg.getIconWidth();

        double orgRatio2 = (double) orgHeight2 / orgWidth2;

        int newWidth2 = tImgContainer.getWidth();
        int newHeight2 = (int) (newWidth2 * orgRatio2);
        svg = new FlatSVGIcon("img/banner.svg", newWidth2, newHeight2);
        dashboardImgLabel.setIcon(svg);
    }

    private void setPaddings() {
        int p1 = 5;
        int p2 = 10;
        sidePanel.setBorder(BorderFactory.createEmptyBorder(p1, p1, p1, p1));   //  top, left, bottom, right
        mainPanel.setBorder(BorderFactory.createEmptyBorder(p2, p1, p2, p2));   //  top, left, bottom, right
    }

    private void setBorderRadius() {
        int radius = 15;

        RoundedUtil.roundedCorners(tMemberCount, radius);
        RoundedUtil.roundedCorners(tTrainerCount, radius);
        RoundedUtil.roundedCorners(tLogginCount, radius);
        RoundedUtil.roundedCorners(tPayments, radius);
        RoundedUtil.roundedCorners(tProductSales, radius);
        RoundedUtil.roundedCorners(tImgContainer, radius);
        RoundedUtil.roundedCorners(dashboardImgLabel, radius);
    }

    public void showCard(String panel, JButton button) {
        CardLayout cL = (CardLayout) mainPanel.getLayout();
        cL.show(mainPanel, panel);

        if (button != null) {
            setActiveBtn(button);
        }
    }

    private void setActiveBtn(JButton activeBtn) {
        Color activeBackgroundColor = new Color(255, 100, 0);
        Color activeForegroundColor = new Color(221, 221, 221);

        Color inactiveBackgroundColor = new Color(86, 86, 86);
        Color inactiveForegroundColor = new Color(221, 221, 221);

        for (JButton btn : sidePanelBtns) {
            btn.setBackground(inactiveBackgroundColor);
            btn.setForeground(inactiveForegroundColor);
        }

        activeBtn.setBackground(activeBackgroundColor);
        activeBtn.setForeground(activeForegroundColor);
    }

//    ------------------------------------
    private void initDashboardData() {
        monthStart = TimeUtil.getMonthStartDate();
        monthEnd = TimeUtil.getMonthEndDate();

        setMemberCount();
        setTrainerCount();
        setDailyLogginCount();
        setMonthlyPaymenTotal();
        setProductSalesTotal();
    }

    private void setMemberCount() {
        String query = "SELECT COUNT(*) AS total_members FROM member";

        try {
            ResultSet rs = MySQL.executeSearch(query);

            if (rs.next()) {
                memberCount = String.format("%02d", rs.getInt("total_members"));
            }
            membersCountLabel.setText(memberCount);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "MainInterface.setMemberCount(){} caused an error!", e);
        }
    }

    private void setTrainerCount() {
        String query = "SELECT COUNT(*) AS total_trainers FROM trainer";

        try {
            ResultSet rs = MySQL.executeSearch(query);

            if (rs.next()) {
                trainerCount = String.format("%02d", rs.getInt("total_trainers"));
            }
            trainersCountLabel.setText(trainerCount);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "MainInterface.setTrainerCount(){} caused an error!", e);
        }
    }

    private void setDailyLogginCount() {
        String query = "SELECT COUNT(*) AS total_loggins "
                + "FROM member_attendance "
                + "WHERE date >= '" + monthStart + "' AND date <= '" + monthEnd + "'";

        try {
            ResultSet rs = MySQL.executeSearch(query);

            if (rs.next()) {
                dailyLogginCount = String.format("%02d", rs.getInt("total_loggins"));
            }
            logginsCountLabel.setText(dailyLogginCount);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "MainInterface.setDailyLogginCount(){} caused an error!", e);
        }
    }

    private void setMonthlyPaymenTotal() {
        String query = "SELECT SUM(paid_amount) AS total_paid_amount "
                + "FROM payment_history "
                + "WHERE date >= '" + monthStart + "' AND date <= '" + monthEnd + "'";

        try {
            ResultSet rs = MySQL.executeSearch(query);

            if (rs.next()) {
                monthlyPaymentTotal = new DecimalFormat("###,###,#00").format(rs.getInt("total_paid_amount"));
            }
            paymentsCountLabel.setText(monthlyPaymentTotal + " LKR");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "MainInterface.setMonthlyPaymenTotal(){} caused an error!", e);
        }
    }

    private void setProductSalesTotal() {
        String query = "SELECT SUM(total_amount) AS total_total_amount "
                + "FROM order_history "
                + "WHERE date >= '" + monthStart + "' AND date <= '" + monthEnd + "'";

        try {
            ResultSet rs = MySQL.executeSearch(query);

            if (rs.next()) {
                productSalesTotal = new DecimalFormat("###,###,#00").format(rs.getInt("total_total_amount"));
            }
            otherSalesCountLabel.setText(productSalesTotal + " LKR");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "MainInterface.setProductSalesTotal(){} caused an error!", e);
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

        sidePanel = new javax.swing.JPanel();
        dashboardBtn = new javax.swing.JButton();
        membersBtn = new javax.swing.JButton();
        workoutPlansBtn = new javax.swing.JButton();
        attendanceBtn = new javax.swing.JButton();
        progressTrackBtn = new javax.swing.JButton();
        trainersBtn = new javax.swing.JButton();
        equipmentBtn = new javax.swing.JButton();
        otherProductsBtn = new javax.swing.JButton();
        paymentRecordsBtn = new javax.swing.JButton();
        generateReportsBtn = new javax.swing.JButton();
        mainPanel = new javax.swing.JPanel();
        dashboardPanel = new javax.swing.JPanel();
        row1 = new javax.swing.JPanel();
        tMemberCount = new javax.swing.JPanel();
        membersLabel = new javax.swing.JLabel();
        membersCountLabel = new javax.swing.JLabel();
        tTrainerCount = new javax.swing.JPanel();
        trainersLabel = new javax.swing.JLabel();
        trainersCountLabel = new javax.swing.JLabel();
        tOther = new javax.swing.JPanel();
        newMemberBtn = new javax.swing.JButton();
        tLogginCount = new javax.swing.JPanel();
        logginsLabel = new javax.swing.JLabel();
        logginsCountLabel = new javax.swing.JLabel();
        row2 = new javax.swing.JPanel();
        tPayments = new javax.swing.JPanel();
        paymentsLabel = new javax.swing.JLabel();
        paymentsCountLabel = new javax.swing.JLabel();
        tProductSales = new javax.swing.JPanel();
        otherSalesLabel = new javax.swing.JLabel();
        otherSalesCountLabel = new javax.swing.JLabel();
        row3 = new javax.swing.JPanel();
        tImgContainer = new javax.swing.JPanel();
        dashboardImgLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("GYM Management System");
        setMinimumSize(new java.awt.Dimension(598, 540));
        setPreferredSize(new java.awt.Dimension(970, 650));
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });

        sidePanel.setMinimumSize(new java.awt.Dimension(200, 495));
        sidePanel.setPreferredSize(new java.awt.Dimension(200, 400));
        sidePanel.setLayout(new java.awt.GridLayout(10, 1, 0, 3));

        dashboardBtn.setBackground(new java.awt.Color(255, 100, 0));
        dashboardBtn.setFont(new java.awt.Font("Poppins SemiBold", 0, 14)); // NOI18N
        dashboardBtn.setText("Dashboard");
        dashboardBtn.setFocusPainted(false);
        dashboardBtn.setMinimumSize(new java.awt.Dimension(0, 45));
        dashboardBtn.setPreferredSize(new java.awt.Dimension(200, 45));
        dashboardBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dashboardBtnActionPerformed(evt);
            }
        });
        sidePanel.add(dashboardBtn);

        membersBtn.setFont(new java.awt.Font("Poppins SemiBold", 0, 14)); // NOI18N
        membersBtn.setText("Members");
        membersBtn.setFocusable(false);
        membersBtn.setMinimumSize(new java.awt.Dimension(0, 45));
        membersBtn.setPreferredSize(new java.awt.Dimension(200, 45));
        membersBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                membersBtnActionPerformed(evt);
            }
        });
        sidePanel.add(membersBtn);

        workoutPlansBtn.setBackground(membersBtn.getBackground());
        workoutPlansBtn.setFont(new java.awt.Font("Poppins SemiBold", 0, 14)); // NOI18N
        workoutPlansBtn.setText("Workout Plans");
        workoutPlansBtn.setFocusable(false);
        workoutPlansBtn.setMinimumSize(new java.awt.Dimension(0, 45));
        workoutPlansBtn.setPreferredSize(new java.awt.Dimension(200, 45));
        workoutPlansBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                workoutPlansBtnActionPerformed(evt);
            }
        });
        sidePanel.add(workoutPlansBtn);

        attendanceBtn.setBackground(membersBtn.getBackground());
        attendanceBtn.setFont(new java.awt.Font("Poppins SemiBold", 0, 14)); // NOI18N
        attendanceBtn.setText("Attendance");
        attendanceBtn.setFocusable(false);
        attendanceBtn.setMinimumSize(new java.awt.Dimension(0, 45));
        attendanceBtn.setPreferredSize(new java.awt.Dimension(200, 45));
        attendanceBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                attendanceBtnActionPerformed(evt);
            }
        });
        sidePanel.add(attendanceBtn);

        progressTrackBtn.setBackground(membersBtn.getBackground());
        progressTrackBtn.setFont(new java.awt.Font("Poppins SemiBold", 0, 14)); // NOI18N
        progressTrackBtn.setText("Progress Track");
        progressTrackBtn.setFocusable(false);
        progressTrackBtn.setMinimumSize(new java.awt.Dimension(0, 45));
        progressTrackBtn.setPreferredSize(new java.awt.Dimension(200, 45));
        progressTrackBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                progressTrackBtnActionPerformed(evt);
            }
        });
        sidePanel.add(progressTrackBtn);

        trainersBtn.setBackground(membersBtn.getBackground());
        trainersBtn.setFont(new java.awt.Font("Poppins SemiBold", 0, 14)); // NOI18N
        trainersBtn.setText("Trainers");
        trainersBtn.setFocusable(false);
        trainersBtn.setMinimumSize(new java.awt.Dimension(0, 45));
        trainersBtn.setPreferredSize(new java.awt.Dimension(200, 45));
        trainersBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                trainersBtnActionPerformed(evt);
            }
        });
        sidePanel.add(trainersBtn);

        equipmentBtn.setBackground(membersBtn.getBackground());
        equipmentBtn.setFont(new java.awt.Font("Poppins SemiBold", 0, 14)); // NOI18N
        equipmentBtn.setText("Equipment");
        equipmentBtn.setFocusable(false);
        equipmentBtn.setMinimumSize(new java.awt.Dimension(0, 45));
        equipmentBtn.setPreferredSize(new java.awt.Dimension(200, 45));
        equipmentBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                equipmentBtnActionPerformed(evt);
            }
        });
        sidePanel.add(equipmentBtn);

        otherProductsBtn.setBackground(membersBtn.getBackground());
        otherProductsBtn.setFont(new java.awt.Font("Poppins SemiBold", 0, 14)); // NOI18N
        otherProductsBtn.setText("Other Products");
        otherProductsBtn.setFocusable(false);
        otherProductsBtn.setMinimumSize(new java.awt.Dimension(0, 45));
        otherProductsBtn.setPreferredSize(new java.awt.Dimension(200, 45));
        otherProductsBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                otherProductsBtnActionPerformed(evt);
            }
        });
        sidePanel.add(otherProductsBtn);

        paymentRecordsBtn.setBackground(membersBtn.getBackground());
        paymentRecordsBtn.setFont(new java.awt.Font("Poppins SemiBold", 0, 14)); // NOI18N
        paymentRecordsBtn.setText("Payment Records");
        paymentRecordsBtn.setFocusable(false);
        paymentRecordsBtn.setMinimumSize(new java.awt.Dimension(0, 45));
        paymentRecordsBtn.setPreferredSize(new java.awt.Dimension(200, 45));
        paymentRecordsBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                paymentRecordsBtnActionPerformed(evt);
            }
        });
        sidePanel.add(paymentRecordsBtn);

        generateReportsBtn.setBackground(membersBtn.getBackground());
        generateReportsBtn.setFont(new java.awt.Font("Poppins SemiBold", 0, 14)); // NOI18N
        generateReportsBtn.setText("Generate Reports");
        generateReportsBtn.setFocusable(false);
        generateReportsBtn.setMinimumSize(new java.awt.Dimension(0, 45));
        generateReportsBtn.setPreferredSize(new java.awt.Dimension(200, 45));
        generateReportsBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateReportsBtnActionPerformed(evt);
            }
        });
        sidePanel.add(generateReportsBtn);

        getContentPane().add(sidePanel, java.awt.BorderLayout.WEST);

        mainPanel.setPreferredSize(new java.awt.Dimension(600, 600));
        mainPanel.setLayout(new java.awt.CardLayout());

        dashboardPanel.setLayout(new java.awt.GridLayout(3, 1, 0, 7));

        row1.setBackground(new java.awt.Color(51, 51, 51));
        row1.setOpaque(false);
        row1.setLayout(new java.awt.GridLayout(1, 3, 7, 0));

        tMemberCount.setBackground(new java.awt.Color(51, 51, 51));
        tMemberCount.setLayout(new java.awt.GridLayout(2, 1));

        membersLabel.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        membersLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        membersLabel.setText("Members");
        tMemberCount.add(membersLabel);

        membersCountLabel.setFont(new java.awt.Font("Poppins", 1, 48)); // NOI18N
        membersCountLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        membersCountLabel.setText("156");
        membersCountLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        tMemberCount.add(membersCountLabel);

        row1.add(tMemberCount);

        tTrainerCount.setBackground(tMemberCount.getBackground());
        tTrainerCount.setLayout(new java.awt.GridLayout(2, 1));

        trainersLabel.setFont(membersLabel.getFont());
        trainersLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        trainersLabel.setText("Trainer");
        tTrainerCount.add(trainersLabel);

        trainersCountLabel.setFont(membersCountLabel.getFont());
        trainersCountLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        trainersCountLabel.setText("13");
        trainersCountLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        tTrainerCount.add(trainersCountLabel);

        row1.add(tTrainerCount);

        tOther.setBackground(new java.awt.Color(162, 30, 255));
        tOther.setOpaque(false);
        tOther.setLayout(new java.awt.GridLayout(2, 1, 0, 5));

        newMemberBtn.setBackground(new java.awt.Color(255, 100, 0));
        newMemberBtn.setFont(dashboardBtn.getFont());
        newMemberBtn.setText("New Member");
        newMemberBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newMemberBtnActionPerformed(evt);
            }
        });
        tOther.add(newMemberBtn);

        tLogginCount.setBackground(tMemberCount.getBackground());
        tLogginCount.setLayout(new java.awt.GridLayout(2, 1));

        logginsLabel.setFont(membersLabel.getFont());
        logginsLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        logginsLabel.setText("Daily Loggins");
        tLogginCount.add(logginsLabel);

        logginsCountLabel.setFont(new java.awt.Font("Poppins", 1, 24)); // NOI18N
        logginsCountLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        logginsCountLabel.setText("42");
        logginsCountLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        tLogginCount.add(logginsCountLabel);

        tOther.add(tLogginCount);

        row1.add(tOther);

        dashboardPanel.add(row1);

        row2.setBackground(new java.awt.Color(51, 51, 51));
        row2.setOpaque(false);
        row2.setLayout(new java.awt.GridLayout(1, 2, 7, 0));

        tPayments.setBackground(tMemberCount.getBackground());
        tPayments.setLayout(new java.awt.GridLayout(2, 1));

        paymentsLabel.setFont(membersLabel.getFont());
        paymentsLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        paymentsLabel.setText("Monthly Payments");
        tPayments.add(paymentsLabel);

        paymentsCountLabel.setFont(membersCountLabel.getFont());
        paymentsCountLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        paymentsCountLabel.setText("452100");
        paymentsCountLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        tPayments.add(paymentsCountLabel);

        row2.add(tPayments);

        tProductSales.setBackground(tMemberCount.getBackground());
        tProductSales.setLayout(new java.awt.GridLayout(2, 1));

        otherSalesLabel.setFont(membersLabel.getFont());
        otherSalesLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        otherSalesLabel.setText("Other Product Sales");
        tProductSales.add(otherSalesLabel);

        otherSalesCountLabel.setFont(membersCountLabel.getFont());
        otherSalesCountLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        otherSalesCountLabel.setText("38000");
        otherSalesCountLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        tProductSales.add(otherSalesCountLabel);

        row2.add(tProductSales);

        dashboardPanel.add(row2);

        row3.setBackground(new java.awt.Color(51, 51, 51));
        row3.setOpaque(false);
        row3.setLayout(new java.awt.GridLayout(1, 1));

        tImgContainer.setLayout(new java.awt.GridLayout(1, 1));

        dashboardImgLabel.setBackground(tMemberCount.getBackground());
        dashboardImgLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        dashboardImgLabel.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 100, 0), 1, true));
        dashboardImgLabel.setOpaque(true);
        dashboardImgLabel.setPreferredSize(new java.awt.Dimension(1280, 353));
        tImgContainer.add(dashboardImgLabel);

        row3.add(tImgContainer);

        dashboardPanel.add(row3);

        mainPanel.add(dashboardPanel, "dashboardPanel");

        getContentPane().add(mainPanel, java.awt.BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void dashboardBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dashboardBtnActionPerformed
        showCard("dashboardPanel", dashboardBtn);
    }//GEN-LAST:event_dashboardBtnActionPerformed

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
        setBanner();
    }//GEN-LAST:event_formComponentResized

    private void newMemberBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newMemberBtnActionPerformed
        ManageMemberDialog manageMemberDialog = new ManageMemberDialog(this, true);
        manageMemberDialog.setLocationRelativeTo(null);
        manageMemberDialog.setVisible(true);
        refreshMemberData();
    }//GEN-LAST:event_newMemberBtnActionPerformed

    private void membersBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_membersBtnActionPerformed
        showCard("membersPanel", membersBtn);
    }//GEN-LAST:event_membersBtnActionPerformed

    private void workoutPlansBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_workoutPlansBtnActionPerformed
        showCard("workoutPlansPanel", workoutPlansBtn);
    }//GEN-LAST:event_workoutPlansBtnActionPerformed

    private void attendanceBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_attendanceBtnActionPerformed
        showCard("attendancePanel", attendanceBtn);
    }//GEN-LAST:event_attendanceBtnActionPerformed

    private void progressTrackBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_progressTrackBtnActionPerformed
        showCard("progressTrackPanel", progressTrackBtn);
    }//GEN-LAST:event_progressTrackBtnActionPerformed

    private void trainersBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_trainersBtnActionPerformed
        showCard("trainersPanel", trainersBtn);
    }//GEN-LAST:event_trainersBtnActionPerformed

    private void equipmentBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_equipmentBtnActionPerformed
        showCard("equipmentPanel", equipmentBtn);
    }//GEN-LAST:event_equipmentBtnActionPerformed

    private void otherProductsBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_otherProductsBtnActionPerformed
        showCard("otherProductsPanel", otherProductsBtn);
    }//GEN-LAST:event_otherProductsBtnActionPerformed

    private void paymentRecordsBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_paymentRecordsBtnActionPerformed
        showCard("paymentRecordsPanel", paymentRecordsBtn);
    }//GEN-LAST:event_paymentRecordsBtnActionPerformed

    private void generateReportsBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateReportsBtnActionPerformed
        showCard("generateReportsPanel", generateReportsBtn);
    }//GEN-LAST:event_generateReportsBtnActionPerformed

    public void refreshAllData() {
        initDashboardData();
        attendancePanel.refreshData();
        equipmentPanel.refreshData();
        membersPanel.refreshData();
        otherProductsPanel.refreshData();
        paymentRecordsPanel.refreshData();
        progressTrackPanel.refreshData();
        trainersPanel.refreshData();
        workoutPlansPanel.refreshData();
    }

    public void logOut() {
        for (java.awt.Window window : java.awt.Window.getWindows()) {
            if (window instanceof panels.dialog.AttendanceDialog) window.dispose();
        }
        dispose();
        mainInterface = null;
        new LoginForm().setVisible(true);
    }

    public void refreshMemberData() {
        initDashboardData();
        membersPanel.refreshData();
    }

    public void refreshAttendanceData() {
        attendancePanel.refreshData();
        initDashboardData();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        FlatMacDarkLaf.setup();

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                getInstance().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton attendanceBtn;
    private javax.swing.JButton dashboardBtn;
    private javax.swing.JLabel dashboardImgLabel;
    private javax.swing.JPanel dashboardPanel;
    private javax.swing.JButton equipmentBtn;
    private javax.swing.JButton generateReportsBtn;
    private javax.swing.JLabel logginsCountLabel;
    private javax.swing.JLabel logginsLabel;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JButton membersBtn;
    private javax.swing.JLabel membersCountLabel;
    private javax.swing.JLabel membersLabel;
    private javax.swing.JButton newMemberBtn;
    private javax.swing.JButton otherProductsBtn;
    private javax.swing.JLabel otherSalesCountLabel;
    private javax.swing.JLabel otherSalesLabel;
    private javax.swing.JButton paymentRecordsBtn;
    private javax.swing.JLabel paymentsCountLabel;
    private javax.swing.JLabel paymentsLabel;
    private javax.swing.JButton progressTrackBtn;
    private javax.swing.JPanel row1;
    private javax.swing.JPanel row2;
    private javax.swing.JPanel row3;
    private javax.swing.JPanel sidePanel;
    private javax.swing.JPanel tImgContainer;
    private javax.swing.JPanel tLogginCount;
    private javax.swing.JPanel tMemberCount;
    private javax.swing.JPanel tOther;
    private javax.swing.JPanel tPayments;
    private javax.swing.JPanel tProductSales;
    private javax.swing.JPanel tTrainerCount;
    private javax.swing.JButton trainersBtn;
    private javax.swing.JLabel trainersCountLabel;
    private javax.swing.JLabel trainersLabel;
    private javax.swing.JButton workoutPlansBtn;
    // End of variables declaration//GEN-END:variables
}
