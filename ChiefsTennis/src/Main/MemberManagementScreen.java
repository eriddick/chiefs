package Main;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import javax.swing.*;
import javax.swing.table.*;

public class MemberManagementScreen extends JFrame {

    private User currentUser;
    private DatabaseConnection dbConnection;

    private JTable memberTable;
    private DefaultTableModel memberTableModel;
    private JButton addMemberButton;
    private JButton editMemberButton;
    private JButton viewHistoryButton;
    private JTextField searchField;
    private JButton searchButton;
    private JCheckBox showInactiveCheckbox;

    private boolean isAdmin;
    private boolean isTreasurer;

    public MemberManagementScreen(User currentUser) {
        this.currentUser = currentUser;
        this.dbConnection = new DatabaseConnection();

        // System.out.println("T0");
        // if (currentUser == null) {
        // System.out.println("C-NULL");
        // }
        // System.out.println("S:" + s);

        isAdmin = "ADMIN".equals(this.currentUser.getRole());
        isTreasurer = "TREASURER".equals(currentUser.getRole());

        initComponents();
        setupLayout();
        setupListeners();
        loadMembers();

        setTitle("Member Management");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private void initComponents() {
        // Create table columns based on user role
        ArrayList<String> columns = new ArrayList<>(Arrays.asList(
                "ID", "First Name", "Last Name", "Email", "Phone", "Join Date", "Status"));

        // Add visibility columns
        columns.add("Show Email");
        columns.add("Show Phone");

        // Add financial columns for treasurer
        if (isTreasurer) {
            columns.add("Current Balance");
            columns.add("Last Payment Date");
        }

        memberTableModel = new DefaultTableModel(columns.toArray(new String[0]), 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }

            @Override
            public Class<?> getColumnClass(int column) {
                if (column == columns.indexOf("Show Email") ||
                        column == columns.indexOf("Show Phone")) {
                    return Boolean.class;
                }
                return super.getColumnClass(column);
            }
        };

        memberTable = new JTable(memberTableModel);
        memberTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        memberTable.setAutoCreateRowSorter(true);

        // Set column widths
        TableColumnModel columnModel = memberTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(50); // ID
        columnModel.getColumn(1).setPreferredWidth(100); // First Name
        columnModel.getColumn(2).setPreferredWidth(100); // Last Name
        columnModel.getColumn(3).setPreferredWidth(150); // Email
        columnModel.getColumn(4).setPreferredWidth(100); // Phone
        columnModel.getColumn(5).setPreferredWidth(100); // Join Date
        columnModel.getColumn(6).setPreferredWidth(80); // Status
        columnModel.getColumn(7).setPreferredWidth(80); // Show Email
        columnModel.getColumn(8).setPreferredWidth(80); // Show Phone

        if (isTreasurer) {
            columnModel.getColumn(9).setPreferredWidth(100); // Current Balance
            columnModel.getColumn(10).setPreferredWidth(120); // Last Payment Date
        }

        // Buttons
        addMemberButton = new JButton("Add Member");
        editMemberButton = new JButton("Edit Member");
        viewHistoryButton = new JButton("View History");

        // Disable edit and history buttons until a row is selected
        editMemberButton.setEnabled(false);
        viewHistoryButton.setEnabled(false);

        // Search components
        searchField = new JTextField(20);
        searchButton = new JButton("Search");
        showInactiveCheckbox = new JCheckBox("Show Inactive Members");

        // Only admins can see inactive members
        showInactiveCheckbox.setVisible(isAdmin);
    }

    private void setupLayout() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Search panel at the top
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(Box.createHorizontalStrut(20));
        searchPanel.add(showInactiveCheckbox);

        mainPanel.add(searchPanel, BorderLayout.NORTH);

