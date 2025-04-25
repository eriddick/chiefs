package Main;

import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import javax.swing.*;
import javax.swing.table.*;

public class BillingSystem extends JFrame {
    private User currentUser;
    private DatabaseConnection dbConnection;
    
    private JTable billsTable;
    private DefaultTableModel billsTableModel;
    private JButton generateAnnualBillsButton;
    private JButton sendReminderButton;
    private JButton viewBillDetailsButton;
    private JButton markAsPaidButton;
    private JTextField searchField;
    private JButton searchButton;
    private JComboBox<String> filterComboBox;
    
    private boolean isTreasurer;
    
    public BillingSystem(User currentUser) {
        this.currentUser = currentUser;
        this.dbConnection = new DatabaseConnection();
        
        isTreasurer = "TREASURER".equals(currentUser.getRole());
        
        initComponents();
        setupLayout();
        setupListeners();
        loadBills();
        
        setTitle("Billing System");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
    
    private void initComponents() {
        // Create table columns
        String[] columns = {
            "Bill ID", "Member", "Date", "Amount", "Due Date", "Status", "Email Sent"
        };
        
        billsTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        billsTable = new JTable(billsTableModel);
        billsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        billsTable.setAutoCreateRowSorter(true);
        
        // Set column widths
        TableColumnModel columnModel = billsTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(60);  // Bill ID
        columnModel.getColumn(1).setPreferredWidth(150); // Member
        columnModel.getColumn(2).setPreferredWidth(80);  // Date
        columnModel.getColumn(3).setPreferredWidth(80);  // Amount
        columnModel.getColumn(4).setPreferredWidth(80);  // Due Date
        columnModel.getColumn(5).setPreferredWidth(80);  // Status
        columnModel.getColumn(6).setPreferredWidth(80);  // Email Sent
        
        // Buttons
        generateAnnualBillsButton = new JButton("Generate Annual Bills");
        sendReminderButton = new JButton("Send Reminder");
        viewBillDetailsButton = new JButton("View Details");
        markAsPaidButton = new JButton("Mark as Paid");
        
        // Disable buttons until a row is selected
        sendReminderButton.setEnabled(false);
        viewBillDetailsButton.setEnabled(false);
        markAsPaidButton.setEnabled(false);
        
        // Only treasurers can generate bills
        generateAnnualBillsButton.setVisible(isTreasurer);
        sendReminderButton.setVisible(isTreasurer);
        markAsPaidButton.setVisible(isTreasurer);
        
        // Search components
        searchField = new JTextField(20);
        searchButton = new JButton("Search");
        
        // Filter options
        String[] filterOptions = {"All Bills", "Unpaid Bills", "Paid Bills"};
        filterComboBox = new JComboBox<>(filterOptions);
    }
    
    private void setupLayout() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Search and filter panel at the top
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(Box.createHorizontalStrut(20));
        searchPanel.add(new JLabel("Filter:"));
        searchPanel.add(filterComboBox);
        
        mainPanel.add(searchPanel, BorderLayout.NORTH);
        
        // Bills table in the center
        JScrollPane scrollPane = new JScrollPane(billsTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Button panel at the bottom
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        if (isTreasurer) {
            buttonPanel.add(generateAnnualBillsButton);
            buttonPanel.add(sendReminderButton);
            buttonPanel.add(markAsPaidButton);
        }
        
        buttonPanel.add(viewBillDetailsButton);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        getContentPane().add(mainPanel);
    }
    
    private void setupListeners() {
        // Table selection listener
        billsTable.getSelectionModel().addListSelectionListener(e -> {
            boolean rowSelected = billsTable.getSelectedRow() != -1;
            viewBillDetailsButton.setEnabled(rowSelected);
            
            if (isTreasurer && rowSelected) {
                int modelRow = billsTable.convertRowIndexToModel(billsTable.getSelectedRow());
                String status = (String) billsTableModel.getValueAt(modelRow, 5);
                boolean isPaid = "Paid".equals(status);
                
                sendReminderButton.setEnabled(!isPaid);
                markAsPaidButton.setEnabled(!isPaid);
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
                int billId = (int) billsTableModel.getValueAt(modelRow, 0);
                sendBillReminder(billId);
            }
        });
        
        // View bill details button
        viewBillDetailsButton.addActionListener(e -> {
            int selectedRow = billsTable.getSelectedRow();
            if (selectedRow != -1) {
                int modelRow = billsTable.convertRowIndexToModel(selectedRow);
                int billId = (int) billsTableModel.getValueAt(modelRow, 0);
                showBillDetailsDialog(billId);
            }
        });
        
        // Mark as paid button
        markAsPaidButton.addActionListener(e -> {
            int selectedRow = billsTable.getSelectedRow();
            if (selectedRow != -1) {
                int modelRow = billsTable.convertRowIndexToModel(selectedRow);
                int billId = (int) billsTableModel.getValueAt(modelRow, 0);
                markBillAsPaid(billId);
            }
        });
        
        // Search button
        searchButton.addActionListener(e -> loadBills());
        
        // Filter combo box
        filterComboBox.addActionListener(e -> loadBills());
        
        // Also allow pressing Enter in search field
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    loadBills();
                }
            }
        });
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
            String filter = (String) filterComboBox.getSelectedItem();
            if ("Unpaid Bills".equals(filter)) {
                queryBuilder.append("AND b.is_paid = 0 ");
            } else if ("Paid Bills".equals(filter)) {
                queryBuilder.append("AND b.is_paid = 1 ");
            }
            
            // Add search condition if search field is not empty
            String searchTerm = searchField.getText().trim();
            if (!searchTerm.isEmpty()) {
                queryBuilder.append("AND (m.first_name LIKE ? OR m.last_name LIKE ?) ");
            }
            
            // If not treasurer, only show current user's bills
            if (!isTreasurer) {
                queryBuilder.append("AND m.member_id = ? ");
            }
            
            // Add ordering
            queryBuilder.append("ORDER BY b.due_date DESC, m.last_name, m.first_name");
            
            PreparedStatement stmt = conn.prepareStatement(queryBuilder.toString());
            
            int paramIndex = 1;
            
            // Set search parameters if needed
            if (!searchTerm.isEmpty()) {
                String likePattern = "%" + searchTerm + "%";
                stmt.setString(paramIndex++, likePattern);
                stmt.setString(paramIndex++, likePattern);
            }
            
            // Set member ID parameter if not treasurer
            if (!isTreasurer) {
                stmt.setInt(paramIndex++, currentUser.getMemberId());
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
                    billId,
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
            ex.printStackTrace();
        }
    }
    
    private void generateAnnualBills() {
        if (!isTreasurer) return;
        
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
                ex.printStackTrace();
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
        if (!isTreasurer) return;
        
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
        if (!isTreasurer) return;
        
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
                ex.printStackTrace();
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
