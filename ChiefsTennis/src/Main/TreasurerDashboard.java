package Main;

import javax.swing.*;

import javax.swing.table.*;

import Main.DatabaseConnection;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.math.BigDecimal;

public class TreasurerDashboard extends JFrame {
    private User currentUser;
    private DatabaseConnection dbConnection;
    
    private JTabbedPane tabbedPane;
    private JPanel billingPanel;
    private JPanel lateFeesPanel;
    private JPanel financialReportsPanel;
    
    // Billing components
    private JTable billsTable;
    private DefaultTableModel billsTableModel;
    private JButton generateAnnualBillsButton;
    private JButton sendReminderButton;
    private JButton viewBillDetailsButton;
    private JButton markAsPaidButton;
    private JTextField billSearchField;
    private JButton billSearchButton;
    private JComboBox<String> billFilterComboBox;
    
    // Late fees components
    private JTable lateFeesTable;
    private DefaultTableModel lateFeesTableModel;
    private JButton applyLateFeesButton;
    private JButton sendLateFeesNoticeButton;
    private JComboBox<String> monthComboBox;
    private JSpinner yearSpinner;
    
    // Financial reports components
    private JPanel revenuePanel;
    private JPanel accountsReceivablePanel;
    private JPanel memberBalancesPanel;
    
    public TreasurerDashboard(User user, DatabaseConnection dbConnection) {
        this.currentUser = user;
        this.dbConnection = dbConnection;
        
        initComponents();
        setupLayout();
        setupListeners();
        loadData();
        
        setTitle("Tennis Club - Treasurer Dashboard");
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    
    // If there's an existing constructor, modify it or call the new one
    public TreasurerDashboard(User user) {
        this(user, new DatabaseConnection());
    }
    
    private void initComponents() {
        tabbedPane = new JTabbedPane();
        
        // Initialize billing components
        String[] billColumns = {"Bill ID", "Member", "Date", "Amount", "Due Date", "Status", "Email Sent"};
        billsTableModel = new DefaultTableModel(billColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        billsTable = new JTable(billsTableModel);
        billsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        billsTable.setAutoCreateRowSorter(true);
        
        generateAnnualBillsButton = new JButton("Generate Annual Bills");
        sendReminderButton = new JButton("Send Reminder");
        viewBillDetailsButton = new JButton("View Details");
        markAsPaidButton = new JButton("Mark as Paid");
        
        // Disable buttons until a row is selected
        sendReminderButton.setEnabled(false);
        viewBillDetailsButton.setEnabled(false);
        markAsPaidButton.setEnabled(false);
        
        billSearchField = new JTextField(20);
        billSearchButton = new JButton("Search");
        
        String[] billFilterOptions = {"All Bills", "Unpaid Bills", "Paid Bills", "Overdue Bills"};
        billFilterComboBox = new JComboBox<>(billFilterOptions);
        
        // Initialize late fees components
        String[] lateFeesColumns = {"Member ID", "Member", "Original Due Date", "Amount Due", "Months Late", "Late Fees", "Total Due"};
        lateFeesTableModel = new DefaultTableModel(lateFeesColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        lateFeesTable = new JTable(lateFeesTableModel);
        lateFeesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lateFeesTable.setAutoCreateRowSorter(true);
        
        applyLateFeesButton = new JButton("Apply Late Fees");
        sendLateFeesNoticeButton = new JButton("Send Notice");
        
        // Disable send notice button until a row is selected
        sendLateFeesNoticeButton.setEnabled(false);
        
        String[] months = {"January", "February", "March", "April", "May", "June", 
                          "July", "August", "September", "October", "November", "December"};
        monthComboBox = new JComboBox<>(months);
        
        Calendar cal = Calendar.getInstance();
        SpinnerNumberModel yearModel = new SpinnerNumberModel(cal.get(Calendar.YEAR), 2000, 2100, 1);
        yearSpinner = new JSpinner(yearModel);
        
        // Initialize financial reports components
        revenuePanel = new JPanel(new BorderLayout());
        revenuePanel.setBorder(BorderFactory.createTitledBorder("Revenue"));
        
        accountsReceivablePanel = new JPanel(new BorderLayout());
        accountsReceivablePanel.setBorder(BorderFactory.createTitledBorder("Accounts Receivable"));
        
        memberBalancesPanel = new JPanel(new BorderLayout());
        memberBalancesPanel.setBorder(BorderFactory.createTitledBorder("Member Balances"));
    }
    
    private void setupLayout() {
        // Billing tab
        billingPanel = new JPanel(new BorderLayout(10, 10));
        billingPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel billTopPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        billTopPanel.add(new JLabel("Search:"));
        billTopPanel.add(billSearchField);
        billTopPanel.add(billSearchButton);
        billTopPanel.add(Box.createHorizontalStrut(20));
        billTopPanel.add(new JLabel("Filter:"));
        billTopPanel.add(billFilterComboBox);
        
        billingPanel.add(billTopPanel, BorderLayout.NORTH);
        
        JScrollPane billScrollPane = new JScrollPane(billsTable);
        billingPanel.add(billScrollPane, BorderLayout.CENTER);
        
        JPanel billButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        billButtonPanel.add(generateAnnualBillsButton);
        billButtonPanel.add(sendReminderButton);
        billButtonPanel.add(viewBillDetailsButton);
        billButtonPanel.add(markAsPaidButton);
        
        billingPanel.add(billButtonPanel, BorderLayout.SOUTH);
        
        // Late fees tab
        lateFeesPanel = new JPanel(new BorderLayout(10, 10));
        lateFeesPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel lateFeesTopPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lateFeesTopPanel.add(new JLabel("Month:"));
        lateFeesTopPanel.add(monthComboBox);
        lateFeesTopPanel.add(Box.createHorizontalStrut(10));
        lateFeesTopPanel.add(new JLabel("Year:"));
        lateFeesTopPanel.add(yearSpinner);
        
        lateFeesPanel.add(lateFeesTopPanel, BorderLayout.NORTH);
        
        JScrollPane lateFeesScrollPane = new JScrollPane(lateFeesTable);
        lateFeesPanel.add(lateFeesScrollPane, BorderLayout.CENTER);
        
        JPanel lateFeesButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        lateFeesButtonPanel.add(sendLateFeesNoticeButton);
        lateFeesButtonPanel.add(applyLateFeesButton);
        
        lateFeesPanel.add(lateFeesButtonPanel, BorderLayout.SOUTH);
        
        // Financial reports tab
        financialReportsPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        financialReportsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        setupRevenuePanel();
        setupAccountsReceivablePanel();
        setupMemberBalancesPanel();
        
        financialReportsPanel.add(revenuePanel);
        financialReportsPanel.add(accountsReceivablePanel);
        financialReportsPanel.add(memberBalancesPanel);
        
        // Add tabs to tabbed pane
        tabbedPane.addTab("Billing", billingPanel);
        tabbedPane.addTab("Late Fees", lateFeesPanel);
        tabbedPane.addTab("Financial Reports", financialReportsPanel);
        
        getContentPane().add(tabbedPane);
    }
    
    private void setupRevenuePanel() {
        revenuePanel.removeAll();
        
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        String[] timeRanges = {"This Month", "Last Month", "Year to Date", "Last Year"};
        JComboBox<String> timeRangeComboBox = new JComboBox<>(timeRanges);
        topPanel.add(new JLabel("Time Period:"));
        topPanel.add(timeRangeComboBox);
        
        revenuePanel.add(topPanel, BorderLayout.NORTH);
        
        // Revenue table
        String[] columns = {"Category", "Amount", "% of Total"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        
        revenuePanel.add(scrollPane, BorderLayout.CENTER);
        
        // Add print button
        JButton printButton = new JButton("Print Report");
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(printButton);
        
        revenuePanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Load data based on selected time range
        timeRangeComboBox.addActionListener(e -> loadRevenueData(timeRangeComboBox.getSelectedItem().toString(), model));
        
        // Print button action
        printButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, 
                "Report printing functionality would be implemented here.", 
                "Print Report", 
                JOptionPane.INFORMATION_MESSAGE);
        });
        
        // Load initial data
        loadRevenueData("This Month", model);
    }
    
    private void setupAccountsReceivablePanel() {
        accountsReceivablePanel.removeAll();
        
        // Accounts receivable aging report
        String[] columns = {"Aging Period", "Amount", "% of Total"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        
        accountsReceivablePanel.add(scrollPane, BorderLayout.CENTER);
        
        // Add print button
        JButton printButton = new JButton("Print Report");
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(printButton);
        
        accountsReceivablePanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Print button action
        printButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, 
                "Report printing functionality would be implemented here.", 
                "Print Report", 
                JOptionPane.INFORMATION_MESSAGE);
        });
        