        // Member table in the center
        JScrollPane scrollPane = new JScrollPane(memberTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Button panel at the bottom
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(viewHistoryButton);
        buttonPanel.add(editMemberButton);
        buttonPanel.add(addMemberButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        getContentPane().add(mainPanel);
    }

    private void setupListeners() {
        // Table selection listener
        memberTable.getSelectionModel().addListSelectionListener(e -> {
            boolean rowSelected = memberTable.getSelectedRow() != -1;
            editMemberButton.setEnabled(rowSelected);
            viewHistoryButton.setEnabled(rowSelected && isAdmin);
        });

        // Add member button
        addMemberButton.addActionListener(e -> showAddMemberDialog());

        // Edit member button
        editMemberButton.addActionListener(e -> {
            int selectedRow = memberTable.getSelectedRow();
            if (selectedRow != -1) {
                int modelRow = memberTable.convertRowIndexToModel(selectedRow);
                int memberId = (int) memberTableModel.getValueAt(modelRow, 0);
                showEditMemberDialog(memberId);
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

        // Search button
        searchButton.addActionListener(e -> {
            loadMembers();
        });

        // Show inactive checkbox
        showInactiveCheckbox.addActionListener(e -> {
            loadMembers();
        });

        // Also allow pressing Enter in search field
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    loadMembers();
                }
            }
        });
    }

    private void loadMembers() {
        memberTableModel.setRowCount(0);

        try {
            Connection conn = dbConnection.getConnection();

            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("SELECT m.member_id, m.first_name, m.last_name, m.email, m.phone, ")
                    .append("m.join_date, m.status, m.show_email, m.show_phone ");

            // Add treasurer-specific fields
            if (isTreasurer) {
                queryBuilder.append(", (SELECT COALESCE(SUM(CASE WHEN is_paid = 0 THEN amount ELSE 0 END), 0) ")
                        .append("FROM (SELECT fee_id, amount, is_paid FROM MembershipFees WHERE member_id = m.member_id ")
                        .append("UNION ALL ")
                        .append("SELECT late_fee_id, amount, is_paid FROM LateFees WHERE member_id = m.member_id ")
                        .append("UNION ALL ")
                        .append("SELECT guest_fee_id, amount, is_paid FROM GuestFees WHERE member_id = m.member_id) AS fees) ")
                        .append("AS current_balance, ")
                        .append("(SELECT MAX(paid_date) FROM MembershipFees WHERE member_id = m.member_id AND paid_date IS NOT NULL) ")
                        .append("AS last_payment_date ");
            }

            queryBuilder.append("FROM Members m WHERE 1=1 ");

            // Add search condition if search field is not empty
            String searchTerm = searchField.getText().trim();
            if (!searchTerm.isEmpty()) {
                queryBuilder
                        .append("AND (m.first_name LIKE ? OR m.last_name LIKE ? OR m.email LIKE ? OR m.phone LIKE ?) ");
            }

            // Add status condition based on checkbox
            if (!showInactiveCheckbox.isSelected() || !isAdmin) {
                queryBuilder.append("AND m.status != 'INACTIVE' ");
            }

            // Add ordering
            queryBuilder.append("ORDER BY m.last_name, m.first_name");

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

                // jacob test

                // System.out.println(rs.getString("join_date"));
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                Date joinDate = new Date();
                try {
                    joinDate = formatter.parse(rs.getString("join_date"));
                } catch (ParseException e) {
                    System.err.println("Error parsing date: " + e.getMessage());
                }

                String status = rs.getString("status");
                boolean showEmail = rs.getBoolean("show_email");
                boolean showPhone = rs.getBoolean("show_phone");

                ArrayList<Object> rowData = new ArrayList<>(Arrays.asList(
                        memberId,
                        firstName,
                        lastName,
                        email,
                        phone,
                        joinDate != null ? dateFormat.format(joinDate) : "",
                        status,
                        showEmail,
                        showPhone));

                // Add treasurer-specific data
                if (isTreasurer) {
                    double currentBalance = rs.getDouble("current_balance");
                    Date lastPaymentDate = rs.getDate("last_payment_date");

                    rowData.add(String.format("$%.2f", currentBalance));
                    rowData.add(lastPaymentDate != null ? dateFormat.format(lastPaymentDate) : "Never");
                }

                memberTableModel.addRow(rowData.toArray());
            }

            rs.close();
            stmt.close();
            conn.close();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error loading members: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void showAddMemberDialog() {
        MemberDialog dialog = new MemberDialog(this, "Add New Member", -1);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            loadMembers();
        }
    }

