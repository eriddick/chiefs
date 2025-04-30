package Main;

import Main.MemberDashboard.ReservationEditDialog;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

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
    private JButton viewMemberListButton;
    

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
        this(user, new DatabaseConnection()); // Call the constructor with both parameters
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
        String[] reservationColumns = { "Court", "Date", "Time", "Type", "Players", "Actions" };
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

      Action editAction = new AbstractAction() {
          public void actionPerformed(ActionEvent e) {
              int row = Integer.parseInt(e.getActionCommand());
              editReservation(row); // This was incorrectly calling cancelReservation
          }
      };


        reservationsTable.getColumn("Actions").setCellRenderer(new ActionCellRenderer());
        reservationsTable.getColumn("Actions").setCellEditor(new ActionCellEditor(reservationsTable, editAction, cancelAction));


       // ButtonColumn buttonColumn = new ButtonColumn(reservationsTable, cancelAction, 5);

        // Initialize action buttons
        makeReservationButton = new JButton("Make Reservation");
        viewScheduleButton = new JButton("View Court Schedule");
        viewBillsButton = new JButton("View & Pay Bills");
        updateProfileButton = new JButton("Update Profile");
        viewMemberListButton = new JButton("View Member List");
    }

    public class ActionCellRenderer extends JPanel implements TableCellRenderer {
        private final JButton editButton = new JButton("Edit");
        private final JButton cancelButton = new JButton("Cancel");

        public ActionCellRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
            add(editButton);
            add(cancelButton);
            setOpaque(true);

        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                      boolean isSelected, boolean hasFocus,
                                                      int row, int column) {
            if (isSelected) {
                setBackground(table.getSelectionBackground());
            } else {
                setBackground(table.getBackground());
            }
            return this;
        }
    }

    public class ActionCellEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        private final JButton editButton = new JButton("Edit");
        private final JButton cancelButton = new JButton("Cancel");

        public ActionCellEditor(JTable table, Action editAction, Action cancelAction) {
            editButton.addActionListener(e -> {
                int row = table.getEditingRow();
                table.getSelectionModel().setSelectionInterval(row, row); // optional: ensure row stays selected
                editAction.actionPerformed(new ActionEvent(table, ActionEvent.ACTION_PERFORMED, String.valueOf(row)));
                fireEditingStopped();
            });

            cancelButton.addActionListener(e -> {
                int row = table.getEditingRow();
                table.getSelectionModel().setSelectionInterval(row, row);
                cancelAction.actionPerformed(new ActionEvent(table, ActionEvent.ACTION_PERFORMED, String.valueOf(row)));
                fireEditingStopped();
            });

            panel.add(editButton);
            panel.add(cancelButton);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                    boolean isSelected, int row, int column) {
            panel.setBackground(table.getSelectionBackground());
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return null;
        }
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
        quickActionsPanel.add(viewMemberListButton);

        topPanel.add(quickActionsPanel, gbc);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Set up reservations panel
        JScrollPane scrollPane = new JScrollPane(reservationsTable);
        TableColumn actionsColumn = reservationsTable.getColumn("Actions");
        actionsColumn.setPreferredWidth(160);
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

        viewMemberListButton.addActionListener(e -> {
            viewMemberlist();
        });

        viewBillsButton.addActionListener(e -> {
            syncBalanceWithBills();
            UserBillsDisplay billsDisplay = new UserBillsDisplay(this, currentUser);
            billsDisplay.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    // Refresh member info to update balance
                    loadMemberInfo();
                }
            });
            billsDisplay.setVisible(true);
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
                    "COALESCE((SELECT SUM(amount) FROM MembershipFees WHERE member_id = m.member_id AND is_paid = 0), 0) + "
                    +
                    "COALESCE((SELECT SUM(amount) FROM LateFees WHERE member_id = m.member_id AND is_paid = 0), 0) + " +
                    "COALESCE((SELECT SUM(amount) FROM GuestFees WHERE member_id = m.member_id AND is_paid = 0), 0) AS balance, "
                    +
                    "(4 - COALESCE((SELECT count FROM GuestPassCount WHERE member_id = m.member_id AND " +
                    "month = CAST(strftime('%m', 'now') AS INTEGER) AND year = CAST(strftime('%Y', 'now') AS INTEGER)), 0)) AS remaining_passes "
                    +
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
                syncBalanceWithBills();
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
    private void syncBalanceWithBills() {
        try {
            Connection conn = dbConnection.getConnection();
            conn.setAutoCommit(false);
            
            try {
                // First, get the current outstanding balance
                String balanceQuery = "SELECT " +
                    "COALESCE(SUM(CASE WHEN mf.is_paid = 0 THEN mf.amount ELSE 0 END), 0) + " +
                    "COALESCE(SUM(CASE WHEN lf.is_paid = 0 THEN lf.amount ELSE 0 END), 0) + " +
                    "COALESCE(SUM(CASE WHEN gf.is_paid = 0 THEN gf.amount ELSE 0 END), 0) AS total_balance " +
                    "FROM Members m " +
                    "LEFT JOIN MembershipFees mf ON m.member_id = mf.member_id " +
                    "LEFT JOIN LateFees lf ON m.member_id = lf.member_id " +
                    "LEFT JOIN GuestFees gf ON m.member_id = gf.member_id " +
                    "WHERE m.member_id = ?";
                    
                PreparedStatement balanceStmt = conn.prepareStatement(balanceQuery);
                balanceStmt.setInt(1, currentUser.getMemberId());
                ResultSet balanceRs = balanceStmt.executeQuery();
                
                double totalBalance = 0;
                if (balanceRs.next()) {
                    totalBalance = balanceRs.getDouble("total_balance");
                }
                
                balanceRs.close();
                balanceStmt.close();
                
                // Now check if there's an unpaid bill matching this amount
                String billQuery = "SELECT COUNT(*) AS bill_count FROM Bills " +
                                  "WHERE member_id = ? AND is_paid = 0 AND total_amount = ?";
                PreparedStatement billStmt = conn.prepareStatement(billQuery);
                billStmt.setInt(1, currentUser.getMemberId());
                billStmt.setDouble(2, totalBalance);
                
                ResultSet billRs = billStmt.executeQuery();
                int billCount = 0;
                if (billRs.next()) {
                    billCount = billRs.getInt("bill_count");
                }
                
                billRs.close();
                billStmt.close();
                
                // If we have a balance but no bill, create one
                if (totalBalance > 0 && billCount == 0) {
                    // Get any unbilled fees
                    String unbilledQuery = "SELECT " +
                        "mf.fee_id AS membership_fee_id, mf.amount AS membership_amount, " +
                        "lf.late_fee_id AS late_fee_id, lf.amount AS late_amount, " +
                        "gf.guest_fee_id AS guest_fee_id, gf.amount AS guest_amount " +
                        "FROM Members m " +
                        "LEFT JOIN MembershipFees mf ON m.member_id = mf.member_id AND mf.is_paid = 0 " +
                        "LEFT JOIN LateFees lf ON m.member_id = lf.member_id AND lf.is_paid = 0 " +
                        "LEFT JOIN GuestFees gf ON m.member_id = gf.member_id AND gf.is_paid = 0 " +
                        "WHERE m.member_id = ?";
                    
                    PreparedStatement unbilledStmt = conn.prepareStatement(unbilledQuery);
                    unbilledStmt.setInt(1, currentUser.getMemberId());
                    ResultSet unbilledRs = unbilledStmt.executeQuery();
                    
                    // Create a new bill
                    String createBillQuery = "INSERT INTO Bills (member_id, bill_date, total_amount, due_date, is_paid, sent_email) " +
                                           "VALUES (?, date('now'), ?, date('now', '+30 days'), 0, 0)";
                    
                    PreparedStatement createBillStmt = conn.prepareStatement(createBillQuery, Statement.RETURN_GENERATED_KEYS);
                    createBillStmt.setInt(1, currentUser.getMemberId());
                    createBillStmt.setDouble(2, totalBalance);
                    createBillStmt.executeUpdate();
                    
                    ResultSet generatedKeys = createBillStmt.getGeneratedKeys();
                    int billId = -1;
                    if (generatedKeys.next()) {
                        billId = generatedKeys.getInt(1);
                    }
                    generatedKeys.close();
                    createBillStmt.close();
                    
                    // Add bill items for each unbilled fee
                    while (unbilledRs.next() && billId != -1) {
                        // Check membership fee
                        int membershipFeeId = unbilledRs.getInt("membership_fee_id");
                        if (!unbilledRs.wasNull() && membershipFeeId > 0) {
                            double amount = unbilledRs.getDouble("membership_amount");
                            addBillItem(conn, billId, "Membership Fee", amount, "MEMBERSHIP_FEE", membershipFeeId);
                        }
                        
                        // Check late fee
                        int lateFeeId = unbilledRs.getInt("late_fee_id");
                        if (!unbilledRs.wasNull() && lateFeeId > 0) {
                            double amount = unbilledRs.getDouble("late_amount");
                            addBillItem(conn, billId, "Late Fee", amount, "LATE_FEE", lateFeeId);
                        }
                        
                        // Check guest fee
                        int guestFeeId = unbilledRs.getInt("guest_fee_id");
                        if (!unbilledRs.wasNull() && guestFeeId > 0) {
                            double amount = unbilledRs.getDouble("guest_amount");
                            addBillItem(conn, billId, "Guest Fee", amount, "GUEST_FEE", guestFeeId);
                        }
                    }
                    
                    unbilledRs.close();
                    unbilledStmt.close();
                }
                
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
                conn.close();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error synchronizing balance with bills: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // 2. Helper method to add bill items
    private void addBillItem(Connection conn, int billId, String description, double amount, 
                             String itemType, int referenceId) throws SQLException {
        String query = "INSERT INTO BillItems (bill_id, description, amount, item_type, reference_id) " +
                      "VALUES (?, ?, ?, ?, ?)";
        
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, billId);
        stmt.setString(2, description);
        stmt.setDouble(3, amount);
        stmt.setString(4, itemType);
        stmt.setInt(5, referenceId);
        stmt.executeUpdate();
        stmt.close();
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
                    "WHERE r.member_id = ? AND r.reservation_date >= date('now') " +
                    "GROUP BY r.reservation_id " +
                    "ORDER BY r.reservation_date, r.start_time";

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, currentUser.getMemberId());

            ResultSet rs = stmt.executeQuery();

            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

            while (rs.next()) {
                int reservationId = rs.getInt("reservation_id");
                int courtNumber = rs.getInt("court_number");
                String dateStr = rs.getString("reservation_date");
                String startTimeStr = rs.getString("start_time");
                String endTimeStr = rs.getString("end_time");
                String type = rs.getString("reservation_type");
                int participantCount = rs.getInt("participant_count");

                // Use the TimeParser utility to safely parse and format times
                String formattedStartTime = TimeParser.parseTimeToDisplay(startTimeStr);
                String formattedEndTime = TimeParser.parseTimeToDisplay(endTimeStr);
                String timeRange = formattedStartTime + " - " + formattedEndTime;

                // Format the date if possible
                String formattedDate = dateStr;
                try {
                    if (dateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
                        // Convert YYYY-MM-DD to MM/DD/YYYY
                        String[] parts = dateStr.split("-");
                        formattedDate = parts[1] + "/" + parts[2] + "/" + parts[0];
                    }
                } catch (Exception e) {
                    // If date parsing fails, use original
                    System.out.println("Error parsing date: " + dateStr);
                }

                reservationsTableModel.addRow(new Object[] {
                        "Court " + courtNumber,
                        formattedDate,
                        timeRange,
                        type,
                        participantCount + " players",
                        "Actions" // Changed to include editing option
                });
            }

            rs.close();
            stmt.close();
            conn.close();

        } catch (SQLException ex) {
            ex.printStackTrace(); // Print trace for debugging
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

            // reformat date for sqllite
            dateStr = dateStr.substring(6) + "-" + dateStr.substring(0, 2) + "-" + dateStr.substring(3, 5);

            int courtNumber = Integer.parseInt(court.replace("Court ", ""));

            // Get reservation ID
            String findQuery = "SELECT r.reservation_id FROM Reservations r " +
                    "JOIN Courts c ON r.court_id = c.court_id " +
                    "WHERE c.court_number = ? AND r.member_id = ? " +
                    "AND r.reservation_date = ? " +
                    "ORDER BY r.reservation_date, r.start_time";

            // test
            // System.out.println("P:" + courtNumber + "#" + currentUser.getMemberId() + "#"
            // + dateStr + "#");

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

    private void editReservation(int row) {
    try {
        Connection conn = dbConnection.getConnection();

        // Get the reservation ID and details
        String court = (String) reservationsTableModel.getValueAt(row, 0);
        String dateStr = (String) reservationsTableModel.getValueAt(row, 1);

        // Reformat date for SQLite
        String sqlDateStr = dateStr.substring(6) + "-" + dateStr.substring(0, 2) + "-" + dateStr.substring(3, 5);

        int courtNumber = Integer.parseInt(court.replace("Court ", ""));

        // Get reservation ID
        String findQuery = "SELECT r.reservation_id FROM Reservations r " +
                "JOIN Courts c ON r.court_id = c.court_id " +
                "WHERE c.court_number = ? AND r.member_id = ? " +
                "AND r.reservation_date = ? " +
                "ORDER BY r.reservation_date, r.start_time";

        PreparedStatement findStmt = conn.prepareStatement(findQuery);
        findStmt.setInt(1, courtNumber);
        findStmt.setInt(2, currentUser.getMemberId());
        findStmt.setString(3, sqlDateStr);

        ResultSet findRs = findStmt.executeQuery();

        if (findRs.next()) {
            int reservationId = findRs.getInt("reservation_id");
    
             // Create and show the reservation edit dialog - using the correct constructor
             ReservationEditDialog editDialog = new ReservationEditDialog(reservationId);

            editDialog.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    // This is the key addition - reload the reservation data when the dialog closes
                    loadUpcomingReservations();
                }
            });
            editDialog.setVisible(true);
        }else {
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
        "Error finding reservation: " + ex.getMessage(),
        "Database Error",
        JOptionPane.ERROR_MESSAGE);
    }
}




    // This class represents a participant in a reservation