        // Load data
        loadAccountsReceivableData(model);
    }
    
    private void setupMemberBalancesPanel() {
        memberBalancesPanel.removeAll();
        
        // Member balances table
        String[] columns = {"Member ID", "Member", "Total Balance", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        
        memberBalancesPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Add export button
        JButton exportButton = new JButton("Export to CSV");
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(exportButton);
        
        memberBalancesPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Export button action
        exportButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, 
                "CSV export functionality would be implemented here.", 
                "Export to CSV", 
                JOptionPane.INFORMATION_MESSAGE);
        });
        
        // Load data
        loadMemberBalancesData(model);
    }
    
    private void setupListeners() {
        // Bills table selection listener
        billsTable.getSelectionModel().addListSelectionListener(e -> {
            boolean rowSelected = billsTable.getSelectedRow() != -1;
            viewBillDetailsButton.setEnabled(rowSelected);
            
            if (rowSelected) {
                int modelRow = billsTable.convertRowIndexToModel(billsTable.getSelectedRow());
                String status = (String) billsTableModel.getValueAt(modelRow, 5);
                boolean isPaid = "Paid".equals(status);
                
                sendReminderButton.setEnabled(!isPaid);
                markAsPaidButton.setEnabled(!isPaid);
            } else {
                sendReminderButton.setEnabled(false);
                markAsPaidButton.setEnabled(false);
            }
        });
        
        // Late fees table selection listener
        lateFeesTable.getSelectionModel().addListSelectionListener(e -> {
            sendLateFeesNoticeButton.setEnabled(lateFeesTable.getSelectedRow() != -1);
        });
        
        // Bill search button
        billSearchButton.addActionListener(e -> loadBills());
        
        // Bill filter combo box
        billFilterComboBox.addActionListener(e -> loadBills());
        
        // Bill search field enter key
        billSearchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    loadBills();
                }
            }
        });
        
        // Generate annual bills button
        generateAnnualBillsButton.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to generate annual membership bills for all active members?",
                "Confirm Annual Billing",
                JOptionPane.YES_NO_OPTION);
            
            if (result == JOptionPane.YES_OPTION) {
                generateAnnualBills();
            }
        });
        
        // Send reminder button
        sendReminderButton.addActionListener(e -> {
            int selectedRow = billsTable.getSelectedRow();
            if (selectedRow != -1) {
                int modelRow = billsTable.convertRowIndexToModel(selectedRow);
                int billId = Integer.parseInt(billsTableModel.getValueAt(modelRow, 0).toString());
                sendBillReminder(billId);
            }
        });
        
        // View bill details button
        viewBillDetailsButton.addActionListener(e -> {
            int selectedRow = billsTable.getSelectedRow();
            if (selectedRow != -1) {
                int modelRow = billsTable.convertRowIndexToModel(selectedRow);
                int billId = Integer.parseInt(billsTableModel.getValueAt(modelRow, 0).toString());
                showBillDetailsDialog(billId);
            }
        });
        
        // Mark as paid button
        markAsPaidButton.addActionListener(e -> {
            int selectedRow = billsTable.getSelectedRow();
            if (selectedRow != -1) {
                int modelRow = billsTable.convertRowIndexToModel(selectedRow);
                int billId = Integer.parseInt(billsTableModel.getValueAt(modelRow, 0).toString());
                markBillAsPaid(billId);
            }
        });
        
        // Month and year selection for late fees
        ActionListener lateFeesDateListener = e -> loadLateFees();
        monthComboBox.addActionListener(lateFeesDateListener);
        yearSpinner.addChangeListener(e -> loadLateFees());
        
        // Apply late fees button
        applyLateFeesButton.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to apply late fees for " + 
                monthComboBox.getSelectedItem() + " " + yearSpinner.getValue() + "?",
                "Confirm Late Fees",
                JOptionPane.YES_NO_OPTION);
            
            if (result == JOptionPane.YES_OPTION) {
                applyLateFees();
            }
        });
        
        // Send late fees notice button
        sendLateFeesNoticeButton.addActionListener(e -> {
            int selectedRow = lateFeesTable.getSelectedRow();
            if (selectedRow != -1) {
                int modelRow = lateFeesTable.convertRowIndexToModel(selectedRow);
                int memberId = Integer.parseInt(lateFeesTableModel.getValueAt(modelRow, 0).toString());
                String memberName = (String) lateFeesTableModel.getValueAt(modelRow, 1);
                
                sendLateFeesNotice(memberId, memberName);
            }
        });
        
        // Tab change listener
        tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            
            switch (selectedIndex) {
                case 0:
                    loadBills();
                    break;
                case 1:
                    loadLateFees();
                    break;
                case 2:
                    // Refresh reports
                    DefaultTableModel revenueModel = (DefaultTableModel) ((JTable) ((JScrollPane) revenuePanel.getComponent(1)).getViewport().getView()).getModel();
                    loadRevenueData("This Month", revenueModel);
                    
                    DefaultTableModel arModel = (DefaultTableModel) ((JTable) ((JScrollPane) accountsReceivablePanel.getComponent(0)).getViewport().getView()).getModel();
                    loadAccountsReceivableData(arModel);
                    
                    DefaultTableModel balancesModel = (DefaultTableModel) ((JTable) ((JScrollPane) memberBalancesPanel.getComponent(0)).getViewport().getView()).getModel();
                    loadMemberBalancesData(balancesModel);
                    break;
            }
        });
    }
    
    private void loadData() {
        loadBills();
        loadLateFees();
    }
    
    private void loadBills() {
        billsTableModel.setRowCount(0);
        
        try {
            Connection conn = dbConnection.getConnection();
            
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("SELECT b.bill_id, m.first_name, m.last_name, b.bill_date, b.total_amount, ")
                       .append("b.due_date, b.is_paid, b.sent_email ")
                       .append("FROM Bills b ")
                       .append("JOIN Members m ON b.member_id = m.member_id ")
                       .append("WHERE 1=1 ");
            
            // Add filter condition
            String filter = (String) billFilterComboBox.getSelectedItem();
            if ("Unpaid Bills".equals(filter)) {
                queryBuilder.append("AND b.is_paid = 0 ");
            } else if ("Paid Bills".equals(filter)) {
                queryBuilder.append("AND b.is_paid = 1 ");
            } else if ("Overdue Bills".equals(filter)) {
                queryBuilder.append("AND b.is_paid = 0 AND b.due_date < date('now') ");
            }
            
            // Add search condition if search field is not empty
            String searchTerm = billSearchField.getText().trim();
            if (!searchTerm.isEmpty()) {
                queryBuilder.append("AND (m.first_name LIKE ? OR m.last_name LIKE ?) ");
            }
            
            // Add ordering
            queryBuilder.append("ORDER BY b.due_date DESC, m.last_name, m.first_name");
            
            PreparedStatement stmt = conn.prepareStatement(queryBuilder.toString());
            
            // Set search parameters if needed
            if (!searchTerm.isEmpty()) {
                String likePattern = "%" + searchTerm + "%";
                stmt.setString(1, likePattern);
                stmt.setString(2, likePattern);
            }
            
            ResultSet rs = stmt.executeQuery();
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
            
            while (rs.next()) {
                int billId = rs.getInt("bill_id");
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                Date billDate = rs.getDate("bill_date");
                BigDecimal totalAmount = rs.getBigDecimal("total_amount");
                Date dueDate = rs.getDate("due_date");
                boolean isPaid = rs.getBoolean("is_paid");
                boolean sentEmail = rs.getBoolean("sent_email");
                
                billsTableModel.addRow(new Object[]{
                    String.valueOf(billId),
                    firstName + " " + lastName,
                    billDate != null ? dateFormat.format(billDate) : "",
                    String.format("$%.2f", totalAmount),
                    dueDate != null ? dateFormat.format(dueDate) : "",
                    isPaid ? "Paid" : "Unpaid",
                    sentEmail ? "Yes" : "No"
                });
            }
            
            rs.close();
            stmt.close();
            conn.close();
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, 
                "Error loading bills: " + ex.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadLateFees() {
        lateFeesTableModel.setRowCount(0);
        
        try {
            Connection conn = dbConnection.getConnection();
            
            // Get selected month and year
            int selectedMonth = monthComboBox.getSelectedIndex() + 1; // 1-based month
            int selectedYear = (Integer) yearSpinner.getValue();
            
            // Query for members with late fees
            String query = "SELECT m.member_id, m.first_name, m.last_name, mf.due_date, mf.amount, " +
                          "PERIOD_DIFF(YEAR(date('now')) * 12 + MONTH(date('now')), YEAR(mf.due_date) * 12 + MONTH(mf.due_date)) AS months_late, " +
                          "COALESCE((SELECT SUM(lf.amount) FROM LateFees lf WHERE lf.member_id = m.member_id AND lf.fee_id = mf.fee_id), 0) AS late_fees " +
                          "FROM Members m " +
                          "JOIN MembershipFees mf ON m.member_id = mf.member_id " +
                          "WHERE mf.is_paid = 0 " +
                          "AND mf.due_date < DATE_FORMAT(MAKEDATE(?, ?), '%Y-%m-%d') " +
                          "ORDER BY m.last_name, m.first_name";
            
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, selectedYear);
            stmt.setInt(2, selectedMonth * 31); // Approximate day of year for the end of the month
            
            ResultSet rs = stmt.executeQuery();
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
            
            while (rs.next()) {
                int memberId = rs.getInt("member_id");
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                Date dueDate = rs.getDate("due_date");
                BigDecimal amount = rs.getBigDecimal("amount");
                int monthsLate = rs.getInt("months_late");
                BigDecimal lateFees = rs.getBigDecimal("late_fees");
                BigDecimal totalDue = amount.add(lateFees);
                
                lateFeesTableModel.addRow(new Object[]{
                    String.valueOf(memberId),
                    firstName + " " + lastName,
                    dueDate != null ? dateFormat.format(dueDate) : "",
                    String.format("$%.2f", amount),
                    String.valueOf(monthsLate),
                    String.format("$%.2f", lateFees),
                    String.format("$%.2f", totalDue)
                });
            }
            
            rs.close();
            stmt.close();
            conn.close();
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, 
                "Error loading late fees data: " + ex.getMessage(), 
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
            String membershipQuery = "SELECT 'Membership Fees' AS category, SUM(amount) AS total FROM MembershipFees WHERE is_paid = 1 AND " + dateRangeClause;
            Statement membershipStmt = conn.createStatement();
            ResultSet membershipRs = membershipStmt.executeQuery(membershipQuery);
            
            double membershipTotal = 0;
            if (membershipRs.next()) {
                membershipTotal = membershipRs.getDouble("total");
            }
            
            membershipRs.close();
            membershipStmt.close();
            
            // Get late fees
            String lateFeesQuery = "SELECT 'Late Fees' AS category, SUM(amount) AS total FROM LateFees WHERE is_paid = 1 AND " + 
                                 dateRangeClause.replace("paid_date", "month_applied");
            Statement lateFeesStmt = conn.createStatement();
            ResultSet lateFeesRs = lateFeesStmt.executeQuery(lateFeesQuery);
            
            double lateFeesTotal = 0;
            if (lateFeesRs.next()) {
                lateFeesTotal = lateFeesRs.getDouble("total");
            }
            
            lateFeesRs.close();
            lateFeesStmt.close();
            
            // Get guest fees
            String guestFeesQuery = "SELECT 'Guest Fees' AS category, SUM(amount) AS total FROM GuestFees WHERE is_paid = 1 AND " + 
                                  dateRangeClause.replace("paid_date", "date_applied");
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
            model.addRow(new Object[]{
                "Membership Fees",
                String.format("$%.2f", membershipTotal),
                String.format("%.1f%%", grandTotal > 0 ? (membershipTotal / grandTotal * 100) : 0)
            });
            
            model.addRow(new Object[]{
                "Late Fees",
                String.format("$%.2f", lateFeesTotal),
                String.format("%.1f%%", grandTotal > 0 ? (lateFeesTotal / grandTotal * 100) : 0)
            });
            
            model.addRow(new Object[]{
                "Guest Fees",
                String.format("$%.2f", guestFeesTotal),
                String.format("%.1f%%", grandTotal > 0 ? (guestFeesTotal / grandTotal * 100) : 0)
            });
            
            model.addRow(new Object[]{
                "TOTAL",
                String.format("$%.2f", grandTotal),
                "100.0%"
            });
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, 
                "Error loading revenue data: " + ex.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadAccountsReceivableData(DefaultTableModel model) {
        model.setRowCount(0);
        
        try {
            Connection conn = dbConnection.getConnection();
            
            // Define aging periods with SQLite date functions
            String[][] agingPeriods = {
                {"Current", "due_date >= date('now')"},
                {"1-30 Days", "due_date BETWEEN date('now', '-30 days') AND date('now', '-1 days')"},
                {"31-60 Days", "due_date BETWEEN date('now', '-60 days') AND date('now', '-31 days')"},
                {"61-90 Days", "due_date BETWEEN date('now', '-90 days') AND date('now', '-61 days')"},
                {"Over 90 Days", "due_date < date('now', '-90 days')"}
            };
            
            double totalReceivable = 0;
            Map<String, Double> agingAmounts = new HashMap<>();
            
            // Get total receivable amount
            String totalQuery = "SELECT SUM(total_amount) AS total FROM Bills WHERE is_paid = 0";
            Statement totalStmt = conn.createStatement();
            ResultSet totalRs = totalStmt.executeQuery(totalQuery);
            
            if (totalRs.next()) {
                totalReceivable = totalRs.getDouble("total");
            }
            
            totalRs.close();
            totalStmt.close();
            
            // Get amounts for each aging period
            for (String[] period : agingPeriods) {
                String periodName = period[0];
                String dateCondition = period[1];
                
                String query = "SELECT SUM(total_amount) AS total FROM Bills WHERE is_paid = 0 AND " + dateCondition;
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query);
                
                double amount = 0;
                if (rs.next()) {
                    amount = rs.getDouble("total");
                }
                
                agingAmounts.put(periodName, amount);
                
                rs.close();
                stmt.close();
            }
            
            conn.close();
            
            // Add rows to table
            for (String[] period : agingPeriods) {
                String periodName = period[0];
                double amount = agingAmounts.get(periodName);
                double percentage = totalReceivable > 0 ? (amount / totalReceivable * 100) : 0;
                
                model.addRow(new Object[]{
                    periodName,
                    String.format("$%.2f", amount),
                    String.format("%.1f%%", percentage)
                });
            }
            
            // Add total row
            model.addRow(new Object[]{
                "TOTAL",
                String.format("$%.2f", totalReceivable),
                "100.0%"
            });
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, 
                "Error loading accounts receivable data: " + ex.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadMemberBalancesData(DefaultTableModel model) {
        model.setRowCount(0);
        
        try {
            Connection conn = dbConnection.getConnection();
            
            String query = "SELECT m.member_id, m.first_name, m.last_name, m.status, " +
                          "COALESCE(SUM(CASE WHEN mf.is_paid = 0 THEN mf.amount ELSE 0 END), 0) + " +
                          "COALESCE(SUM(CASE WHEN lf.is_paid = 0 THEN lf.amount ELSE 0 END), 0) + " +
                          "COALESCE(SUM(CASE WHEN gf.is_paid = 0 THEN gf.amount ELSE 0 END), 0) AS total_balance " +
                          "FROM Members m " +
                          "LEFT JOIN MembershipFees mf ON m.member_id = mf.member_id " +
                          "LEFT JOIN LateFees lf ON m.member_id = lf.member_id " +
                          "LEFT JOIN GuestFees gf ON m.member_id = gf.member_id " +
                          "WHERE m.status != 'INACTIVE' " +
                          "GROUP BY m.member_id, m.first_name, m.last_name, m.status " +
                          "HAVING total_balance > 0 " +
                          "ORDER BY total_balance DESC";
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                int memberId = rs.getInt("member_id");
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                String status = rs.getString("status");
                double totalBalance = rs.getDouble("total_balance");
                
                model.addRow(new Object[]{
                    String.valueOf(memberId),
                    firstName + " " + lastName,
                    String.format("$%.2f", totalBalance),
                    status
                });
            }
            
            rs.close();
            stmt.close();
            conn.close();
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, 
                "Error loading member balances: " + ex.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void generateAnnualBills() {
        try {
            Connection conn = dbConnection.getConnection();
            conn.setAutoCommit(false);
            
            try {
                // Get current date
                Calendar cal = Calendar.getInstance();
                int currentMonth = cal.get(Calendar.MONTH) + 1; // 1-based month
                int currentYear = cal.get(Calendar.YEAR);
                
                // Only allow generating annual bills in February
                if (currentMonth != 2) {
                    JOptionPane.showMessageDialog(this, 
                        "Annual bills can only be generated in February.", 
                        "Billing Error", 
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                // Check if annual bills have already been generated this year
                String checkQuery = "SELECT COUNT(*) FROM Bills WHERE YEAR(bill_date) = ? AND bill_date BETWEEN ? AND ?";
                PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
                checkStmt.setInt(1, currentYear);
                checkStmt.setDate(2, java.sql.Date.valueOf(currentYear + "-02-01"));
                checkStmt.setDate(3, java.sql.Date.valueOf(currentYear + "-02-28"));
                
                ResultSet checkRs = checkStmt.executeQuery();
                checkRs.next();
                int billCount = checkRs.getInt(1);
                
                checkRs.close();
                checkStmt.close();
                
                if (billCount > 0) {
                    JOptionPane.showMessageDialog(this, 
                        "Annual bills have already been generated for this year.", 
                        "Billing Error", 
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                // Get all active members
                String memberQuery = "SELECT member_id FROM Members WHERE status = 'ACTIVE'";
                Statement memberStmt = conn.createStatement();
                ResultSet memberRs = memberStmt.executeQuery(memberQuery);
                
                int billsGenerated = 0;
                
                while (memberRs.next()) {
                    int memberId = memberRs.getInt("member_id");
                    
                    // Create membership fee for current year
                    String feeQuery = "INSERT INTO MembershipFees (member_id, fee_year, amount, due_date, is_paid) " +
                                     "VALUES (?, ?, 400.00, ?, FALSE)";
                    
                    PreparedStatement feeStmt = conn.prepareStatement(feeQuery, Statement.RETURN_GENERATED_KEYS);
                    feeStmt.setInt(1, memberId);
                    feeStmt.setInt(2, currentYear);
                    
                    // Set due date to March 1st
                    feeStmt.setDate(3, java.sql.Date.valueOf(currentYear + "-03-01"));
                    
                    feeStmt.executeUpdate();
                    
                    ResultSet feeKeys = feeStmt.getGeneratedKeys();
                    if (feeKeys.next()) {
                        int feeId = feeKeys.getInt(1);
                        
                        // Create bill
                        String billQuery = "INSERT INTO Bills (member_id, bill_date, total_amount, due_date, is_paid, sent_email) " +
                                         "VALUES (?, date('now'), 400.00, ?, FALSE, FALSE)";
                        
                        PreparedStatement billStmt = conn.prepareStatement(billQuery, Statement.RETURN_GENERATED_KEYS);
                        billStmt.setInt(1, memberId);
                        billStmt.setDate(2, java.sql.Date.valueOf(currentYear + "-03-01"));
                        
                        billStmt.executeUpdate();
                        
                        ResultSet billKeys = billStmt.getGeneratedKeys();
                        if (billKeys.next()) {
                            int billId = billKeys.getInt(1);
                            
                            // Add bill item for membership fee
                            String itemQuery = "INSERT INTO BillItems (bill_id, description, amount, item_type, reference_id) " +
                                             "VALUES (?, ?, 400.00, 'MEMBERSHIP_FEE', ?)";
                            
                            PreparedStatement itemStmt = conn.prepareStatement(itemQuery);
                            itemStmt.setInt(1, billId);
                            itemStmt.setString(2, "Annual Membership Fee " + currentYear);
                            itemStmt.setInt(3, feeId);
                            
                            itemStmt.executeUpdate();
                            itemStmt.close();
                            
                            billsGenerated++;
                        }
                        
                        billKeys.close();
                        billStmt.close();
                    }
                    
                    feeKeys.close();
                    feeStmt.close();
                }
                
                memberRs.close();
                memberStmt.close();
                
                conn.commit();
                
                JOptionPane.showMessageDialog(this, 
                    "Generated " + billsGenerated + " annual bills.", 
                    "Bills Generated", 
                    JOptionPane.INFORMATION_MESSAGE);
                
                // Refresh the bills table
                loadBills();
                
            } catch (SQLException ex) {
                conn.rollback();
                JOptionPane.showMessageDialog(this, 
                    "Error generating annual bills: " + ex.getMessage(), 
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
    
    private void sendBillReminder(int billId) {
        try {
            Connection conn = dbConnection.getConnection();
            
            // Get bill details
            String billQuery = "SELECT b.bill_id, m.first_name, m.last_name, m.email, b.total_amount, b.due_date " +
                             "FROM Bills b " +
                             "JOIN Members m ON b.member_id = m.member_id " +
                             "WHERE b.bill_id = ?";
            
            PreparedStatement billStmt = conn.prepareStatement(billQuery);
            billStmt.setInt(1, billId);
            
            ResultSet billRs = billStmt.executeQuery();
            
            if (billRs.next()) {
                String firstName = billRs.getString("first_name");
                String lastName = billRs.getString("last_name");
                String email = billRs.getString("email");
                BigDecimal amount = billRs.getBigDecimal("total_amount");
                Date dueDate = billRs.getDate("due_date");
                
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");
                
                // In a real application, this would send an actual email
                // For this example, we'll just simulate it
                
                StringBuilder emailContent = new StringBuilder();
                emailContent.append("Dear ").append(firstName).append(" ").append(lastName).append(",\n\n");
                emailContent.append("This is a reminder that your payment of $").append(amount).append(" is due on ");
                emailContent.append(dateFormat.format(dueDate)).append(".\n\n");
                emailContent.append("Please log in to your account to make a payment.\n\n");
                emailContent.append("Thank you,\nTennis Club Billing Department");
                
                // Show the email content in a dialog (for demonstration)
                JTextArea textArea = new JTextArea(emailContent.toString());
                textArea.setEditable(false);
                textArea.setLineWrap(true);
                textArea.setWrapStyleWord(true);
                
                JScrollPane scrollPane = new JScrollPane(textArea);
                scrollPane.setPreferredSize(new Dimension(400, 300));
                
                JOptionPane.showMessageDialog(this, 
                    scrollPane, 
                    "Email Reminder to " + email, 
                    JOptionPane.INFORMATION_MESSAGE);
                
                // Mark the bill as having been emailed
                String updateQuery = "UPDATE Bills SET sent_email = TRUE WHERE bill_id = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                updateStmt.setInt(1, billId);
                updateStmt.executeUpdate();
                updateStmt.close();
                
                // Refresh the bills table
                loadBills();
            }
            
            billRs.close();
            billStmt.close();
            conn.close();
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, 
                "Error sending reminder: " + ex.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showBillDetailsDialog(int billId) {
        try {
            Connection conn = dbConnection.getConnection();
            
            // Get bill header information
            String billQuery = "SELECT b.bill_id, m.first_name, m.last_name, b.bill_date, b.total_amount, " +
                             "b.due_date, b.is_paid " +
                             "FROM Bills b " +
                             "JOIN Members m ON b.member_id = m.member_id " +
                             "WHERE b.bill_id = ?";
            
            PreparedStatement billStmt = conn.prepareStatement(billQuery);
            billStmt.setInt(1, billId);
            
            ResultSet billRs = billStmt.executeQuery();
            
            if (billRs.next()) {
                String memberName = billRs.getString("first_name") + " " + billRs.getString("last_name");
                Date billDate = billRs.getDate("bill_date");
                BigDecimal totalAmount = billRs.getBigDecimal("total_amount");
                Date dueDate = billRs.getDate("due_date");
                boolean isPaid = billRs.getBoolean("is_paid");
                
                // Create dialog
                JDialog dialog = new JDialog(this, "Bill Details - #" + billId, true);
                dialog.setSize(600, 400);
                dialog.setLocationRelativeTo(this);
                
                JPanel panel = new JPanel(new BorderLayout(10, 10));
                panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                
                // Header panel
                JPanel headerPanel = new JPanel(new GridLayout(5, 2, 5, 5));
                headerPanel.setBorder(BorderFactory.createTitledBorder("Bill Information"));
                
                SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
                
                headerPanel.add(new JLabel("Member:"));
                headerPanel.add(new JLabel(memberName));
                
                headerPanel.add(new JLabel("Bill Date:"));
                headerPanel.add(new JLabel(billDate != null ? dateFormat.format(billDate) : ""));
                
                headerPanel.add(new JLabel("Due Date:"));
                headerPanel.add(new JLabel(dueDate != null ? dateFormat.format(dueDate) : ""));
                
                headerPanel.add(new JLabel("Status:"));
                headerPanel.add(new JLabel(isPaid ? "Paid" : "Unpaid"));
                
                headerPanel.add(new JLabel("Total Amount:"));
                headerPanel.add(new JLabel(String.format("$%.2f", totalAmount)));
                
                panel.add(headerPanel, BorderLayout.NORTH);
                
                // Get bill items
                String itemsQuery = "SELECT description, amount, item_type FROM BillItems WHERE bill_id = ?";
                PreparedStatement itemsStmt = conn.prepareStatement(itemsQuery);
                itemsStmt.setInt(1, billId);
                
                ResultSet itemsRs = itemsStmt.executeQuery();
                
                // Create table for bill items
                String[] columns = {"Description", "Type", "Amount"};
                DefaultTableModel itemsModel = new DefaultTableModel(columns, 0) {
                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                };
                
                JTable itemsTable = new JTable(itemsModel);
                JScrollPane scrollPane = new JScrollPane(itemsTable);
                
                while (itemsRs.next()) {
                    String description = itemsRs.getString("description");
                    BigDecimal amount = itemsRs.getBigDecimal("amount");
                    String itemType = itemsRs.getString("item_type");
                    
                    // Format item type for display
                    String formattedType = itemType.replace('_', ' ');
                    formattedType = formattedType.charAt(0) + formattedType.substring(1).toLowerCase();
                    
                    itemsModel.addRow(new Object[]{
                        description,
                        formattedType,
                        String.format("$%.2f", amount)
                    });
                }
                
                itemsRs.close();
                itemsStmt.close();
                
                panel.add(scrollPane, BorderLayout.CENTER);
                
                // Button panel
                JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                
                if (!isPaid) {
                    JButton markPaidButton = new JButton("Mark as Paid");
                    markPaidButton.addActionListener(e -> {
                        dialog.dispose();
                        markBillAsPaid(billId);
                    });
                    buttonPanel.add(markPaidButton);
                }
                
                JButton closeButton = new JButton("Close");
                closeButton.addActionListener(e -> dialog.dispose());
                buttonPanel.add(closeButton);
                
                panel.add(buttonPanel, BorderLayout.SOUTH);
                
                dialog.getContentPane().add(panel);
                dialog.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Bill not found.", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
            
            billRs.close();
            billStmt.close();
            conn.close();
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, 
                "Error loading bill details: " + ex.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void markBillAsPaid(int billId) {
        try {
            Connection conn = dbConnection.getConnection();
            conn.setAutoCommit(false);
            
            try {
                // Get member ID and bill items from the bill
                String billQuery = "SELECT member_id FROM Bills WHERE bill_id = ?";
                PreparedStatement billStmt = conn.prepareStatement(billQuery);
                billStmt.setInt(1, billId);
                
                ResultSet billRs = billStmt.executeQuery();
                
                if (!billRs.next()) {
                    JOptionPane.showMessageDialog(this, 
                        "Bill not found.", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                int memberId = billRs.getInt("member_id");
                
                billRs.close();
                billStmt.close();
                
                // Mark bill as paid
                String updateBillQuery = "UPDATE Bills SET is_paid = TRUE WHERE bill_id = ?";
                PreparedStatement updateBillStmt = conn.prepareStatement(updateBillQuery);
                updateBillStmt.setInt(1, billId);
                updateBillStmt.executeUpdate();
                updateBillStmt.close();
                
                // Get items from the bill
                String itemsQuery = "SELECT item_type, reference_id FROM BillItems WHERE bill_id = ?";
                PreparedStatement itemsStmt = conn.prepareStatement(itemsQuery);
                itemsStmt.setInt(1, billId);
                
                ResultSet itemsRs = itemsStmt.executeQuery();
                
                while (itemsRs.next()) {
                    String itemType = itemsRs.getString("item_type");
                    int referenceId = itemsRs.getInt("reference_id");
                    
                    // Mark the referenced fee as paid
                    String updateQuery = "";
                    
                    switch (itemType) {
                        case "MEMBERSHIP_FEE":
                            updateQuery = "UPDATE MembershipFees SET is_paid = TRUE, paid_date = date('now') WHERE fee_id = ?";
                            break;
                        case "LATE_FEE":
                            updateQuery = "UPDATE LateFees SET is_paid = TRUE WHERE late_fee_id = ?";
                            break;
                        case "GUEST_FEE":
                            updateQuery = "UPDATE GuestFees SET is_paid = TRUE WHERE guest_fee_id = ?";
                            break;
                    }
                    
                    if (!updateQuery.isEmpty()) {
                        PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                        updateStmt.setInt(1, referenceId);
                        updateStmt.executeUpdate();
                        updateStmt.close();
                    }
                }
                
                itemsRs.close();
                itemsStmt.close();
                
                // If this was a membership fee payment, update member status
                String updateMemberQuery = "UPDATE Members SET status = 'ACTIVE' WHERE member_id = ? AND status = 'LATE_PAYMENT'";
                PreparedStatement updateMemberStmt = conn.prepareStatement(updateMemberQuery);
                updateMemberStmt.setInt(1, memberId);
                updateMemberStmt.executeUpdate();
                updateMemberStmt.close();
                
                conn.commit();
                
                JOptionPane.showMessageDialog(this, 
                    "Bill marked as paid successfully.", 
                    "Payment Recorded", 
                    JOptionPane.INFORMATION_MESSAGE);
                
                // Refresh the bills table
                loadBills();
                
            } catch (SQLException ex) {
                conn.rollback();
                JOptionPane.showMessageDialog(this, 
                    "Error marking bill as paid: " + ex.getMessage(), 
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
    
    private void applyLateFees() {
        try {
            Connection conn = dbConnection.getConnection();
            conn.setAutoCommit(false);
            
            try {
                // Get selected month and year
                int selectedMonth = monthComboBox.getSelectedIndex() + 1; // 1-based month
                int selectedYear = (Integer) yearSpinner.getValue();
                
                // Calculate the fee application date (usually the 1st of the month)
                java.sql.Date applicationDate = java.sql.Date.valueOf(selectedYear + "-" + 
                                               String.format("%02d", selectedMonth) + "-01");
                
                // Get unpaid membership fees that are overdue
                String unpaidQuery = "SELECT m.member_id, mf.fee_id, mf.amount " +
                                    "FROM Members m " +
                                    "JOIN MembershipFees mf ON m.member_id = mf.member_id " +
                                    "WHERE mf.is_paid = 0 " +
                                    "AND mf.due_date < ? " +
                                    "AND NOT EXISTS (SELECT 1 FROM LateFees lf WHERE lf.fee_id = mf.fee_id AND MONTH(lf.month_applied) = ? AND YEAR(lf.month_applied) = ?)";
                
                PreparedStatement unpaidStmt = conn.prepareStatement(unpaidQuery);
                unpaidStmt.setDate(1, applicationDate);
                unpaidStmt.setInt(2, selectedMonth);
                unpaidStmt.setInt(3, selectedYear);
                
                ResultSet unpaidRs = unpaidStmt.executeQuery();
                
                int feesApplied = 0;
                
                while (unpaidRs.next()) {
                    int memberId = unpaidRs.getInt("member_id");
                    int feeId = unpaidRs.getInt("fee_id");
                    BigDecimal originalAmount = unpaidRs.getBigDecimal("amount");
                    
                    // Apply a $40 late fee
                    BigDecimal lateFeeAmount = new BigDecimal(40.00);
                    
                    // Insert the late fee
                    String insertQuery = "INSERT INTO LateFees (member_id, fee_id, amount, month_applied, is_paid) " +
                                       "VALUES (?, ?, ?, ?, FALSE)";
                    
                    PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
                    insertStmt.setInt(1, memberId);
                    insertStmt.setInt(2, feeId);
                    insertStmt.setBigDecimal(3, lateFeeAmount);
                    insertStmt.setDate(4, applicationDate);
                    
                    insertStmt.executeUpdate();
                    insertStmt.close();
                    
                    // Update member status to LATE_PAYMENT
                    String updateStatusQuery = "UPDATE Members SET status = 'LATE_PAYMENT' WHERE member_id = ?";
                    PreparedStatement updateStatusStmt = conn.prepareStatement(updateStatusQuery);
                    updateStatusStmt.setInt(1, memberId);
                    updateStatusStmt.executeUpdate();
                    updateStatusStmt.close();
                    
                    feesApplied++;
                }
                
                unpaidRs.close();
                unpaidStmt.close();
                
                conn.commit();
                
                if (feesApplied > 0) {
                    JOptionPane.showMessageDialog(this, 
                        "Applied " + feesApplied + " late fees.", 
                        "Late Fees Applied", 
                        JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "No late fees to apply for this period.", 
                        "No Late Fees", 
                        JOptionPane.INFORMATION_MESSAGE);
                }
                
                // Refresh data
                loadLateFees();
                
            } catch (SQLException ex) {
                conn.rollback();
                JOptionPane.showMessageDialog(this, 
                    "Error applying late fees: " + ex.getMessage(), 
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
    
    private void sendLateFeesNotice(int memberId, String memberName) {
        try {
            Connection conn = dbConnection.getConnection();
            
            // Get member email
            String emailQuery = "SELECT email FROM Members WHERE member_id = ?";
            PreparedStatement emailStmt = conn.prepareStatement(emailQuery);
            emailStmt.setInt(1, memberId);
            
            ResultSet emailRs = emailStmt.executeQuery();
            
            if (emailRs.next()) {
                String email = emailRs.getString("email");
                
                // Get total dues
                String duesQuery = "SELECT " +
                                  "COALESCE(SUM(CASE WHEN mf.is_paid = 0 THEN mf.amount ELSE 0 END), 0) AS membership_fees, " +
                                  "COALESCE(SUM(CASE WHEN lf.is_paid = 0 THEN lf.amount ELSE 0 END), 0) AS late_fees, " +
                                  "COALESCE(SUM(CASE WHEN gf.is_paid = 0 THEN gf.amount ELSE 0 END), 0) AS guest_fees " +
                                  "FROM Members m " +
                                  "LEFT JOIN MembershipFees mf ON m.member_id = mf.member_id " +
                                  "LEFT JOIN LateFees lf ON m.member_id = lf.member_id " +
                                  "LEFT JOIN GuestFees gf ON m.member_id = gf.member_id " +
                                  "WHERE m.member_id = ?";
                
                PreparedStatement duesStmt = conn.prepareStatement(duesQuery);
                duesStmt.setInt(1, memberId);
                
                ResultSet duesRs = duesStmt.executeQuery();
                
                double membershipFees = 0;
                double lateFees = 0;
                double guestFees = 0;
                
                if (duesRs.next()) {
                    membershipFees = duesRs.getDouble("membership_fees");
                    lateFees = duesRs.getDouble("late_fees");
                    guestFees = duesRs.getDouble("guest_fees");
                }
                
                double totalDue = membershipFees + lateFees + guestFees;
                
                // In a real application, this would send an actual email
                // For this example, we'll just simulate it
                
                StringBuilder emailContent = new StringBuilder();
                emailContent.append("Dear ").append(memberName).append(",\n\n");
                emailContent.append("Our records indicate that you have overdue fees totaling $").append(String.format("%.2f", totalDue)).append(".\n\n");
                emailContent.append("Breakdown:\n");
                emailContent.append("- Membership Fees: $").append(String.format("%.2f", membershipFees)).append("\n");
                emailContent.append("- Late Fees: $").append(String.format("%.2f", lateFees)).append("\n");
                emailContent.append("- Guest Fees: $").append(String.format("%.2f", guestFees)).append("\n\n");
                emailContent.append("Please log in to your account to make a payment as soon as possible.\n\n");
                emailContent.append("If you have questions about your account, please contact the treasurer.\n\n");
                emailContent.append("Thank you,\nTennis Club Billing Department");
                
                // Show the email content in a dialog (for demonstration)
                JTextArea textArea = new JTextArea(emailContent.toString());
                textArea.setEditable(false);
                textArea.setLineWrap(true);
                textArea.setWrapStyleWord(true);
                
                JScrollPane scrollPane = new JScrollPane(textArea);
                scrollPane.setPreferredSize(new Dimension(400, 300));
                
                JOptionPane.showMessageDialog(this, 
                    scrollPane, 
                    "Late Fee Notice to " + email, 
                    JOptionPane.INFORMATION_MESSAGE);
                
                duesRs.close();
                duesStmt.close();
            }
            
            emailRs.close();
            emailStmt.close();
            conn.close();
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, 
                "Error sending late fees notice: " + ex.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