    private void showEditMemberDialog(int memberId) {
        MemberDialog dialog = new MemberDialog(this, "Edit Member", memberId);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            loadMembers();
        }
    }

    private void showMemberHistoryDialog(int memberId) {
        if (!isAdmin)
            return;

        JDialog dialog = new JDialog(this, "Member History", true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Get member name
        String memberName = "";
        try {
            Connection conn = dbConnection.getConnection();
            String query = "SELECT CONCAT(first_name, ' ', last_name) AS name FROM Members WHERE member_id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, memberId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                memberName = rs.getString("name");
            }

            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(dialog,
                    "Error getting member name: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        // Add title
        JLabel titleLabel = new JLabel("History for " + memberName, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Create history table
        String[] columns = { "Start Date", "End Date", "Reason" };
        DefaultTableModel historyModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable historyTable = new JTable(historyModel);
        JScrollPane scrollPane = new JScrollPane(historyTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Load history data
        try {
            Connection conn = dbConnection.getConnection();
            String query = "SELECT start_date, end_date, reason FROM MemberHistory " +
                    "WHERE member_id = ? ORDER BY start_date DESC";

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, memberId);
            ResultSet rs = stmt.executeQuery();

            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

            while (rs.next()) {
                String startDate = rs.getString("start_date");
                String endDate = rs.getString("end_date");
                String reason = rs.getString("reason");

                historyModel.addRow(new Object[] {
                        // startDate != null ? dateFormat.format(startDate) : "",
                        // endDate != null ? dateFormat.format(endDate) : "Current",
                        startDate,
                        endDate,
                        reason != null ? reason : ""
                });
            }

            rs.close();
            stmt.close();

            // If we're an admin and want to see past 3 years of membership
            if (isAdmin) {
                // Add additional query to show expired memberships
                String pastQuery = "SELECT m.member_id, m.first_name, m.last_name, mh.start_date, mh.end_date " +
                        "FROM Members m " +
                        "JOIN MemberHistory mh ON m.member_id = mh.member_id " +
                        "WHERE mh.end_date IS NOT NULL " +
                        "AND mh.end_date >= DATE_SUB(CURDATE(), INTERVAL 3 YEAR) " +
                        "ORDER BY mh.end_date DESC";

                Statement pastStmt = conn.createStatement();
                ResultSet pastRs = pastStmt.executeQuery(pastQuery);

                while (pastRs.next()) {

                }

                pastRs.close();
                pastStmt.close();
            }

            conn.close();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(dialog,
                    "Error loading member history: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        // Add close button
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dialog.dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(closeButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.getContentPane().add(panel);
        dialog.setVisible(true);
    }

    // Inner class for Add/Edit Member dialog
    class MemberDialog extends JDialog {
        private int memberId;
        private boolean confirmed = false;

        private JTextField firstNameField;
        private JTextField lastNameField;
        private JTextField emailField;
        private JTextField phoneField;
        private JComboBox<String> statusComboBox;
        private JCheckBox showEmailCheckbox;
        private JCheckBox showPhoneCheckbox;
        private JButton saveButton;
        private JButton cancelButton;

        public MemberDialog(JFrame parent, String title, int memberId) {
            super(parent, title, true);
            this.memberId = memberId;

            initComponents();
            setupLayout();
            setupListeners();

            if (memberId > 0) {
                loadMemberData();
            }

            setSize(500, 400);
            setLocationRelativeTo(parent);
            setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        }

        private void initComponents() {
            firstNameField = new JTextField(20);
            lastNameField = new JTextField(20);
            emailField = new JTextField(20);
            phoneField = new JTextField(20);

            String[] statusOptions = { "ACTIVE", "LATE_PAYMENT" };
            if (isAdmin) {
                statusOptions = new String[] { "ACTIVE", "LATE_PAYMENT", "INACTIVE" };
            }
            statusComboBox = new JComboBox<>(statusOptions);

            showEmailCheckbox = new JCheckBox("Show Email in Directory");
            showPhoneCheckbox = new JCheckBox("Show Phone in Directory");

            saveButton = new JButton("Save");
            cancelButton = new JButton("Cancel");
        }

        private void setupLayout() {
            JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
            mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            JPanel formPanel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(5, 5, 5, 5);

            // First name
            gbc.gridx = 0;
            gbc.gridy = 0;
            formPanel.add(new JLabel("First Name:"), gbc);

            gbc.gridx = 1;
            gbc.gridy = 0;
            formPanel.add(firstNameField, gbc);

            // Last name
            gbc.gridx = 0;
            gbc.gridy = 1;
            formPanel.add(new JLabel("Last Name:"), gbc);

            gbc.gridx = 1;
            gbc.gridy = 1;
            formPanel.add(lastNameField, gbc);

            // Email
            gbc.gridx = 0;
            gbc.gridy = 2;
            formPanel.add(new JLabel("Email:"), gbc);

            gbc.gridx = 1;
            gbc.gridy = 2;
            formPanel.add(emailField, gbc);

            // Phone
            gbc.gridx = 0;
            gbc.gridy = 3;
            formPanel.add(new JLabel("Phone:"), gbc);

            gbc.gridx = 1;
            gbc.gridy = 3;
            formPanel.add(phoneField, gbc);

            // Status (only for admins and treasurers, or when editing)
            if (isAdmin || isTreasurer || memberId > 0) {
                gbc.gridx = 0;
                gbc.gridy = 4;
                formPanel.add(new JLabel("Status:"), gbc);

                gbc.gridx = 1;
                gbc.gridy = 4;
                formPanel.add(statusComboBox, gbc);
            }

            // Directory options
            gbc.gridx = 0;
            gbc.gridy = 5;
            gbc.gridwidth = 2;
            formPanel.add(showEmailCheckbox, gbc);

            gbc.gridx = 0;
            gbc.gridy = 6;
            formPanel.add(showPhoneCheckbox, gbc);

            mainPanel.add(formPanel, BorderLayout.CENTER);

            // Button panel
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.add(saveButton);
            buttonPanel.add(cancelButton);
            mainPanel.add(buttonPanel, BorderLayout.SOUTH);

            getContentPane().add(mainPanel);
        }

        private void setupListeners() {
            saveButton.addActionListener(e -> {
                if (validateForm()) {
                    if (saveMember()) {
                        confirmed = true;
                        dispose();
                    }
                }
            });

            cancelButton.addActionListener(e -> dispose());
        }

        private boolean validateForm() {
            if (firstNameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "First name is required.",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }

            if (lastNameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Last name is required.",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }

            if (emailField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Email is required.",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }

            // Simple email validation
            String email = emailField.getText().trim();
            if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
                JOptionPane.showMessageDialog(this,
                        "Please enter a valid email address.",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }

            return true;
        }

        private void loadMemberData() {
            try {
                Connection conn = dbConnection.getConnection();
                String query = "SELECT first_name, last_name, email, phone, status, show_email, show_phone " +
                        "FROM Members WHERE member_id = ?";

                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, memberId);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    firstNameField.setText(rs.getString("first_name"));
                    lastNameField.setText(rs.getString("last_name"));
                    emailField.setText(rs.getString("email"));
                    phoneField.setText(rs.getString("phone"));
                    statusComboBox.setSelectedItem(rs.getString("status"));
                    showEmailCheckbox.setSelected(rs.getBoolean("show_email"));
                    showPhoneCheckbox.setSelected(rs.getBoolean("show_phone"));
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Member not found.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    dispose();
                }

                rs.close();
                stmt.close();
                conn.close();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                        "Error loading member data: " + ex.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
                dispose();
            }
        }

        // Find the saveMember() method in MemberManagementScreen$MemberDialog class
        // and replace the relevant part with this SQLite-compatible version

        private boolean saveMember() {
            try {
                Connection conn = dbConnection.getConnection();
                conn.setAutoCommit(false);

                try {
                    if (memberId > 0) {
                        // Update existing member
                        String updateQuery = "UPDATE Members SET first_name = ?, last_name = ?, " +
                                "email = ?, phone = ?, status = ?, show_email = ?, show_phone = ? " +
                                "WHERE member_id = ?";

                        PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                        updateStmt.setString(1, firstNameField.getText().trim());
                        updateStmt.setString(2, lastNameField.getText().trim());
                        updateStmt.setString(3, emailField.getText().trim());
                        updateStmt.setString(4, phoneField.getText().trim());
                        updateStmt.setString(5, (String) statusComboBox.getSelectedItem());
                        updateStmt.setBoolean(6, showEmailCheckbox.isSelected());
                        updateStmt.setBoolean(7, showPhoneCheckbox.isSelected());
                        updateStmt.setInt(8, memberId);

                        updateStmt.executeUpdate();
                        updateStmt.close();
                    } else {
                        // Insert new member
                        // Use SQLite date function instead of CURDATE()
                        String insertQuery = "INSERT INTO Members (first_name, last_name, email, phone, " +
                                "join_date, status, show_email, show_phone) " +
                                "VALUES (?, ?, ?, ?, date('now'), ?, ?, ?)";

                        PreparedStatement insertStmt = conn.prepareStatement(insertQuery,
                                Statement.RETURN_GENERATED_KEYS);
                        insertStmt.setString(1, firstNameField.getText().trim());
                        insertStmt.setString(2, lastNameField.getText().trim());
                        insertStmt.setString(3, emailField.getText().trim());
                        insertStmt.setString(4, phoneField.getText().trim());
                        insertStmt.setString(5, "ACTIVE"); // Default status for new members
                        insertStmt.setBoolean(6, showEmailCheckbox.isSelected());
                        insertStmt.setBoolean(7, showPhoneCheckbox.isSelected());

                        insertStmt.executeUpdate();

                        ResultSet generatedKeys = insertStmt.getGeneratedKeys();
                        if (generatedKeys.next()) {
                            int newMemberId = generatedKeys.getInt(1);

                            // Create membership fee record
                            // Use SQLite date function instead of DATE_ADD
                            String feeQuery = "INSERT INTO MembershipFees (member_id, fee_year, amount, due_date, is_paid) "
                                    +
                                    "VALUES (?, strftime('%Y', 'now'), 400.00, date('now', '+30 days'), 0)";

                            PreparedStatement feeStmt = conn.prepareStatement(feeQuery);
                            feeStmt.setInt(1, newMemberId);
                            feeStmt.executeUpdate();
                            feeStmt.close();

                            // Create member history record
                            // Use SQLite date function instead of CURDATE()
                            String historyQuery = "INSERT INTO MemberHistory (member_id, start_date, end_date, reason) "
                                    +
                                    "VALUES (?, date('now'), NULL, 'Initial membership')";

                            PreparedStatement historyStmt = conn.prepareStatement(historyQuery);
                            historyStmt.setInt(1, newMemberId);
                            historyStmt.executeUpdate();
                            historyStmt.close();

                            // Create user account
                            String username = (firstNameField.getText().trim().charAt(0) +
                                    lastNameField.getText().trim()).toLowerCase();
                            String defaultPassword = "tennis" + newMemberId; // In a real app, use a secure random
                                                                             // password

                            String userQuery = "INSERT INTO Users (username, password, role, member_id) " +
                                    "VALUES (?, ?, 'MEMBER', ?)";

                            PreparedStatement userStmt = conn.prepareStatement(userQuery);
                            userStmt.setString(1, username);
                            userStmt.setString(2, defaultPassword); // In a real app, hash this password
                            userStmt.setInt(3, newMemberId);
                            userStmt.executeUpdate();
                            userStmt.close();

                            JOptionPane.showMessageDialog(this,
                                    "New member added successfully.\n\n" +
                                            "Username: " + username + "\n" +
                                            "Password: " + defaultPassword + "\n\n" +
                                            "Please inform the member to change their password after first login.",
                                    "Member Added",
                                    JOptionPane.INFORMATION_MESSAGE);
                        }

                        generatedKeys.close();
                        insertStmt.close();
                    }

                    conn.commit();
                    return true;

                } catch (SQLException ex) {
                    conn.rollback();
                    JOptionPane.showMessageDialog(this,
                            "Error saving member: " + ex.getMessage(),
                            "Database Error",
                            JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                    return false;
                } finally {
                    conn.setAutoCommit(true);
                    conn.close();
                }

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                        "Error connecting to database: " + ex.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }

        public boolean isConfirmed() {
            return confirmed;
        }
    }
}