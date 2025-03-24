package Main;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;
import java.text.SimpleDateFormat;

public class MemberDashboard extends JFrame {
    private User currentUser;
    private DatabaseConnection dbConnection;
    
    // Main panels
    private JPanel upcomingReservationsPanel;
    private JPanel accountStatusPanel;
    private JPanel quickActionsPanel;
    
    // Tables
    private JTable reservationsTable;
    private DefaultTableModel reservationsTableModel;
    
    // Labels for account info
    private JLabel nameLabel;
    private JLabel emailLabel;
    private JLabel phoneLabel;
    private JLabel statusLabel;
    private JLabel balanceLabel;
    private JLabel guestPassesLabel;
    
    // Action buttons
    private JButton makeReservationButton;
    private JButton viewScheduleButton;
    private JButton viewBillsButton;
    private JButton updateProfileButton;
    
    public MemberDashboard(User user, DatabaseConnection dbConnection) {
        this.currentUser = user;
        this.dbConnection = dbConnection;
        
        initComponents();
        setupLayout();
        setupListeners();
        loadMemberInfo();
        loadUpcomingReservations();
        
        setTitle("Tennis Club - Member Dashboard");
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    
    public MemberDashboard(User user) {
        this(user, new DatabaseConnection());  // Call the constructor with both parameters
    }
    
    private void initComponents() {
        // Initialize panels
        upcomingReservationsPanel = new JPanel(new BorderLayout(5, 5));
        upcomingReservationsPanel.setBorder(BorderFactory.createTitledBorder("Your Upcoming Reservations"));
        
        accountStatusPanel = new JPanel(new GridBagLayout());
        accountStatusPanel.setBorder(BorderFactory.createTitledBorder("Account Status"));
        
        quickActionsPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        quickActionsPanel.setBorder(BorderFactory.createTitledBorder("Quick Actions"));
        
        // Initialize account info labels
        nameLabel = new JLabel();
        emailLabel = new JLabel();
        phoneLabel = new JLabel();
        statusLabel = new JLabel();
        balanceLabel = new JLabel();
        guestPassesLabel = new JLabel();
        
        // Make labels bold
        Font boldFont = new Font(nameLabel.getFont().getName(), Font.BOLD, nameLabel.getFont().getSize());
        nameLabel.setFont(boldFont);
        
        // Initialize reservation table
        String[] reservationColumns = {"Court", "Date", "Time", "Type", "Players", "Actions"};
        reservationsTableModel = new DefaultTableModel(reservationColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // Only actions column is editable
            }
        };
        
        reservationsTable = new JTable(reservationsTableModel);
        reservationsTable.setRowHeight(30);
        
        // Set up action buttons in the table
        Action cancelAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                int row = Integer.parseInt(e.getActionCommand());
                cancelReservation(row);
            }
        };
        
        ButtonColumn buttonColumn = new ButtonColumn(reservationsTable, cancelAction, 5);
        
        // Initialize action buttons
        makeReservationButton = new JButton("Make Reservation");
        viewScheduleButton = new JButton("View Court Schedule");
        viewBillsButton = new JButton("View & Pay Bills");
        updateProfileButton = new JButton("Update Profile");
    }
    
    private void setupLayout() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Set up top panel with account info and quick actions
        JPanel topPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        // Account status panel
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.7;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 10);
        
        // Set up account info grid
        GridBagConstraints accGbc = new GridBagConstraints();
        accGbc.anchor = GridBagConstraints.WEST;
        accGbc.insets = new Insets(5, 5, 5, 5);
        
        accGbc.gridx = 0;
        accGbc.gridy = 0;
        accountStatusPanel.add(new JLabel("Member:"), accGbc);
        
        accGbc.gridx = 1;
        accGbc.gridy = 0;
        accountStatusPanel.add(nameLabel, accGbc);
        
        accGbc.gridx = 0;
        accGbc.gridy = 1;
        accountStatusPanel.add(new JLabel("Email:"), accGbc);
        
        accGbc.gridx = 1;
        accGbc.gridy = 1;
        accountStatusPanel.add(emailLabel, accGbc);
        
        accGbc.gridx = 0;
        accGbc.gridy = 2;
        accountStatusPanel.add(new JLabel("Phone:"), accGbc);
        
        accGbc.gridx = 1;
        accGbc.gridy = 2;
        accountStatusPanel.add(phoneLabel, accGbc);
        
        accGbc.gridx = 0;
        accGbc.gridy = 3;
        accountStatusPanel.add(new JLabel("Status:"), accGbc);
        
        accGbc.gridx = 1;
        accGbc.gridy = 3;
        accountStatusPanel.add(statusLabel, accGbc);
        
        accGbc.gridx = 0;
        accGbc.gridy = 4;
        accountStatusPanel.add(new JLabel("Balance Due:"), accGbc);
        
        accGbc.gridx = 1;
        accGbc.gridy = 4;
        accountStatusPanel.add(balanceLabel, accGbc);
        
        accGbc.gridx = 0;
        accGbc.gridy = 5;
        accountStatusPanel.add(new JLabel("Guest Passes Remaining:"), accGbc);
        
        accGbc.gridx = 1;
        accGbc.gridy = 5;
        accountStatusPanel.add(guestPassesLabel, accGbc);
        
        topPanel.add(accountStatusPanel, gbc);
        
        // Quick actions panel
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 10, 0, 0);
        
        quickActionsPanel.add(makeReservationButton);
        quickActionsPanel.add(viewScheduleButton);
        quickActionsPanel.add(viewBillsButton);
        quickActionsPanel.add(updateProfileButton);
        
        topPanel.add(quickActionsPanel, gbc);
        
        mainPanel.add(topPanel, BorderLayout.NORTH);
        
        // Set up reservations panel
        JScrollPane scrollPane = new JScrollPane(reservationsTable);
        upcomingReservationsPanel.add(scrollPane, BorderLayout.CENTER);
        
        mainPanel.add(upcomingReservationsPanel, BorderLayout.CENTER);
        
        getContentPane().add(mainPanel);
    }
    
    private void setupListeners() {
        makeReservationButton.addActionListener(e -> {
            CourtReservationScreen reservationScreen = new CourtReservationScreen(currentUser);
            reservationScreen.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    loadUpcomingReservations();
                }
            });
            reservationScreen.setVisible(true);
        });
        
        viewScheduleButton.addActionListener(e -> {
            CourtScheduleScreen scheduleScreen = new CourtScheduleScreen(currentUser);
            scheduleScreen.setVisible(true);
        });
        
        viewBillsButton.addActionListener(e -> {
            BillingSystem billingScreen = new BillingSystem(currentUser);
            billingScreen.setVisible(true);
        });
        
        updateProfileButton.addActionListener(e -> {
            ProfileUpdateScreen profileScreen = new ProfileUpdateScreen(currentUser);
            profileScreen.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    loadMemberInfo();
                }
            });
            profileScreen.setVisible(true);
        });
    }
    
    private void loadMemberInfo() {
        try {
            Connection conn = dbConnection.getConnection();
            
            // Get member information
            String query = "SELECT m.first_name, m.last_name, m.email, m.phone, m.status, " +
                          "COALESCE((SELECT SUM(amount) FROM MembershipFees WHERE member_id = m.member_id AND is_paid = 0), 0) + " +
                          "COALESCE((SELECT SUM(amount) FROM LateFees WHERE member_id = m.member_id AND is_paid = 0), 0) + " +
                          "COALESCE((SELECT SUM(amount) FROM GuestFees WHERE member_id = m.member_id AND is_paid = 0), 0) AS balance, " +
                          "(4 - COALESCE((SELECT count FROM GuestPassCount WHERE member_id = m.member_id AND " +
                          "month = CAST(strftime('%m', 'now') AS INTEGER) AND year = CAST(strftime('%Y', 'now') AS INTEGER)), 0)) AS remaining_passes " +
                          "FROM Members m WHERE m.member_id = ?";
            
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, currentUser.getMemberId());
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                String email = rs.getString("email");
                String phone = rs.getString("phone");
                String status = rs.getString("status");
                double balance = rs.getDouble("balance");
                int remainingPasses = rs.getInt("remaining_passes");
                
                // Set label values
                nameLabel.setText(firstName + " " + lastName);
                emailLabel.setText(email);
                phoneLabel.setText(phone);
                statusLabel.setText(status);
                balanceLabel.setText(String.format("$%.2f", balance));
                guestPassesLabel.setText(String.valueOf(remainingPasses));
                
                // Style status label based on status
                if ("ACTIVE".equals(status)) {
                    statusLabel.setForeground(new Color(0, 128, 0)); // Green
                } else if ("LATE_PAYMENT".equals(status)) {
                    statusLabel.setForeground(new Color(255, 128, 0)); // Orange
                } else {
                    statusLabel.setForeground(Color.RED);
                }
                
                // Style balance label based on amount
                if (balance > 0) {
                    balanceLabel.setForeground(Color.RED);
                } else {
                    balanceLabel.setForeground(new Color(0, 128, 0)); // Green
                }
            }
            
            rs.close();
            stmt.close();
            conn.close();
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, 
                "Error loading member information: " + ex.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadUpcomingReservations() {
        reservationsTableModel.setRowCount(0);
        
        try {
            Connection conn = dbConnection.getConnection();
            
            // Get upcoming reservations
            String query = "SELECT r.reservation_id, c.court_number, r.reservation_date, r.start_time, r.end_time, " +
                    "r.reservation_type, COUNT(rp.participant_id) AS participant_count " +
                    "FROM Reservations r " +
                    "JOIN Courts c ON r.court_id = c.court_id " +
                    "LEFT JOIN ReservationParticipants rp ON r.reservation_id = rp.reservation_id " +
                    "WHERE r.member_id = ? AND r.reservation_date >= date('now') " + // Changed from CURDATE()
                    "GROUP BY r.reservation_id " +
                    "ORDER BY r.reservation_date, r.start_time";
            
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, currentUser.getMemberId());
            
            ResultSet rs = stmt.executeQuery();
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
            SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");
            
            while (rs.next()) {
                int reservationId = rs.getInt("reservation_id");
                int courtNumber = rs.getInt("court_number");
                java.sql.Date date = rs.getDate("reservation_date");
                java.sql.Time startTime = rs.getTime("start_time");
                java.sql.Time endTime = rs.getTime("end_time");
                String type = rs.getString("reservation_type");
                int participantCount = rs.getInt("participant_count");
                
                reservationsTableModel.addRow(new Object[]{
                    "Court " + courtNumber,
                    dateFormat.format(date),
                    timeFormat.format(startTime) + " - " + timeFormat.format(endTime),
                    type,
                    participantCount + " players",
                    "Cancel"
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
    
    private void cancelReservation(int row) {
        int result = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to cancel this reservation?",
            "Confirm Cancellation",
            JOptionPane.YES_NO_OPTION);
        
        if (result != JOptionPane.YES_OPTION) {
            return;
        }
        
        try {
            Connection conn = dbConnection.getConnection();
            
            // Get the reservation date and time to validate cancellation
            String court = (String) reservationsTableModel.getValueAt(row, 0);
            String dateStr = (String) reservationsTableModel.getValueAt(row, 1);
            String timeRange = (String) reservationsTableModel.getValueAt(row, 2);
            
            int courtNumber = Integer.parseInt(court.replace("Court ", ""));
            
            // Get reservation ID
            String findQuery = "SELECT r.reservation_id FROM Reservations r " +
                              "JOIN Courts c ON r.court_id = c.court_id " +
                              "WHERE c.court_number = ? AND r.member_id = ? " +
                              "AND r.reservation_date = STR_TO_DATE(?, '%m/%d/%Y') " +
                              "ORDER BY r.reservation_date, r.start_time";
            
            PreparedStatement findStmt = conn.prepareStatement(findQuery);
            findStmt.setInt(1, courtNumber);
            findStmt.setInt(2, currentUser.getMemberId());
            findStmt.setString(3, dateStr);
            
            ResultSet findRs = findStmt.executeQuery();
            
            if (findRs.next()) {
                int reservationId = findRs.getInt("reservation_id");
                
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
                
                JOptionPane.showMessageDialog(this, 
                    "Reservation cancelled successfully.", 
                    "Reservation Cancelled", 
                    JOptionPane.INFORMATION_MESSAGE);
                
                // Reload reservations
                loadUpcomingReservations();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Reservation not found.", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
            
            findRs.close();
            findStmt.close();
            conn.close();
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, 
                "Error cancelling reservation: " + ex.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Helper class for buttons in JTable
    private class ButtonColumn extends AbstractCellEditor implements TableCellRenderer, TableCellEditor, ActionListener {
        private JTable table;
        private Action action;
        private JButton renderButton;
        private JButton editButton;
        private String text;
        
        public ButtonColumn(JTable table, Action action, int column) {
            this.table = table;
            this.action = action;
            
            renderButton = new JButton();
            editButton = new JButton();
            editButton.setFocusPainted(false);
            editButton.addActionListener(this);
            
            TableColumnModel columnModel = table.getColumnModel();
            columnModel.getColumn(column).setCellRenderer(this);
            columnModel.getColumn(column).setCellEditor(this);
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value == null) {
                renderButton.setText("");
            } else {
                renderButton.setText(value.toString());
            }
            return renderButton;
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            text = (value == null) ? "" : value.toString();
            editButton.setText(text);
            return editButton;
        }
        
        @Override
        public Object getCellEditorValue() {
            return text;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            int row = table.convertRowIndexToModel(table.getEditingRow());
            fireEditingStopped();
            
            ActionEvent event = new ActionEvent(
                table,
                ActionEvent.ACTION_PERFORMED,
                "" + row);
            
            action.actionPerformed(event);
        }
    }
    
    // Simple profile update screen (would be expanded in a real application)
    private class ProfileUpdateScreen extends JDialog {
        private User user;
        private JTextField phoneField;
        private JCheckBox showEmailCheckbox;
        private JCheckBox showPhoneCheckbox;
        private JButton saveButton;
        private JButton cancelButton;
        
        public ProfileUpdateScreen(User user) {
            super((JFrame)null, "Update Profile", true);
            this.user = user;
            
            initComponents();
            setupLayout();
            setupListeners();
            loadCurrentSettings();
            
            setSize(400, 300);
            setLocationRelativeTo(null);
        }
        
        private void initComponents() {
            phoneField = new JTextField(20);
            showEmailCheckbox = new JCheckBox("Show email in club directory");
            showPhoneCheckbox = new JCheckBox("Show phone in club directory");
            saveButton = new JButton("Save");
            cancelButton = new JButton("Cancel");
        }
        
        private void setupLayout() {
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(5, 5, 5, 5);
            
            gbc.gridx = 0;
            gbc.gridy = 0;
            panel.add(new JLabel("Phone:"), gbc);
            
            gbc.gridx = 1;
            gbc.gridy = 0;
            panel.add(phoneField, gbc);
            
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.gridwidth = 2;
            panel.add(showEmailCheckbox, gbc);
            
            gbc.gridx = 0;
            gbc.gridy = 2;
            panel.add(showPhoneCheckbox, gbc);
            
            gbc.gridx = 0;
            gbc.gridy = 3;
            gbc.gridwidth = 1;
            gbc.anchor = GridBagConstraints.EAST;
            panel.add(saveButton, gbc);
            
            gbc.gridx = 1;
            gbc.gridy = 3;
            gbc.anchor = GridBagConstraints.WEST;
            panel.add(cancelButton, gbc);
            
            getContentPane().add(panel);
        }
        
        private void setupListeners() {
            saveButton.addActionListener(e -> {
                if (saveProfile()) {
                    dispose();
                }
            });
            
            cancelButton.addActionListener(e -> dispose());
        }
        
        private void loadCurrentSettings() {
            try {
                Connection conn = dbConnection.getConnection();
                
                String query = "SELECT phone, show_email, show_phone FROM Members WHERE member_id = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, user.getMemberId());
                
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    phoneField.setText(rs.getString("phone"));
                    showEmailCheckbox.setSelected(rs.getBoolean("show_email"));
                    showPhoneCheckbox.setSelected(rs.getBoolean("show_phone"));
                }
                
                rs.close();
                stmt.close();
                conn.close();
                
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, 
                    "Error loading profile settings: " + ex.getMessage(), 
                    "Database Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
        
        private boolean saveProfile() {
            try {
                Connection conn = dbConnection.getConnection();
                
                String query = "UPDATE Members SET phone = ?, show_email = ?, show_phone = ? WHERE member_id = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, phoneField.getText().trim());
                stmt.setBoolean(2, showEmailCheckbox.isSelected());
                stmt.setBoolean(3, showPhoneCheckbox.isSelected());
                stmt.setInt(4, user.getMemberId());
                
                int rowsUpdated = stmt.executeUpdate();
                
                stmt.close();
                conn.close();
                
                if (rowsUpdated > 0) {
                    JOptionPane.showMessageDialog(this, 
                        "Profile updated successfully.", 
                        "Success", 
                        JOptionPane.INFORMATION_MESSAGE);
                    return true;
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Failed to update profile.", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, 
                    "Error saving profile: " + ex.getMessage(), 
                    "Database Error", 
                    JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
    }
    
    // Simplified court schedule screen
    private class CourtScheduleScreen extends JDialog {
        private User user;
        private JTable scheduleTable;
        private DefaultTableModel scheduleTableModel;
        private JComboBox<String> dateComboBox;
        
        public CourtScheduleScreen(User user) {
            super((JFrame)null, "Court Schedule", true);
            this.user = user;
            
            initComponents();
            setupLayout();
            setupListeners();
            loadSchedule();
            
            setSize(800, 600);
            setLocationRelativeTo(null);
        }
        
        private void initComponents() {
            // Date selector
            dateComboBox = new JComboBox<>();
            
            // Populate with next 7 days
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
            Calendar cal = Calendar.getInstance();
            
            for (int i = 0; i < 7; i++) {
                dateComboBox.addItem(dateFormat.format(cal.getTime()));
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }
            
            // Schedule table
            String[] columns = {"Court", "7 AM", "8 AM", "9 AM", "10 AM", "11 AM", "12 PM", 
                              "1 PM", "2 PM", "3 PM", "4 PM", "5 PM", "6 PM", "7 PM", "8 PM"};
            
            scheduleTableModel = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            
            scheduleTable = new JTable(scheduleTableModel);
            scheduleTable.setDefaultRenderer(Object.class, new ScheduleCellRenderer());
            
            // Add courts
            for (int i = 1; i <= 12; i++) {
                Object[] rowData = new Object[columns.length];
                rowData[0] = "Court " + i;
                for (int j = 1; j < columns.length; j++) {
                    rowData[j] = "";
                }
                scheduleTableModel.addRow(rowData);
            }
        }
        
        private void setupLayout() {
            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            
            // Date selector at the top
            JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            topPanel.add(new JLabel("Select Date:"));
            topPanel.add(dateComboBox);
            
            panel.add(topPanel, BorderLayout.NORTH);
            
            // Schedule table in the center
            JScrollPane scrollPane = new JScrollPane(scheduleTable);
            panel.add(scrollPane, BorderLayout.CENTER);
            
            // Close button at the bottom
            JButton closeButton = new JButton("Close");
            closeButton.addActionListener(e -> dispose());
            
            JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            bottomPanel.add(closeButton);
            
            panel.add(bottomPanel, BorderLayout.SOUTH);
            
            getContentPane().add(panel);
        }
        
        private void setupListeners() {
            dateComboBox.addActionListener(e -> loadSchedule());
        }
        
        private void loadSchedule() {
            // Clear existing reservations
            for (int row = 0; row < scheduleTableModel.getRowCount(); row++) {
                for (int col = 1; col < scheduleTableModel.getColumnCount(); col++) {
                    scheduleTableModel.setValueAt("", row, col);
                }
            }
            
            try {
                Connection conn = dbConnection.getConnection();
                
                // Get selected date
                String dateStr = (String) dateComboBox.getSelectedItem();
                
                // Get reservations for the selected date
                String query = "SELECT c.court_number, r.start_time, r.end_time, r.reservation_type, " +
                              "m.first_name, m.last_name " +
                              "FROM Reservations r " +
                              "JOIN Courts c ON r.court_id = c.court_id " +
                              "JOIN Members m ON r.member_id = m.member_id " +
                              "WHERE r.reservation_date = STR_TO_DATE(?, '%m/%d/%Y') " +
                              "ORDER BY c.court_number, r.start_time";
                
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, dateStr);
                
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    int courtNumber = rs.getInt("court_number");
                    Time startTime = rs.getTime("start_time");
                    Time endTime = rs.getTime("end_time");
                    String type = rs.getString("reservation_type");
                    String firstName = rs.getString("first_name");
                    String lastName = rs.getString("last_name");
                    
                    // Calculate which columns to fill
                    int startHour = startTime.getHours();
                    int endHour = endTime.getHours();
                    
                    if (endTime.getMinutes() > 0) {
                        endHour++; // Round up to cover partial hours
                    }
                    
                    // The first column is the court number, so we start at column 1
                    // Column 1 corresponds to 7 AM, column 2 to 8 AM, etc.
                    int startCol = startHour - 7 + 1;
                    int endCol = endHour - 7 + 1;
                    
                    if (startCol < 1) startCol = 1;
                    if (endCol > scheduleTableModel.getColumnCount() - 1) {
                        endCol = scheduleTableModel.getColumnCount() - 1;
                    }
                    
                    // Find the row for this court
                    int row = courtNumber - 1;
                    
                    // Fill in reservation info
                    String reservationInfo = type + ": " + firstName.charAt(0) + ". " + lastName;
                    
                    for (int col = startCol; col < endCol; col++) {
                        scheduleTableModel.setValueAt(reservationInfo, row, col);
                    }
                }
                
                rs.close();
                stmt.close();
                conn.close();
                
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, 
                    "Error loading schedule: " + ex.getMessage(), 
                    "Database Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
        
        // Custom cell renderer to color-code reservations
        private class ScheduleCellRenderer extends DefaultTableCellRenderer {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                                                         boolean isSelected, boolean hasFocus, 
                                                         int row, int column) {
                
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (column == 0) {
                    // Court column
                    c.setBackground(new Color(230, 230, 230));
                    c.setForeground(Color.BLACK);
                } else {
                    String text = (String) value;
                    
                    if (text.isEmpty()) {
                        // Available time slot
                        c.setBackground(Color.WHITE);
                        c.setForeground(Color.BLACK);
                    } else if (text.startsWith("Singles:")) {
                        // Singles reservation
                        c.setBackground(new Color(173, 216, 230)); // Light blue
                        c.setForeground(Color.BLACK);
                    } else {
                        // Doubles reservation
                        c.setBackground(new Color(144, 238, 144)); // Light green
                        c.setForeground(Color.BLACK);
                    }
                }
                
                return c;
            }
        }
    }
}
