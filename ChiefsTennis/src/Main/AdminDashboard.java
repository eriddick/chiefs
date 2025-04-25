package Main;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import javax.swing.*;
import javax.swing.table.*;

public class AdminDashboard extends JFrame {
    private User currentUser;
    private DatabaseConnection dbConnection;

    private JTabbedPane tabbedPane;
    private JPanel memberPanel;
    private JPanel reservationsPanel;
    private JPanel reportingPanel;

    // Member management components
    private JTable memberTable;
    private DefaultTableModel memberTableModel;
    private JButton addMemberButton;
    private JButton editMemberButton;
    private JButton viewHistoryButton;
    private JButton deactivateMemberButton;
    private JTextField memberSearchField;
    private JButton memberSearchButton;
    private JCheckBox showInactiveCheckbox;

    // Reservation management components
    private JTable reservationTable;
    private DefaultTableModel reservationTableModel;
    private JButton viewReservationButton;
    private JButton cancelReservationButton;
    private JTextField reservationSearchField;
    private JButton reservationSearchButton;
    private JComboBox<String> dateRangeComboBox;

    // Reporting components
    private JPanel courtUsagePanel;
    private JPanel membershipPanel;
    private JPanel revenuePanel;

    public AdminDashboard(User user, DatabaseConnection dbConnection) {
        this.currentUser = user;

        // jacob test
        // if (currentUser == null) {
        // System.out.println("CC-N");
        // } else {
        // System.out.println("CC-NOT-N");
        // }

        this.dbConnection = new DatabaseConnection();

        initComponents();
        setupLayout();
        setupListeners();
        loadData();

        setTitle("Tennis Club - Admin Dashboard");
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void initComponents() {
        tabbedPane = new JTabbedPane();

        // Initialize member management components
        String[] memberColumns = { "ID", "First Name", "Last Name", "Email", "Phone", "Join Date", "Status" };
        memberTableModel = new DefaultTableModel(memberColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        memberTable = new JTable(memberTableModel);
        memberTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        memberTable.setAutoCreateRowSorter(true);

        addMemberButton = new JButton("Add Member");
        editMemberButton = new JButton("Edit Member");
        viewHistoryButton = new JButton("View History");
        deactivateMemberButton = new JButton("Deactivate Member");

        // Disable buttons until a row is selected
        editMemberButton.setEnabled(false);
        viewHistoryButton.setEnabled(false);
        deactivateMemberButton.setEnabled(false);

        memberSearchField = new JTextField(20);
        memberSearchButton = new JButton("Search");
        showInactiveCheckbox = new JCheckBox("Show Inactive Members");

        // Initialize reservation management components
        String[] reservationColumns = { "ID", "Court", "Member", "Date", "Time", "Type", "Players", "Created" };
        reservationTableModel = new DefaultTableModel(reservationColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        reservationTable = new JTable(reservationTableModel);
        reservationTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        reservationTable.setAutoCreateRowSorter(true);

        viewReservationButton = new JButton("View Details");
        cancelReservationButton = new JButton("Cancel Reservation");

        // Disable buttons until a row is selected
        viewReservationButton.setEnabled(false);
        cancelReservationButton.setEnabled(false);

        reservationSearchField = new JTextField(20);
        reservationSearchButton = new JButton("Search");

        String[] dateRanges = { "Today", "Tomorrow", "This Week", "Next Week", "This Month", "All Upcoming" };
        dateRangeComboBox = new JComboBox<>(dateRanges);

        // Initialize reporting components
        courtUsagePanel = new JPanel(new BorderLayout());
        courtUsagePanel.setBorder(BorderFactory.createTitledBorder("Court Usage"));

        membershipPanel = new JPanel(new BorderLayout());
        membershipPanel.setBorder(BorderFactory.createTitledBorder("Membership Statistics"));

        revenuePanel = new JPanel(new BorderLayout());
        revenuePanel.setBorder(BorderFactory.createTitledBorder("Revenue"));
    }

    private void setupLayout() {
        // Member management tab
        memberPanel = new JPanel(new BorderLayout(10, 10));
        memberPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel memberTopPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        memberTopPanel.add(new JLabel("Search:"));
        memberTopPanel.add(memberSearchField);
        memberTopPanel.add(memberSearchButton);
        memberTopPanel.add(Box.createHorizontalStrut(20));
        memberTopPanel.add(showInactiveCheckbox);

        memberPanel.add(memberTopPanel, BorderLayout.NORTH);

        JScrollPane memberScrollPane = new JScrollPane(memberTable);
        memberPanel.add(memberScrollPane, BorderLayout.CENTER);

        JPanel memberButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        memberButtonPanel.add(viewHistoryButton);
        memberButtonPanel.add(deactivateMemberButton);
        memberButtonPanel.add(editMemberButton);
        memberButtonPanel.add(addMemberButton);

        memberPanel.add(memberButtonPanel, BorderLayout.SOUTH);

        // Reservation management tab
        reservationsPanel = new JPanel(new BorderLayout(10, 10));
        reservationsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel reservationTopPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        reservationTopPanel.add(new JLabel("Search:"));
        reservationTopPanel.add(reservationSearchField);
        reservationTopPanel.add(reservationSearchButton);
        reservationTopPanel.add(Box.createHorizontalStrut(20));
        reservationTopPanel.add(new JLabel("Date Range:"));
        reservationTopPanel.add(dateRangeComboBox);

        reservationsPanel.add(reservationTopPanel, BorderLayout.NORTH);

        JScrollPane reservationScrollPane = new JScrollPane(reservationTable);
        reservationsPanel.add(reservationScrollPane, BorderLayout.CENTER);

        JPanel reservationButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        reservationButtonPanel.add(viewReservationButton);
        reservationButtonPanel.add(cancelReservationButton);

        reservationsPanel.add(reservationButtonPanel, BorderLayout.SOUTH);

        // Reporting tab
        reportingPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        reportingPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        setupCourtUsagePanel();
        setupMembershipPanel();
        setupRevenuePanel();

        reportingPanel.add(courtUsagePanel);
        reportingPanel.add(membershipPanel);
        reportingPanel.add(revenuePanel);

        // Add tabs to tabbed pane
        tabbedPane.addTab("Members", memberPanel);
        tabbedPane.addTab("Reservations", reservationsPanel);
        tabbedPane.addTab("Reports", reportingPanel);

        getContentPane().add(tabbedPane);
    }

    private void setupCourtUsagePanel() {
        courtUsagePanel.removeAll();

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        String[] timeRanges = { "This Week", "Last Week", "This Month", "Last Month", "Year to Date" };
        JComboBox<String> timeRangeComboBox = new JComboBox<>(timeRanges);
        topPanel.add(new JLabel("Time Period:"));
        topPanel.add(timeRangeComboBox);

        courtUsagePanel.add(topPanel, BorderLayout.NORTH);

        // Court usage table
        String[] columns = { "Court", "Hours Reserved", "% Utilization", "Singles", "Doubles" };
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        courtUsagePanel.add(scrollPane, BorderLayout.CENTER);

        // Load data based on selected time range
        timeRangeComboBox.addActionListener(e -> loadCourtUsageData(timeRangeComboBox.getSelectedItem().toString()));

        // Load initial data
        loadCourtUsageData("This Week");
    }

    private void setupMembershipPanel() {
        membershipPanel.removeAll();

        JPanel chartPanel = new JPanel();
        chartPanel.setLayout(new BorderLayout());

        JTextArea statsArea = new JTextArea();
        statsArea.setEditable(false);
        statsArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));

        chartPanel.add(new JScrollPane(statsArea), BorderLayout.CENTER);

        membershipPanel.add(chartPanel, BorderLayout.CENTER);

        // Load membership statistics
        loadMembershipStatistics(statsArea);
    }

    private void setupRevenuePanel() {
        revenuePanel.removeAll();

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        String[] timeRanges = { "This Month", "Last Month", "Last 3 Months", "Year to Date", "Last Year" };
        JComboBox<String> timeRangeComboBox = new JComboBox<>(timeRanges);
        topPanel.add(new JLabel("Time Period:"));
        topPanel.add(timeRangeComboBox);

        revenuePanel.add(topPanel, BorderLayout.NORTH);

        // Revenue statistics table
        String[] columns = { "Category", "Amount", "% of Total" };
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        revenuePanel.add(scrollPane, BorderLayout.CENTER);

        // Load data based on selected time range
        timeRangeComboBox
                .addActionListener(e -> loadRevenueData(timeRangeComboBox.getSelectedItem().toString(), model));

        // Load initial data
        loadRevenueData("This Month", model);
    }

    private void setupListeners() {
        // Member table selection listener
        memberTable.getSelectionModel().addListSelectionListener(e -> {
            boolean rowSelected = memberTable.getSelectedRow() != -1;
            editMemberButton.setEnabled(rowSelected);
            viewHistoryButton.setEnabled(rowSelected);
            deactivateMemberButton.setEnabled(rowSelected);
        });

        // Reservation table selection listener
        reservationTable.getSelectionModel().addListSelectionListener(e -> {
            boolean rowSelected = reservationTable.getSelectedRow() != -1;
            viewReservationButton.setEnabled(rowSelected);
            cancelReservationButton.setEnabled(rowSelected);
        });

        // Member search button
        memberSearchButton.addActionListener(e -> loadMembers());

        // Show inactive checkbox
        showInactiveCheckbox.addActionListener(e -> loadMembers());

        // Member search field enter key
        memberSearchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    loadMembers();
                }
            }
        });

        // if (currentUser == null) {
        // System.out.println("C-NULL0");
        // } else {
        // System.out.println("C-NULL-N0");
        // }

        // Add member button
        addMemberButton.addActionListener(e -> {
            MemberManagementScreen.MemberDialog dialog = new MemberManagementScreen(currentUser).new MemberDialog(this,
                    "Add New Member", -1);
            dialog.setVisible(true);

            if (dialog.isConfirmed()) {
                loadMembers();
            }
        });

        // Edit member button
        editMemberButton.addActionListener(e -> {
            int selectedRow = memberTable.getSelectedRow();
            if (selectedRow != -1) {
                int modelRow = memberTable.convertRowIndexToModel(selectedRow);
                int memberId = (int) memberTableModel.getValueAt(modelRow, 0);

                MemberManagementScreen.MemberDialog dialog = new MemberManagementScreen(currentUser).new MemberDialog(
                        this, "Edit Member", memberId);
                dialog.setVisible(true);

                if (dialog.isConfirmed()) {
                    loadMembers();
                }
            }
        });

        // View history button
        viewHistoryButton.addActionListener(e -> {
            int selectedRow = memberTable.getSelectedRow();
            if (selectedRow != -1) {
                int modelRow = memberTable.convertRowIndexToModel(selectedRow);
                int memberId = (int) memberTableModel.getValueAt(modelRow, 0);

                showMemberHistoryDialog(memberId);
            }
        });

        // Deactivate member button
        deactivateMemberButton.addActionListener(e -> {
            int selectedRow = memberTable.getSelectedRow();
            if (selectedRow != -1) {
                int modelRow = memberTable.convertRowIndexToModel(selectedRow);
                int memberId = (int) memberTableModel.getValueAt(modelRow, 0);
                String firstName = (String) memberTableModel.getValueAt(modelRow, 1);
                String lastName = (String) memberTableModel.getValueAt(modelRow, 2);

                int result = JOptionPane.showConfirmDialog(this,
                        "Are you sure you want to deactivate " + firstName + " " + lastName + "'s membership?",
                        "Confirm Deactivation",
                        JOptionPane.YES_NO_OPTION);

                if (result == JOptionPane.YES_OPTION) {
                    deactivateMember(memberId);
                }
            }
        });

        // Reservation search button
        reservationSearchButton.addActionListener(e -> loadReservations());

        // Date range combo box
        dateRangeComboBox.addActionListener(e -> loadReservations());

        // Reservation search field enter key
        reservationSearchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    loadReservations();
                }
            }
        });

        // View reservation button
        viewReservationButton.addActionListener(e -> {
            int selectedRow = reservationTable.getSelectedRow();
            if (selectedRow != -1) {
                int modelRow = reservationTable.convertRowIndexToModel(selectedRow);
                int reservationId = (int) reservationTableModel.getValueAt(modelRow, 0);

                showReservationDetailsDialog(reservationId);
            }
        });

        // Cancel reservation button
        cancelReservationButton.addActionListener(e -> {
            int selectedRow = reservationTable.getSelectedRow();
            if (selectedRow != -1) {
                int modelRow = reservationTable.convertRowIndexToModel(selectedRow);
                int reservationId = (int) reservationTableModel.getValueAt(modelRow, 0);
                String court = (String) reservationTableModel.getValueAt(modelRow, 1);
                String dateTime = (String) reservationTableModel.getValueAt(modelRow, 3) + " " +
                        (String) reservationTableModel.getValueAt(modelRow, 4);

                int result = JOptionPane.showConfirmDialog(this,
                        "Are you sure you want to cancel the reservation for " + court + " on " + dateTime + "?",
                        "Confirm Cancellation",
                        JOptionPane.YES_NO_OPTION);

                if (result == JOptionPane.YES_OPTION) {
                    cancelReservation(reservationId);
                }
            }
        });

        // Tab change listener
        tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();

            switch (selectedIndex) {
                case 0:
                    loadMembers();
                    break;
                case 1:
                    loadReservations();
                    break;
                case 2:
                    // Refresh reports
                    break;
            }
        });
    }

    private void loadData() {
        loadMembers();
        loadReservations();
    }

    private void loadMembers() {
        memberTableModel.setRowCount(0);

        try {
            Connection conn = dbConnection.getConnection();

            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("SELECT member_id, first_name, last_name, email, phone, ")
                    .append("join_date, status ")
                    .append("FROM Members WHERE 1=1 ");

            // Add search condition if search field is not empty
            String searchTerm = memberSearchField.getText().trim();
            if (!searchTerm.isEmpty()) {
                queryBuilder.append("AND (first_name LIKE ? OR last_name LIKE ? OR email LIKE ? OR phone LIKE ?) ");
            }

            // Add status condition based on checkbox
            if (!showInactiveCheckbox.isSelected()) {
                queryBuilder.append("AND status != 'INACTIVE' ");
            }

            // Add ordering
            queryBuilder.append("ORDER BY last_name, first_name");

            PreparedStatement stmt = conn.prepareStatement(queryBuilder.toString());

            // Set search parameters if needed
            if (!searchTerm.isEmpty()) {
                String likePattern = "%" + searchTerm + "%";
                stmt.setString(1, likePattern);
                stmt.setString(2, likePattern);
                stmt.setString(3, likePattern);
                stmt.setString(4, likePattern);
            }

            ResultSet rs = stmt.executeQuery();

            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

            while (rs.next()) {
                int memberId = rs.getInt("member_id");
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                String email = rs.getString("email");
                String phone = rs.getString("phone");
                String joinDate = rs.getString("join_date");
                String status = rs.getString("status");

                // Parse and format the date if it's not null
                // String formattedDate = "";
                // if (joinDate != null && !joinDate.isEmpty()) {
                //     try {
                //         formattedDate = dateFormat.format(DateUtils.parseSQLiteDate(joinDate));
                //     } catch (Exception e) {
                //         formattedDate = joinDate; // Use as-is if parsing fails
                //     }
                // }

                memberTableModel.addRow(new Object[] {
                        memberId,
                        firstName,
                        lastName,
                        email,
                        phone,
                     //   formattedDate,
                        status
                });
            }

            rs.close();
            stmt.close();
            conn.close();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error loading members: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadReservations() {
        reservationTableModel.setRowCount(0);

        try {
            Connection conn = dbConnection.getConnection();

            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("SELECT r.reservation_id, c.court_number, m.first_name, m.last_name, ")
                    .append("r.reservation_date, r.start_time, r.end_time, r.reservation_type, ")
                    .append("(SELECT COUNT(*) FROM ReservationParticipants WHERE reservation_id = r.reservation_id) AS player_count, ")
                    .append("r.created_at ")
                    .append("FROM Reservations r ")
                    .append("JOIN Courts c ON r.court_id = c.court_id ")
                    .append("JOIN Members m ON r.member_id = m.member_id ")
                    .append("WHERE 1=1 ");

            // Add search condition
            String searchTerm = reservationSearchField.getText().trim();
            if (!searchTerm.isEmpty()) {
                queryBuilder.append("AND (m.first_name LIKE ? OR m.last_name LIKE ?) ");
            }

            // Add date range condition
            String dateRange = (String) dateRangeComboBox.getSelectedItem();
            switch (dateRange) {
                case "Today":
                    queryBuilder.append("AND r.reservation_date = date('now') ");
                    break;
                case "Tomorrow":
                    queryBuilder.append("AND r.reservation_date = date('now', '+1 day') ");
                    break;
                case "This Week":
                    queryBuilder.append("AND r.reservation_date BETWEEN date('now') AND date('now', '+7 day') ");
                    break;
                case "Next Week":
                    queryBuilder
                            .append("AND r.reservation_date BETWEEN date('now', '+7 day') AND date('now', '+14 day') ");
                    break;
                case "This Month":
                    queryBuilder.append("AND strftime('%Y-%m', r.reservation_date) = strftime('%Y-%m', 'now') ");
                    break;
                case "All Upcoming":
                    queryBuilder.append("AND r.reservation_date >= date('now') ");
                    break;
            }

            // Add ordering
            queryBuilder.append("ORDER BY r.reservation_date, r.start_time");

            PreparedStatement stmt = conn.prepareStatement(queryBuilder.toString());

            // Set search parameters if needed
            if (!searchTerm.isEmpty()) {
                String likePattern = "%" + searchTerm + "%";
                stmt.setString(1, likePattern);
                stmt.setString(2, likePattern);
            }

            ResultSet rs = stmt.executeQuery();

            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
            SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");
            SimpleDateFormat createdFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");

            while (rs.next()) {
                int reservationId = rs.getInt("reservation_id");
                int courtNumber = rs.getInt("court_number");
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                String reservationDate = rs.getString("reservation_date");
                String startTime = rs.getString("start_time");
                String endTime = rs.getString("end_time");
                String type = rs.getString("reservation_type");
                int playerCount = rs.getInt("player_count");
                String createdAt = rs.getString("created_at");

                // Format the date and times
                String formattedDate = "";
                String formattedTimeRange = "";
                String formattedCreatedAt = "";

                // try {
                //     if (reservationDate != null) {
                //         formattedDate = dateFormat.format(DateUtils.parseSQLiteDate(reservationDate));
                //     }

                //     if (startTime != null && endTime != null) {
                //         Date startTimeDate = DateUtils.parseSQLiteTime(startTime);
                //         Date endTimeDate = DateUtils.parseSQLiteTime(endTime);
                //         formattedTimeRange = timeFormat.format(startTimeDate) + " - " + timeFormat.format(endTimeDate);
                //     }

                //     if (createdAt != null) {
                //         formattedCreatedAt = createdFormat.format(DateUtils.parseSQLiteDateTime(createdAt));
                //     }
                // } catch (Exception e) {
                //     // Use raw values if parsing fails
                //     formattedDate = reservationDate;
                //     formattedTimeRange = startTime + " - " + endTime;
                //     formattedCreatedAt = createdAt;
                // }

                reservationTableModel.addRow(new Object[] {
                        reservationId,
                        "Court " + courtNumber,
                        firstName + " " + lastName,
                        formattedDate,
                        formattedTimeRange,
                        type,
                        playerCount + " players",
                        formattedCreatedAt
                });
            }

            rs.close();
            stmt.close();
            conn.close();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error loading reservations: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadCourtUsageData(String timeRange) {
        DefaultTableModel model = (DefaultTableModel) ((JTable) ((JScrollPane) courtUsagePanel.getComponent(1))
                .getViewport().getView()).getModel();
        model.setRowCount(0);

        try {
            Connection conn = dbConnection.getConnection();

            // Build date range clause based on selected time range
            String dateRangeClause = "";
            switch (timeRange) {
                case "This Week":
                    dateRangeClause = "r.reservation_date BETWEEN date('now', 'weekday 0', '-7 days') AND date('now', 'weekday 0', '+6 days')";
                    break;
                case "Last Week":
                    dateRangeClause = "r.reservation_date BETWEEN date('now', 'weekday 0', '-14 days') AND date('now', 'weekday 0', '-1 days')";
                    break;
                case "This Month":
                    dateRangeClause = "strftime('%Y-%m', r.reservation_date) = strftime('%Y-%m', 'now')";
                    break;
                case "Last Month":
                    dateRangeClause = "strftime('%Y-%m', r.reservation_date) = strftime('%Y-%m', 'now', '-1 month')";
                    break;
                case "Year to Date":
                    dateRangeClause = "strftime('%Y', r.reservation_date) = strftime('%Y', 'now') AND r.reservation_date <= date('now')";
                    break;
            }

            String query = "SELECT c.court_number, " +
                    "SUM((" +
                    "  (CAST(substr(r.end_time, 1, 2) AS INTEGER) * 60 + CAST(substr(r.end_time, 4, 2) AS INTEGER)) - "
                    +
                    "  (CAST(substr(r.start_time, 1, 2) AS INTEGER) * 60 + CAST(substr(r.start_time, 4, 2) AS INTEGER))"
                    +
                    ") / 60.0) AS total_hours, " +
                    "SUM(CASE WHEN r.reservation_type = 'SINGLES' THEN 1 ELSE 0 END) AS singles_count, " +
                    "SUM(CASE WHEN r.reservation_type = 'DOUBLES' THEN 1 ELSE 0 END) AS doubles_count " +
                    "FROM Reservations r " +
                    "JOIN Courts c ON r.court_id = c.court_id " +
                    "WHERE " + dateRangeClause + " " +
                    "GROUP BY c.court_number " +
                    "ORDER BY c.court_number";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            // Calculate maximum possible hours based on time range
            double maxHoursPerDay = 14.0; // 7am to 9pm
            double maxPossibleHours = 0.0;

            switch (timeRange) {
                case "This Week":
                case "Last Week":
                    maxPossibleHours = 7 * maxHoursPerDay;
                    break;
                case "This Month":
                case "Last Month":
                    maxPossibleHours = 30 * maxHoursPerDay; // Approximate
                    break;
                case "Year to Date":
                    // Get day of year
                    Calendar cal = Calendar.getInstance();
                    int dayOfYear = cal.get(Calendar.DAY_OF_YEAR);
                    maxPossibleHours = dayOfYear * maxHoursPerDay;
                    break;
            }

            while (rs.next()) {
                int courtNumber = rs.getInt("court_number");
                double totalHours = rs.getDouble("total_hours");
                int singlesCount = rs.getInt("singles_count");
                int doublesCount = rs.getInt("doubles_count");

                double utilization = (totalHours / maxPossibleHours) * 100;

                model.addRow(new Object[] {
                        "Court " + courtNumber,
                        String.format("%.1f", totalHours),
                        String.format("%.1f%%", utilization),
                        singlesCount,
                        doublesCount
                });
            }

            // Add a row for averages
            if (model.getRowCount() > 0) {
                double avgHours = 0;
                double avgUtilization = 0;
                int totalSingles = 0;
                int totalDoubles = 0;

                for (int i = 0; i < model.getRowCount(); i++) {
                    avgHours += Double.parseDouble(((String) model.getValueAt(i, 1)).replace(",", ""));
                    avgUtilization += Double.parseDouble(((String) model.getValueAt(i, 2)).replace("%", ""));
                    totalSingles += (int) model.getValueAt(i, 3);
                    totalDoubles += (int) model.getValueAt(i, 4);
                }

                avgHours /= model.getRowCount();
                avgUtilization /= model.getRowCount();

                model.addRow(new Object[] {
                        "AVERAGE",
                        String.format("%.1f", avgHours),
                        String.format("%.1f%%", avgUtilization),
                        totalSingles,
                        totalDoubles
                });
            }

            rs.close();
            stmt.close();
            conn.close();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error loading court usage data: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadMembershipStatistics(JTextArea statsArea) {
        try {
            Connection conn = dbConnection.getConnection();

            StringBuilder statistics = new StringBuilder();

            // Total active members
            String activeQuery = "SELECT COUNT(*) AS count FROM Members WHERE status = 'ACTIVE'";
            Statement activeStmt = conn.createStatement();
            ResultSet activeRs = activeStmt.executeQuery(activeQuery);

            if (activeRs.next()) {
                int activeCount = activeRs.getInt("count");
                statistics.append("Total Active Members: ").append(activeCount).append("\n\n");
            }

            activeRs.close();
            activeStmt.close();

            // Members with late payments
            String lateQuery = "SELECT COUNT(*) AS count FROM Members WHERE status = 'LATE_PAYMENT'";
            Statement lateStmt = conn.createStatement();
            ResultSet lateRs = lateStmt.executeQuery(lateQuery);

            if (lateRs.next()) {
                int lateCount = lateRs.getInt("count");
                statistics.append("Members with Late Payments: ").append(lateCount).append("\n\n");
            }

            lateRs.close();
            lateStmt.close();

            // New members in the past 30 days
            String newMembersQuery = "SELECT COUNT(*) AS count FROM Members WHERE join_date >= date('now', '-30 days')";
            Statement newMembersStmt = conn.createStatement();
            ResultSet newMembersRs = newMembersStmt.executeQuery(newMembersQuery);

            if (newMembersRs.next()) {
                int newCount = newMembersRs.getInt("count");
                statistics.append("New Members (Last 30 Days): ").append(newCount).append("\n\n");
            }

            newMembersRs.close();
            newMembersStmt.close();

            // Members by join year - FULLY FIXED QUERY
            String joinYearQuery = "SELECT strftime('%Y', join_date) AS year, COUNT(*) AS count FROM Members " +
                    "GROUP BY strftime('%Y', join_date) ORDER BY year DESC LIMIT 5";
            Statement joinYearStmt = conn.createStatement();
            ResultSet joinYearRs = joinYearStmt.executeQuery(joinYearQuery);

            statistics.append("Members by Join Year:\n");
            statistics.append("-----------------------\n");

            while (joinYearRs.next()) {
                String year = joinYearRs.getString("year");
                int count = joinYearRs.getInt("count");

                statistics.append(String.format("%-6s: %d members\n", year, count));
            }

            joinYearRs.close();
            joinYearStmt.close();

            statistics.append("\n");

            // Top members by court usage
            String topUsersQuery = "SELECT m.member_id, m.first_name, m.last_name, COUNT(r.reservation_id) AS reservation_count "
                    +
                    "FROM Members m " +
                    "JOIN Reservations r ON m.member_id = r.member_id " +
                    "WHERE r.reservation_date >= date('now', '-90 days') " +
                    "GROUP BY m.member_id, m.first_name, m.last_name " +
                    "ORDER BY reservation_count DESC " +
                    "LIMIT 5";

            Statement topUsersStmt = conn.createStatement();
            ResultSet topUsersRs = topUsersStmt.executeQuery(topUsersQuery);

            statistics.append("Top 5 Members by Court Usage (Last 90 Days):\n");
            statistics.append("------------------------------------------\n");

            while (topUsersRs.next()) {
                String firstName = topUsersRs.getString("first_name");
                String lastName = topUsersRs.getString("last_name");
                int count = topUsersRs.getInt("reservation_count");

                statistics.append(String.format("%-20s: %d reservations\n", firstName + " " + lastName, count));
            }

            topUsersRs.close();
            topUsersStmt.close();

            conn.close();

            // Set the text in the text area
            statsArea.setText(statistics.toString());

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error loading membership statistics: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadRevenueData(String timeRange, DefaultTableModel model) {
        model.setRowCount(0);

        try {
            Connection conn = dbConnection.getConnection();

            // Build date range clause based on selected time range
            String dateRangeClause = "";
            switch (timeRange) {
                case "This Month":
                    dateRangeClause = "strftime('%Y-%m', paid_date) = strftime('%Y-%m', 'now')";
                    break;
                case "Last Month":
                    dateRangeClause = "strftime('%Y-%m', paid_date) = strftime('%Y-%m', 'now', '-1 month')";
                    break;
                case "Last 3 Months":
                    dateRangeClause = "paid_date >= date('now', '-3 month')";
                    break;
                case "Year to Date":
                    dateRangeClause = "strftime('%Y', paid_date) = strftime('%Y', 'now')";
                    break;
                case "Last Year":
                    dateRangeClause = "strftime('%Y', paid_date) = strftime('%Y', 'now', '-1 year')";
                    break;
            }

            // Get membership fees
            String membershipQuery = "SELECT 'Membership Fees' AS category, SUM(amount) AS total FROM MembershipFees WHERE is_paid = 1 AND "
                    + dateRangeClause;
            Statement membershipStmt = conn.createStatement();
            ResultSet membershipRs = membershipStmt.executeQuery(membershipQuery);

            double membershipTotal = 0;
            if (membershipRs.next()) {
                membershipTotal = membershipRs.getDouble("total");
            }

            membershipRs.close();
            membershipStmt.close();

            // Get late fees
            String lateFeesQuery = "SELECT 'Late Fees' AS category, SUM(amount) AS total FROM LateFees WHERE is_paid = 1 AND "
                    + dateRangeClause.replace("paid_date", "month_applied");
            Statement lateFeesStmt = conn.createStatement();
            ResultSet lateFeesRs = lateFeesStmt.executeQuery(lateFeesQuery);

            double lateFeesTotal = 0;
            if (lateFeesRs.next()) {
                lateFeesTotal = lateFeesRs.getDouble("total");
            }

            lateFeesRs.close();
            lateFeesStmt.close();

            // Get guest fees
            String guestFeesQuery = "SELECT 'Guest Fees' AS category, SUM(amount) AS total FROM GuestFees WHERE is_paid = 1 AND "
                    + dateRangeClause.replace("paid_date", "date_applied");
            Statement guestFeesStmt = conn.createStatement();
            ResultSet guestFeesRs = guestFeesStmt.executeQuery(guestFeesQuery);

            double guestFeesTotal = 0;
            if (guestFeesRs.next()) {
                guestFeesTotal = guestFeesRs.getDouble("total");
            }

            guestFeesRs.close();
            guestFeesStmt.close();

            conn.close();

            // Calculate total
            double grandTotal = membershipTotal + lateFeesTotal + guestFeesTotal;

            // Add rows to table
            model.addRow(new Object[] {
                    "Membership Fees",
                    String.format("$%.2f", membershipTotal),
                    String.format("%.1f%%", grandTotal > 0 ? (membershipTotal / grandTotal * 100) : 0)
            });

            model.addRow(new Object[] {
                    "Late Fees",
                    String.format("$%.2f", lateFeesTotal),
                    String.format("%.1f%%", grandTotal > 0 ? (lateFeesTotal / grandTotal * 100) : 0)
            });

            model.addRow(new Object[] {
                    "Guest Fees",
                    String.format("$%.2f", guestFeesTotal),
                    String.format("%.1f%%", grandTotal > 0 ? (guestFeesTotal / grandTotal * 100) : 0)
            });

            model.addRow(new Object[] {
                    "TOTAL",
                    String.format("$%.2f", grandTotal),
                    "100.0%"
            });

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error loading revenue data: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showMemberHistoryDialog(int memberId) {
        try {
            Connection conn = dbConnection.getConnection();

            // Get member name
            String nameQuery = "SELECT first_name, last_name FROM Members WHERE member_id = ?";
            PreparedStatement nameStmt = conn.prepareStatement(nameQuery);
            nameStmt.setInt(1, memberId);

            ResultSet nameRs = nameStmt.executeQuery();

            String memberName = "";
            if (nameRs.next()) {
                memberName = nameRs.getString("first_name") + " " + nameRs.getString("last_name");
            }

            nameRs.close();
            nameStmt.close();

            // Create dialog
            JDialog dialog = new JDialog(this, "Member History - " + memberName, true);
            dialog.setSize(600, 400);
            dialog.setLocationRelativeTo(this);

            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            // History table
            String[] columns = { "Start Date", "End Date", "Status", "Reason" };
            DefaultTableModel model = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            JTable table = new JTable(model);
            JScrollPane scrollPane = new JScrollPane(table);

            panel.add(scrollPane, BorderLayout.CENTER);

            // Get member history
            String historyQuery = "SELECT start_date, end_date, " +
                    "(CASE WHEN end_date IS NULL THEN 'Active' ELSE 'Inactive' END) AS status, " +
                    "reason FROM MemberHistory " +
                    "WHERE member_id = ? ORDER BY start_date DESC";

            PreparedStatement historyStmt = conn.prepareStatement(historyQuery);
            historyStmt.setInt(1, memberId);

            ResultSet historyRs = historyStmt.executeQuery();

            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

            while (historyRs.next()) {
                Date startDate = historyRs.getDate("start_date");
                Date endDate = historyRs.getDate("end_date");
                String status = historyRs.getString("status");
                String reason = historyRs.getString("reason");

                model.addRow(new Object[] {
                        startDate != null ? dateFormat.format(startDate) : "",
                        endDate != null ? dateFormat.format(endDate) : "Current",
                        status,
                        reason
                });
            }

            historyRs.close();
            historyStmt.close();

            // Button panel
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton closeButton = new JButton("Close");
            closeButton.addActionListener(e -> dialog.dispose());
            buttonPanel.add(closeButton);

            panel.add(buttonPanel, BorderLayout.SOUTH);

            dialog.getContentPane().add(panel);
            dialog.setVisible(true);

            conn.close();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error loading member history: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deactivateMember(int memberId) {
        try {
            Connection conn = dbConnection.getConnection();
            conn.setAutoCommit(false);

            try {
                // Update member status
                String updateQuery = "UPDATE Members SET status = 'INACTIVE' WHERE member_id = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                updateStmt.setInt(1, memberId);
                updateStmt.executeUpdate();
                updateStmt.close();

                // Close current history record
                String closeHistoryQuery = "UPDATE MemberHistory SET end_date = date('now') " +
                        "WHERE member_id = ? AND end_date IS NULL";
                PreparedStatement closeHistoryStmt = conn.prepareStatement(closeHistoryQuery);
                closeHistoryStmt.setInt(1, memberId);
                closeHistoryStmt.executeUpdate();
                closeHistoryStmt.close();

                // Add reason for deactivation
                String reason = JOptionPane.showInputDialog(this,
                        "Reason for deactivation:",
                        "Deactivation Reason",
                        JOptionPane.QUESTION_MESSAGE);

                if (reason != null) {
                    String updateReasonQuery = "UPDATE MemberHistory SET reason = ? " +
                            "WHERE member_id = ? AND end_date = date('now')";
                    PreparedStatement updateReasonStmt = conn.prepareStatement(updateReasonQuery);
                    updateReasonStmt.setString(1, reason);
                    updateReasonStmt.setInt(2, memberId);
                    updateReasonStmt.executeUpdate();
                    updateReasonStmt.close();
                }

                conn.commit();

                JOptionPane.showMessageDialog(this,
                        "Member deactivated successfully.",
                        "Deactivation Complete",
                        JOptionPane.INFORMATION_MESSAGE);

                // Reload members
                loadMembers();

            } catch (SQLException ex) {
                conn.rollback();
                JOptionPane.showMessageDialog(this,
                        "Error deactivating member: " + ex.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
            } finally {
                conn.setAutoCommit(true);
                conn.close();
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error connecting to database: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showReservationDetailsDialog(int reservationId) {
        try {
            Connection conn = dbConnection.getConnection();

            // Get reservation details
            String query = "SELECT r.reservation_id, c.court_number, m.first_name, m.last_name, " +
                    "r.reservation_date, r.start_time, r.end_time, r.reservation_type, r.created_at " +
                    "FROM Reservations r " +
                    "JOIN Courts c ON r.court_id = c.court_id " +
                    "JOIN Members m ON r.member_id = m.member_id " +
                    "WHERE r.reservation_id = ?";

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, reservationId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int courtNumber = rs.getInt("court_number");
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                Date reservationDate = rs.getDate("reservation_date");
                Time startTime = rs.getTime("start_time");
                Time endTime = rs.getTime("end_time");
                String type = rs.getString("reservation_type");
                Timestamp createdAt = rs.getTimestamp("created_at");

                // Create dialog
                JDialog dialog = new JDialog(this, "Reservation Details - #" + reservationId, true);
                dialog.setSize(600, 400);
                dialog.setLocationRelativeTo(this);

                JPanel panel = new JPanel(new BorderLayout(10, 10));
                panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                // Details panel
                JPanel detailsPanel = new JPanel(new GridLayout(6, 2, 5, 5));
                detailsPanel.setBorder(BorderFactory.createTitledBorder("Reservation Information"));

                SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
                SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");
                SimpleDateFormat createdFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

                detailsPanel.add(new JLabel("Court:"));
                detailsPanel.add(new JLabel("Court " + courtNumber));

                detailsPanel.add(new JLabel("Primary Member:"));
                detailsPanel.add(new JLabel(firstName + " " + lastName));

                detailsPanel.add(new JLabel("Date:"));
                detailsPanel.add(new JLabel(dateFormat.format(reservationDate)));

                detailsPanel.add(new JLabel("Time:"));
                detailsPanel.add(new JLabel(timeFormat.format(startTime) + " - " + timeFormat.format(endTime)));

                detailsPanel.add(new JLabel("Type:"));
                detailsPanel.add(new JLabel(type));

                detailsPanel.add(new JLabel("Created:"));
                detailsPanel.add(new JLabel(createdFormat.format(createdAt)));

                panel.add(detailsPanel, BorderLayout.NORTH);

                // Participants table
                String[] columns = { "Type", "Name", "Email" };
                DefaultTableModel model = new DefaultTableModel(columns, 0) {
                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                };

                JTable table = new JTable(model);
                JScrollPane scrollPane = new JScrollPane(table);

                panel.add(scrollPane, BorderLayout.CENTER);

                // Get participants
                String participantsQuery = "SELECT 'Member' AS type, m.first_name, m.last_name, m.email " +
                        "FROM ReservationParticipants rp " +
                        "JOIN Members m ON rp.member_id = m.member_id " +
                        "WHERE rp.reservation_id = ? AND rp.member_id IS NOT NULL " +
                        "UNION " +
                        "SELECT 'Guest' AS type, g.first_name, g.last_name, g.email " +
                        "FROM ReservationParticipants rp " +
                        "JOIN Guests g ON rp.guest_id = g.guest_id " +
                        "WHERE rp.reservation_id = ? AND rp.guest_id IS NOT NULL";

                PreparedStatement participantsStmt = conn.prepareStatement(participantsQuery);
                participantsStmt.setInt(1, reservationId);
                participantsStmt.setInt(2, reservationId);

                ResultSet participantsRs = participantsStmt.executeQuery();

                while (participantsRs.next()) {
                    String participantType = participantsRs.getString("type");
                    String participantFirstName = participantsRs.getString("first_name");
                    String participantLastName = participantsRs.getString("last_name");
                    String participantEmail = participantsRs.getString("email");

                    model.addRow(new Object[] {
                            participantType,
                            participantFirstName + " " + participantLastName,
                            participantEmail
                    });
                }

                participantsRs.close();
                participantsStmt.close();

                // Button panel
                JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                JButton closeButton = new JButton("Close");
                closeButton.addActionListener(e -> dialog.dispose());
                buttonPanel.add(closeButton);

                panel.add(buttonPanel, BorderLayout.SOUTH);

                dialog.getContentPane().add(panel);
                dialog.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Reservation not found.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }

            rs.close();
            stmt.close();
            conn.close();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error loading reservation details: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cancelReservation(int reservationId) {
        try {
            Connection conn = dbConnection.getConnection();
            conn.setAutoCommit(false);

            try {
                // Delete reservation participants first
                String deleteParticipantsQuery = "DELETE FROM ReservationParticipants WHERE reservation_id = ?";
                PreparedStatement deleteParticipantsStmt = conn.prepareStatement(deleteParticipantsQuery);
                deleteParticipantsStmt.setInt(1, reservationId);
                deleteParticipantsStmt.executeUpdate();
                deleteParticipantsStmt.close();

                // Then delete the reservation
                String deleteQuery = "DELETE FROM Reservations WHERE reservation_id = ?";
                PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery);
                deleteStmt.setInt(1, reservationId);
                deleteStmt.executeUpdate();
                deleteStmt.close();

                conn.commit();

                JOptionPane.showMessageDialog(this,
                        "Reservation cancelled successfully.",
                        "Reservation Cancelled",
                        JOptionPane.INFORMATION_MESSAGE);

                // Reload reservations
                loadReservations();

            } catch (SQLException ex) {
                conn.rollback();
                JOptionPane.showMessageDialog(this,
                        "Error cancelling reservation: " + ex.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
            } finally {
                conn.setAutoCommit(true);
                conn.close();
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error connecting to database: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