// It can be either a member or a guest
class ParticipantEntry {
    private String type;
    private int memberId;
    private int guestId;
    private String name;
    private String email;

    public ParticipantEntry(String type, int memberId, int guestId, String name, String email) {
        this.type = type;
        this.memberId = memberId;
        this.guestId = guestId;
        this.name = name;
        this.email = email;
    }

    public String getType() {
        return type;
    }

    public int getMemberId() {
        return memberId;
    }

    public int getGuestId() {
        return guestId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}

    private void viewMemberlist() {
        JDialog dialog = new JDialog(this, "Member Directory", true);
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create the table with member information
        String[] columns = {"ID", "Name", "Email", "Phone", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable memberTable = new JTable(model);
        memberTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        memberTable.setAutoCreateRowSorter(true);

        // Add a search field at the top
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        // Add checkboxes for filtering
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JCheckBox showEmailCheckbox = new JCheckBox("Show only members who share email");
        JCheckBox showPhoneCheckbox = new JCheckBox("Show only members who share phone");
        filterPanel.add(showEmailCheckbox);
        filterPanel.add(showPhoneCheckbox);

        // Combine search and filter in a top panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(searchPanel, BorderLayout.NORTH);
        topPanel.add(filterPanel, BorderLayout.SOUTH);

        // Add components to the main panel
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(memberTable), BorderLayout.CENTER);

        // Add a close button at the bottom
        JButton closeButton = new JButton("Close");
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(closeButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        // Load member data (initial load)
        loadMemberDirectoryData(model, "", showEmailCheckbox.isSelected(), showPhoneCheckbox.isSelected());

        // Add listeners for search and filter
        searchButton.addActionListener(e ->
            loadMemberDirectoryData(model, searchField.getText(),
                                showEmailCheckbox.isSelected(),
                                showPhoneCheckbox.isSelected()));

        showEmailCheckbox.addActionListener(e ->
            loadMemberDirectoryData(model, searchField.getText(),
                                showEmailCheckbox.isSelected(),
                                showPhoneCheckbox.isSelected()));

        showPhoneCheckbox.addActionListener(e ->
            loadMemberDirectoryData(model, searchField.getText(),
                                showEmailCheckbox.isSelected(),
                                showPhoneCheckbox.isSelected()));

        // Add listener for enter key in search field
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    loadMemberDirectoryData(model, searchField.getText(),
                                        showEmailCheckbox.isSelected(),
                                        showPhoneCheckbox.isSelected());
                }
            }
        });

        // Add listener for close button
        closeButton.addActionListener(e -> dialog.dispose());

        dialog.getContentPane().add(panel);
        dialog.setVisible(true);
    }

    private void loadMemberDirectoryData(DefaultTableModel model, String searchTerm, boolean showEmailOnly, boolean showPhoneOnly) {
        model.setRowCount(0); // Clear existing data

        try {
            Connection conn = dbConnection.getConnection();

            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("SELECT m.member_id, m.first_name, m.last_name, m.email, m.phone, m.status, m.show_email, m.show_phone ")
                       .append("FROM Members m ")
                       .append("WHERE m.status != 'INACTIVE' ");

            // Add search condition if search term is provided
            if (!searchTerm.isEmpty()) {
                queryBuilder.append("AND (m.first_name LIKE ? OR m.last_name LIKE ? OR m.email LIKE ?) ");
            }

            // Add filters for email and phone visibility
            if (showEmailOnly) {
                queryBuilder.append("AND m.show_email = 1 ");
            }

            if (showPhoneOnly) {
                queryBuilder.append("AND m.show_phone = 1 ");
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
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int memberId = rs.getInt("member_id");
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                String email = rs.getString("email");
                String phone = rs.getString("phone");
                String status = rs.getString("status");
                boolean showEmail = rs.getBoolean("show_email");
                boolean showPhone = rs.getBoolean("show_phone");

                // Only display email/phone if the member has chosen to share it
                String displayEmail = showEmail ? email : "Not shared";
                String displayPhone = showPhone ? phone : "Not shared";

                model.addRow(new Object[]{
                    memberId,
                    firstName + " " + lastName,
                    displayEmail,
                    displayPhone,
                    status
                });
            }

            rs.close();
            stmt.close();
            conn.close();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null,
                    "Error loading member directory: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private ArrayList<String> getMemberList() {

        ArrayList<String> memberlist = new ArrayList<>();

        try {
            Connection conn = dbConnection.getConnection();

            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("SELECT Members.member_id, first_name, last_name ")
                    .append("FROM Members, Users ")
                    .append("WHERE Members.member_id = Users.member_id ")
                    .append("AND Users.role = 'MEMBER' ")
                    .append("AND status != 'INACTIVE' ")
                    .append("ORDER BY last_name, first_name");

            PreparedStatement stmt = conn.prepareStatement(queryBuilder.toString());

            ResultSet rs = stmt.executeQuery();

            memberlist.add("Member id  First Name  Last Name");

            while (rs.next()) {
                int memberId = rs.getInt("member_id");
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");

                memberlist.add(String.format("%15d %-12s %-20s", memberId, firstName.trim(), lastName.trim()));
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
        return memberlist;
    }

    // Helper class for buttons in JTable
    private class ButtonColumn extends AbstractCellEditor
            implements TableCellRenderer, TableCellEditor, ActionListener {
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
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            if (value == null) {
                renderButton.setText("");
            } else {
                renderButton.setText(value.toString());
            }
            return renderButton;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
                int column) {
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

    // Reservation Edit Dialog to handle member substitution
    class ReservationEditDialog extends JDialog {
        private int reservationId;
        private JTable participantsTable;
        private DefaultTableModel participantsTableModel;
        private JButton addParticipantButton;
        private JButton saveButton;
        private JButton cancelButton;

        private ArrayList<ParticipantEntry> participants = new ArrayList<>();

        public ReservationEditDialog(int reservationId) {
            super((JFrame) null, "Edit Reservation", true);
            this.reservationId = reservationId;

            initComponents();
            setupLayout();
            setupListeners();
            loadReservationParticipants();

            setSize(600, 400);
            setLocationRelativeTo(null);
        }

        private void initComponents() {
            // Participants table
            String[] participantColumns = { "Type", "Name", "Email", "Remove" };
            participantsTableModel = new DefaultTableModel(participantColumns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return column == 3; // Only "Remove" column is editable
                }
            };
            participantsTable = new JTable(participantsTableModel);

            // Buttons
            addParticipantButton = new JButton("Substitute Member");
            saveButton = new JButton("Save Changes");
            cancelButton = new JButton("Cancel");

            // Set up the "Remove" button in the table
            Action removeAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    int modelRow = Integer.parseInt(e.getActionCommand());
                    if (modelRow == 0) {
                        JOptionPane.showMessageDialog(ReservationEditDialog.this,
                                "Cannot remove the primary reservation holder.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    participants.remove(modelRow);
                    participantsTableModel.removeRow(modelRow);
                }
            };

            ButtonColumn buttonColumn = new ButtonColumn(participantsTable, removeAction, 3);
        }

        private void setupLayout() {
            JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
            mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            // Title at the top
            JLabel titleLabel = new JLabel("Edit Reservation Participants", SwingConstants.CENTER);
            titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
            mainPanel.add(titleLabel, BorderLayout.NORTH);

            // Participants table in the center
            JScrollPane scrollPane = new JScrollPane(participantsTable);
            mainPanel.add(scrollPane, BorderLayout.CENTER);

            // Buttons at the bottom
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.add(addParticipantButton);
            buttonPanel.add(saveButton);
            buttonPanel.add(cancelButton);
            mainPanel.add(buttonPanel, BorderLayout.SOUTH);

            getContentPane().add(mainPanel);
        }
        private boolean saveReservationChanges() {
            // Try up to 3 times with increasing timeouts
            for (int attempt = 1; attempt <= 3; attempt++) {
                Connection conn = null;
                
                try {
                    // Force garbage collection before attempting database operation
                    System.gc();
                    Thread.sleep(attempt * 200); // Increasing delay with each attempt
                    
                    // Get a new connection
                    conn = dbConnection.getConnection();
                    
                    // Set busy timeout, increasing with each attempt
                    Statement stmt = conn.createStatement();
                    stmt.execute("PRAGMA busy_timeout = " + (attempt * 5000) + ";"); // 5, 10, 15 seconds
                    stmt.close();
                    
                    // Start transaction
                    conn.setAutoCommit(false);
                    
                    // 1. Delete all participants except primary member
                    String deleteQuery = "DELETE FROM ReservationParticipants " +
                        "WHERE reservation_id = ? " +
                        "AND participant_id NOT IN " +
                        "(SELECT MIN(participant_id) FROM ReservationParticipants " +
                        "WHERE reservation_id = ?)";
                    
                    PreparedStatement pstmt = conn.prepareStatement(deleteQuery);
                    pstmt.setInt(1, reservationId);
                    pstmt.setInt(2, reservationId);
                    pstmt.executeUpdate();
                    pstmt.close();
                    
                    // 2. Add new participants
                    for (int i = 1; i < participants.size(); i++) {
                        ParticipantEntry participant = participants.get(i);
                        
                        if ("Member".equals(participant.getType())) {
                            // Add member participant
                            PreparedStatement memberStmt = conn.prepareStatement(
                                "INSERT INTO ReservationParticipants " +
                                "(reservation_id, member_id, guest_id) VALUES (?, ?, NULL)");
                            memberStmt.setInt(1, reservationId);
                            memberStmt.setInt(2, participant.getMemberId());
                            memberStmt.executeUpdate();
                            memberStmt.close();
                        } else {
                            // Add guest participant
                            PreparedStatement guestStmt = conn.prepareStatement(
                                "INSERT INTO ReservationParticipants " +
                                "(reservation_id, member_id, guest_id) VALUES (?, NULL, ?)");
                            guestStmt.setInt(1, reservationId);
                            guestStmt.setInt(2, participant.getGuestId());
                            guestStmt.executeUpdate();
                            guestStmt.close();
                        }
                    }
                    
                    // Commit transaction
                    conn.commit();
                    
                    JOptionPane.showMessageDialog(this, 
                        "Reservation updated successfully.",
                        "Success", 
                        JOptionPane.INFORMATION_MESSAGE);
                    
                    return true;
                } catch (SQLException ex) {
                    // If this is a database locked error and not our last attempt, try again
                    if (ex.getMessage().contains("locked") && attempt < 3) {
                        System.out.println("Database locked, retry attempt " + (attempt + 1));
                        
                        // Roll back if needed
                        if (conn != null) {
                            try {
                                conn.rollback();
                            } catch (SQLException rollbackEx) {
                                System.err.println("Error rolling back: " + rollbackEx.getMessage());
                            }
                        }
                        
                        // Continue to next attempt
                        continue;
                    }
                    
                    // Otherwise, show error
                    if (conn != null) {
                        try {
                            conn.rollback();
                        } catch (SQLException rollbackEx) {
                            System.err.println("Error rolling back: " + rollbackEx.getMessage());
                        }
                    }
                    
                    JOptionPane.showMessageDialog(this,
                        "Error updating reservation: " + ex.getMessage() + 
                        "\nPlease close other windows or try again later.",
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
                    return false;
                } catch (InterruptedException ex) {
                    JOptionPane.showMessageDialog(this,
                        "Operation interrupted: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                    Thread.currentThread().interrupt();
                    return false;
                } finally {
                    if (conn != null) {
                        try {
                            // Reset auto-commit
                            conn.setAutoCommit(true);
                            conn.close();
                        } catch (SQLException ex) {
                            System.err.println("Error closing connection: " + ex.getMessage());
                        }
                    }
                }
            }
            
            // If we get here, all attempts failed
            JOptionPane.showMessageDialog(this,
                "Unable to update reservation after multiple attempts.\n" +
                "The database may be in use by another process.",
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }

                

        
        

        private void setupListeners() {
            addParticipantButton.addActionListener(e -> showAddParticipantDialog());

            saveButton.addActionListener(e -> {
                if (saveReservationChanges()) {
                    dispose();
                }
            });

            cancelButton.addActionListener(e -> dispose());
        }

        private void loadReservationParticipants() {
            participantsTableModel.setRowCount(0);
            participants.clear();

            try {
                Connection conn = dbConnection.getConnection();

                // Get reservation type
                String typeQuery = "SELECT reservation_type FROM Reservations WHERE reservation_id = ?";
                PreparedStatement typeStmt = conn.prepareStatement(typeQuery);
                typeStmt.setInt(1, reservationId);
                ResultSet typeRs = typeStmt.executeQuery();

                String reservationType = "Singles";
                if (typeRs.next()) {
                    reservationType = typeRs.getString("reservation_type");
                }

                typeRs.close();
                typeStmt.close();

                // Get participants - first get members
                String memberQuery = "SELECT rp.participant_id, m.member_id, m.first_name, m.last_name, m.email " +
                        "FROM ReservationParticipants rp " +
                        "JOIN Members m ON rp.member_id = m.member_id " +
                        "WHERE rp.reservation_id = ? AND rp.member_id IS NOT NULL " +
                        "ORDER BY rp.participant_id";

                PreparedStatement memberStmt = conn.prepareStatement(memberQuery);
                memberStmt.setInt(1, reservationId);
                ResultSet memberRs = memberStmt.executeQuery();

                while (memberRs.next()) {
                    int memberId = memberRs.getInt("member_id");
                    String firstName = memberRs.getString("first_name");
                    String lastName = memberRs.getString("last_name");
                    String email = memberRs.getString("email");
                    String fullName = firstName + " " + lastName;

                    ParticipantEntry entry = new ParticipantEntry(
                            "Member",
                            memberId,
                            0, // Guest ID is 0 for members
                            fullName,
                            email);

                    participants.add(entry);
                    participantsTableModel.addRow(new Object[] {
                            "Member",
                            fullName,
                            email,
                            "Remove"
                    });
                }

                memberRs.close();
                memberStmt.close();

                // Then get guests
                String guestQuery = "SELECT rp.participant_id, g.guest_id, g.first_name, g.last_name, g.email, g.host_member_id " +
                        "FROM ReservationParticipants rp " +
                        "JOIN Guests g ON rp.guest_id = g.guest_id " +
                        "WHERE rp.reservation_id = ? " +
                        "ORDER BY rp.participant_id";

                PreparedStatement guestStmt = conn.prepareStatement(guestQuery);
                guestStmt.setInt(1, reservationId);
                ResultSet guestRs = guestStmt.executeQuery();

                while (guestRs.next()) {
                    int guestId = guestRs.getInt("guest_id");
                    int hostMemberId = guestRs.getInt("host_member_id");
                    String firstName = guestRs.getString("first_name");
                    String lastName = guestRs.getString("last_name");
                    String email = guestRs.getString("email");
                    String fullName = firstName + " " + lastName;

                    ParticipantEntry entry = new ParticipantEntry(
                            "Guest",
                            hostMemberId,
                            guestId,
                            fullName,
                            email);

                    participants.add(entry);
                    participantsTableModel.addRow(new Object[] {
                            "Guest",
                            fullName,
                            email,
                            "Remove"
                    });
                }

                guestRs.close();
                guestStmt.close();

                conn.close();

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                        "Error loading reservation participants: " + ex.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }

        private void showAddParticipantDialog() {
            JDialog dialog = new JDialog(this, "Add Member Participant", true);
            dialog.setSize(400, 300);
            dialog.setLocationRelativeTo(this);

            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            // Search field
            JTextField searchField = new JTextField(20);
            JButton searchButton = new JButton("Search");

            JPanel searchPanel = new JPanel(new BorderLayout(5, 5));
            searchPanel.add(new JLabel("Search Member:"), BorderLayout.WEST);
            searchPanel.add(searchField, BorderLayout.CENTER);
            searchPanel.add(searchButton, BorderLayout.EAST);

            panel.add(searchPanel, BorderLayout.NORTH);

            // Results table
            String[] columns = { "Member ID", "Name", "Email" };
            DefaultTableModel model = new DefaultTableModel(columns, 0);
            JTable resultsTable = new JTable(model);
            resultsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            JScrollPane scrollPane = new JScrollPane(resultsTable);
            panel.add(scrollPane, BorderLayout.CENTER);

            // Add button
            JButton addButton = new JButton("Add Selected Member");
            addButton.setEnabled(false);

            resultsTable.getSelectionModel().addListSelectionListener(e -> {
                addButton.setEnabled(resultsTable.getSelectedRow() != -1);
            });

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.add(addButton);
            panel.add(buttonPanel, BorderLayout.SOUTH);

            dialog.getContentPane().add(panel);

            // Search button action
            searchButton.addActionListener(e -> {
                String searchTerm = searchField.getText().trim();
                if (searchTerm.isEmpty()) {
                    return;
                }

                model.setRowCount(0);

                try {
                    Connection conn = dbConnection.getConnection();
                    String query = "SELECT member_id, first_name, last_name, email FROM Members " +
                            "WHERE (first_name LIKE ? OR last_name LIKE ? OR email LIKE ?) ";

                    PreparedStatement stmt = conn.prepareStatement(query);
                    stmt.setString(1, "%" + searchTerm + "%");
                    stmt.setString(2, "%" + searchTerm + "%");
                    stmt.setString(3, "%" + searchTerm + "%");

                    ResultSet rs = stmt.executeQuery();

                    while (rs.next()) {
                        int memberId = rs.getInt("member_id");
                        String firstName = rs.getString("first_name");
                        String lastName = rs.getString("last_name");
                        String email = rs.getString("email");

                        // Check if this member is already in the participants list
                        boolean alreadyAdded = false;
                        for (ParticipantEntry entry : participants) {
                            if ("Member".equals(entry.getType()) && entry.getMemberId() == memberId) {
                                alreadyAdded = true;
                                break;
                            }
                        }

                        if (!alreadyAdded) {
                            model.addRow(new Object[] {
                                    memberId,
                                    firstName + " " + lastName,
                                    email
                            });
                        }
                    }

                    rs.close();
                    stmt.close();
                    conn.close();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(dialog,
                            "Error searching members: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            });

            // Add button action
            addButton.addActionListener(e -> {
                int selectedRow = resultsTable.getSelectedRow();
                if (selectedRow != -1) {
                    int memberId = (int) model.getValueAt(selectedRow, 0);
                    String name = (String) model.getValueAt(selectedRow, 1);
                    String email = (String) model.getValueAt(selectedRow, 2);

                    ParticipantEntry entry = new ParticipantEntry(
                            "Member",
                            memberId,
                            0, // Guest ID is 0 for members
                            name,
                            email);

                    participants.add(entry);
                    participantsTableModel.addRow(new Object[] {
                            "Member",
                            name,
                            email,
                            "Remove"
                    });

                    dialog.dispose();
                }
            });

            dialog.setVisible(true);
        }

       
    }

    // Enhanced profile update screen with email and password change functionality
    private class ProfileUpdateScreen extends JDialog {
        private User user;
        private JTextField emailField;
        private JTextField phoneField;
        private JCheckBox showEmailCheckbox;
        private JCheckBox showPhoneCheckbox;
        private JButton changePasswordButton;
        private JButton saveButton;
        private JButton cancelButton;

        public ProfileUpdateScreen(User user) {
            super((JFrame) null, "Update Profile", true);
            this.user = user;

            initComponents();
            setupLayout();
            setupListeners();
            loadCurrentSettings();

            setSize(400, 350); // Increased size to accommodate new fields
            setLocationRelativeTo(null);
        }

        private void initComponents() {
            emailField = new JTextField(20);
            phoneField = new JTextField(20);
            showEmailCheckbox = new JCheckBox("Show email in club directory");
            showPhoneCheckbox = new JCheckBox("Show phone in club directory");
            changePasswordButton = new JButton("Change Password");
            saveButton = new JButton("Save");
            cancelButton = new JButton("Cancel");
        }

        private void setupLayout() {
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(5, 5, 5, 5);

            // Email field
            gbc.gridx = 0;
            gbc.gridy = 0;
            panel.add(new JLabel("Email:"), gbc);

            gbc.gridx = 1;
            gbc.gridy = 0;
            panel.add(emailField, gbc);

            // Phone field
            gbc.gridx = 0;
            gbc.gridy = 1;
            panel.add(new JLabel("Phone:"), gbc);

            gbc.gridx = 1;
            gbc.gridy = 1;
            panel.add(phoneField, gbc);

            // Email checkbox
            gbc.gridx = 0;
            gbc.gridy = 2;
            gbc.gridwidth = 2;
            panel.add(showEmailCheckbox, gbc);

            // Phone checkbox
            gbc.gridx = 0;
            gbc.gridy = 3;
            panel.add(showPhoneCheckbox, gbc);

            // Change password button
            gbc.gridx = 0;
            gbc.gridy = 4;
            gbc.gridwidth = 2;
            gbc.fill = GridBagConstraints.NONE;
            gbc.anchor = GridBagConstraints.CENTER;
            panel.add(changePasswordButton, gbc);

            // Save and cancel buttons
            gbc.gridx = 0;
            gbc.gridy = 5;
            gbc.gridwidth = 1;
            gbc.anchor = GridBagConstraints.EAST;
            panel.add(saveButton, gbc);

            gbc.gridx = 1;
            gbc.gridy = 5;
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

            changePasswordButton.addActionListener(e -> showChangePasswordDialog());
        }

        private void loadCurrentSettings() {
            try {
                Connection conn = dbConnection.getConnection();

                String query = "SELECT email, phone, show_email, show_phone FROM Members WHERE member_id = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, user.getMemberId());

                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    emailField.setText(rs.getString("email"));
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
                // Basic email validation
                String email = emailField.getText().trim();
                if (email.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "Email cannot be empty.",
                            "Validation Error",
                            JOptionPane.ERROR_MESSAGE);
                    return false;
                }

                if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
                    JOptionPane.showMessageDialog(this,
                            "Please enter a valid email address.",
                            "Validation Error",
                            JOptionPane.ERROR_MESSAGE);
                    return false;
                }

                Connection conn = dbConnection.getConnection();

                String query = "UPDATE Members SET email = ?, phone = ?, show_email = ?, show_phone = ? WHERE member_id = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, email);
                stmt.setString(2, phoneField.getText().trim());
                stmt.setBoolean(3, showEmailCheckbox.isSelected());
                stmt.setBoolean(4, showPhoneCheckbox.isSelected());
                stmt.setInt(5, user.getMemberId());

                int rowsUpdated = stmt.executeUpdate();

                // Update the User object with the new email
                if (rowsUpdated > 0) {
                    // Also update the email field in User object if it's accessible
                    // This might require adding a setter method to the User class or updating how current user info is stored
                }

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

        private void showChangePasswordDialog() {
            JDialog dialog = new JDialog(this, "Change Password", true);
            dialog.setSize(350, 250);
            dialog.setLocationRelativeTo(this);

            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(5, 5, 5, 5);

            // Current password field
            gbc.gridx = 0;
            gbc.gridy = 0;
            panel.add(new JLabel("Current Password:"), gbc);

            gbc.gridx = 1;
            gbc.gridy = 0;
            JPasswordField currentPasswordField = new JPasswordField(15);
            panel.add(currentPasswordField, gbc);

            // New password field
            gbc.gridx = 0;
            gbc.gridy = 1;
            panel.add(new JLabel("New Password:"), gbc);

            gbc.gridx = 1;
            gbc.gridy = 1;
            JPasswordField newPasswordField = new JPasswordField(15);
            panel.add(newPasswordField, gbc);

            // Confirm password field
            gbc.gridx = 0;
            gbc.gridy = 2;
            panel.add(new JLabel("Confirm Password:"), gbc);

            gbc.gridx = 1;
            gbc.gridy = 2;
            JPasswordField confirmPasswordField = new JPasswordField(15);
            panel.add(confirmPasswordField, gbc);

            // Buttons
            JButton saveButton = new JButton("Save");
            JButton cancelButton = new JButton("Cancel");

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.add(saveButton);
            buttonPanel.add(cancelButton);

            gbc.gridx = 0;
            gbc.gridy = 3;
            gbc.gridwidth = 2;
            panel.add(buttonPanel, gbc);

            dialog.getContentPane().add(panel);

            // Save button action
            saveButton.addActionListener(e -> {
                String currentPassword = new String(currentPasswordField.getPassword());
                String newPassword = new String(newPasswordField.getPassword());
                String confirmPassword = new String(confirmPasswordField.getPassword());

                if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog,
                            "All fields are required.",
                            "Validation Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (!newPassword.equals(confirmPassword)) {
                    JOptionPane.showMessageDialog(dialog,
                            "New password and confirm password do not match.",
                            "Validation Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (changePassword(currentPassword, newPassword)) {
                    dialog.dispose();
                }
            });

            // Cancel button action
            cancelButton.addActionListener(e -> dialog.dispose());

            dialog.setVisible(true);
        }

        private boolean changePassword(String currentPassword, String newPassword) {
            try {
                Connection conn = dbConnection.getConnection();

                // First verify the current password
                String verifyQuery = "SELECT COUNT(*) FROM Users WHERE member_id = ? AND password = ?";
                PreparedStatement verifyStmt = conn.prepareStatement(verifyQuery);
                verifyStmt.setInt(1, user.getMemberId());
                verifyStmt.setString(2, currentPassword);

                ResultSet verifyRs = verifyStmt.executeQuery();
                verifyRs.next();
                int count = verifyRs.getInt(1);

                verifyRs.close();
                verifyStmt.close();

                if (count == 0) {
                    JOptionPane.showMessageDialog(this,
                            "Current password is incorrect.",
                            "Authentication Error",
                            JOptionPane.ERROR_MESSAGE);
                    return false;
                }

                // Update the password
                String updateQuery = "UPDATE Users SET password = ? WHERE member_id = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                updateStmt.setString(1, newPassword);
                updateStmt.setInt(2, user.getMemberId());

                int rowsUpdated = updateStmt.executeUpdate();

                updateStmt.close();
                conn.close();

                if (rowsUpdated > 0) {
                    JOptionPane.showMessageDialog(this,
                            "Password changed successfully.",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    return true;
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Failed to change password.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return false;
                }

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                        "Error changing password: " + ex.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
    }

    // Simple profile update screen (would be expanded in a real application)
    class CourtScheduleScreen extends JDialog {
        private User user;
        private JTable scheduleTable;
        private DefaultTableModel scheduleTableModel;
        private JComboBox<String> dateComboBox;

        public CourtScheduleScreen(User user) {
            super((JFrame) null, "Court Schedule", true);
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
            String[] columns = { "Court", "7 AM", "8 AM", "9 AM", "10 AM", "11 AM", "12 PM",
                    "1 PM", "2 PM", "3 PM", "4 PM", "5 PM", "6 PM", "7 PM", "8 PM" };

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

                // Convert date format from MM/DD/YYYY to YYYY-MM-DD for SQLite
                String formattedDate = convertDateFormat(dateStr);

                System.out.println("Looking for reservations on date: " + formattedDate);

                // Get reservations for the selected date
                String query = "SELECT c.court_number, r.start_time, r.end_time, r.reservation_type, " +
                        "m.first_name, m.last_name, r.reservation_date " +
                        "FROM Reservations r " +
                        "JOIN Courts c ON r.court_id = c.court_id " +
                        "JOIN Members m ON r.member_id = m.member_id " +
                        "WHERE r.reservation_date = ? " +
                        "ORDER BY c.court_number, r.start_time";

                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, formattedDate);

                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    int courtNumber = rs.getInt("court_number");
                    String startTimeStr = rs.getString("start_time");
                    String endTimeStr = rs.getString("end_time");
                    String type = rs.getString("reservation_type");
                    String firstName = rs.getString("first_name");
                    String lastName = rs.getString("last_name");
                    String resDate = rs.getString("reservation_date");

                    System.out.println("Found reservation: Court " + courtNumber +
                            " on " + resDate +
                            " start time " + startTimeStr +
                            " end time " + endTimeStr);

                    // Use TimeParser to safely get hour values
                    int startHour = TimeParser.parseHour(startTimeStr);
                    int endHour = TimeParser.parseHour(endTimeStr);

                    System.out.println("Parsed hours: start=" + startHour + ", end=" + endHour);

                    if (TimeParser.parseMinutes(endTimeStr) > 0) {
                        endHour++; // Round up to cover partial hours
                    }

                    // The first column is the court number, so we start at column 1
                    // Column 1 corresponds to 7 AM, column 2 to 8 AM, etc.
                    int startCol = startHour - 7 + 1;
                    int endCol = endHour - 7 + 1;

                    if (startCol < 1)
                        startCol = 1;
                    if (endCol > scheduleTableModel.getColumnCount() - 1) {
                        endCol = scheduleTableModel.getColumnCount() - 1;
                    }

                    // Find the row for this court
                    int row = courtNumber - 1;
                    if (row >= 0 && row < scheduleTableModel.getRowCount()) {
                        // Fill in reservation info
                        String reservationInfo = type + ": " + firstName.charAt(0) + ". " + lastName;

                        for (int col = startCol; col < endCol; col++) {
                            if (col < scheduleTableModel.getColumnCount()) {
                                scheduleTableModel.setValueAt(reservationInfo, row, col);
                            }
                        }
                    }
                }

                rs.close();
                stmt.close();
                conn.close();

            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Error loading schedule: " + ex.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }

        // Helper method to convert date format
        private String convertDateFormat(String dateStr) {
            try {
                String[] parts = dateStr.split("/");
                if (parts.length == 3) {
                    // Convert from MM/DD/YYYY to YYYY-MM-DD
                    return parts[2] + "-" + parts[0] + "-" + parts[1];
                }
            } catch (Exception e) {
                // If parsing fails, return the original string
            }
            return dateStr;
        }

        private int parseHour(String timeStr) {
            try {
                return Integer.parseInt(timeStr.split(":")[0]);
            } catch (Exception e) {
                return 0;
            }
        }

        // Helper method to extract minutes from time string
        private int parseMinutes(String timeStr) {
            try {
                String[] parts = timeStr.split(":");
                if (parts.length >= 2) {
                    return Integer.parseInt(parts[1]);
                }
            } catch (Exception e) {
                // If parsing fails, return 0
            }
            return 0;
        }

        // Custom cell renderer to color-code reservations(google)
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
