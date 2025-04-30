package Main;

import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.*;

/**
 * A dialog for displaying and managing user bills.
 */
public class UserBillsDisplay extends JDialog {
    private User currentUser;
    private DatabaseConnection dbConnection;
    
    private JTable billsTable;
    private DefaultTableModel billsTableModel;
    private JButton viewDetailsButton;
    private JButton payNowButton;
    private JButton refreshButton;
    private JButton closeButton;
    
    /**
     * Constructs a new bills display dialog.
     * 
     * @param parent The parent frame
     * @param currentUser The currently logged-in user
     */
    public UserBillsDisplay(JFrame parent, User currentUser) {
        super(parent, "My Bills", true);
        this.currentUser = currentUser;
        this.dbConnection = new DatabaseConnection();
        
        initComponents();
        setupLayout();
        setupListeners();
        loadBills();
        
        setSize(800, 500);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }
    
    /**
     * Initializes dialog components.
     */
    private void initComponents() {
        // Create table for bills
        String[] columns = {"Bill ID", "Date", "Description", "Amount", "Due Date", "Status"};
        
        billsTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 0) return Integer.class;
                return String.class;
            }
        };
        
        billsTable = new JTable(billsTableModel);
        billsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        billsTable.setAutoCreateRowSorter(true);
        
        // Set column widths
        TableColumnModel columnModel = billsTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(60);  // Bill ID
        columnModel.getColumn(1).setPreferredWidth(100); // Date
        columnModel.getColumn(2).setPreferredWidth(200); // Description
        columnModel.getColumn(3).setPreferredWidth(80);  // Amount
        columnModel.getColumn(4).setPreferredWidth(100); // Due Date
        columnModel.getColumn(5).setPreferredWidth(80);  // Status
        
        // Create buttons
        viewDetailsButton = new JButton("View Details");
        payNowButton = new JButton("Pay Now");
        refreshButton = new JButton("Refresh");
        closeButton = new JButton("Close");
        
        // Disable action buttons until a row is selected
        viewDetailsButton.setEnabled(false);
        payNowButton.setEnabled(false);
    }
    
    /**
     * Sets up the dialog layout.
     */
    private void setupLayout() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Title at top
        JLabel titleLabel = new JLabel("My Bills and Payments", SwingConstants.CENTER);
        titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.BOLD, 16));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Bills table in center
        JScrollPane scrollPane = new JScrollPane(billsTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Action buttons at bottom
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(viewDetailsButton);
        buttonPanel.add(payNowButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(closeButton);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        getContentPane().add(mainPanel);
    }
    
    /**
     * Sets up event listeners.
     */
    private void setupListeners() {
        // Table selection listener
        billsTable.getSelectionModel().addListSelectionListener(e -> {
            boolean rowSelected = billsTable.getSelectedRow() != -1;
            viewDetailsButton.setEnabled(rowSelected);
            
            if (rowSelected) {
                int modelRow = billsTable.convertRowIndexToModel(billsTable.getSelectedRow());
                String status = (String) billsTableModel.getValueAt(modelRow, 5);
                payNowButton.setEnabled("Unpaid".equals(status));
            } else {
                payNowButton.setEnabled(false);
            }
        });
        
        // View details button
        viewDetailsButton.addActionListener(e -> {
            int selectedRow = billsTable.getSelectedRow();
            if (selectedRow != -1) {
                int modelRow = billsTable.convertRowIndexToModel(selectedRow);
                int billId = (int) billsTableModel.getValueAt(modelRow, 0);
                showBillDetailsDialog(billId);
            }
        });
        
        // Pay now button
        payNowButton.addActionListener(e -> {
            int selectedRow = billsTable.getSelectedRow();
            if (selectedRow != -1) {
                int modelRow = billsTable.convertRowIndexToModel(selectedRow);
                int billId = (int) billsTableModel.getValueAt(modelRow, 0);
                String amountStr = ((String) billsTableModel.getValueAt(modelRow, 3)).replace("$", "");
                double amount = Double.parseDouble(amountStr);
                
                BillPaymentForm paymentForm = new BillPaymentForm((JFrame) SwingUtilities.getWindowAncestor(this), 
                        billId, amount, currentUser);
                paymentForm.setVisible(true);
                
                if (paymentForm.isPaymentSuccessful()) {
                    loadBills(); // Refresh bills list if payment was successful
                }
            }
        });
        
        // Refresh button
        refreshButton.addActionListener(e -> loadBills());
        
        // Close button
        closeButton.addActionListener(e -> dispose());
    }
    
    /**
     * Loads the user's bills from the database.
     */
    private void loadBills() {
        billsTableModel.setRowCount(0);
        
        try {
            Connection conn = dbConnection.getConnection();
            
            String query = "SELECT b.bill_id, b.bill_date, b.total_amount, b.due_date, b.is_paid, " +
                          "GROUP_CONCAT(bi.description, ', ') AS descriptions " +
                          "FROM Bills b " +
                          "LEFT JOIN BillItems bi ON b.bill_id = bi.bill_id " +
                          "WHERE b.member_id = ? " +
                          "GROUP BY b.bill_id " +
                          "ORDER BY b.is_paid ASC, b.due_date ASC";
            
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, currentUser.getMemberId());
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                int billId = rs.getInt("bill_id");
                String billDate = rs.getString("bill_date");
                double totalAmount = rs.getDouble("total_amount");
                String dueDate = rs.getString("due_date");
                boolean isPaid = rs.getBoolean("is_paid");
                String descriptions = rs.getString("descriptions");
                
                if (descriptions == null) descriptions = "Bill";
                
                // Format dates using the utility method
                String formattedBillDate = DateUtils.formatSqliteDate(billDate);
                String formattedDueDate = DateUtils.formatSqliteDate(dueDate);
                
                // Determine status and status style
                String status = isPaid ? "Paid" : "Unpaid";
                
                billsTableModel.addRow(new Object[] {
                    billId,
                    formattedBillDate,
                    descriptions,
                    String.format("$%.2f", totalAmount),
                    formattedDueDate,
                    status
                });
            }
            
            rs.close();
            stmt.close();
            conn.close();
            
            // Update table renderer with status colors
            billsTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value,
                        boolean isSelected, boolean hasFocus, int row, int column) {
                    
                    Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    
                    // Get the status value (from the model to handle sorting)
                    int modelRow = table.convertRowIndexToModel(row);
                    String status = (String) billsTableModel.getValueAt(modelRow, 5);
                    
                    // Only apply custom colors when not selected
                    if (!isSelected) {
                        // Set row background based on status
                        if ("Paid".equals(status)) {
                            c.setBackground(new Color(240, 255, 240)); // Light green
                        } else {
                            c.setBackground(Color.WHITE);
                        }
                        
                        // Set text color for status column
                        if (column == 5) {
                            if ("Paid".equals(status)) {
                                c.setForeground(new Color(0, 128, 0)); // Green
                            } else {
                                c.setForeground(new Color(192, 0, 0)); // Red
                            }
                        } else {
                            c.setForeground(Color.BLACK);
                        }
                    }
                    
                    return c;
                }
            });
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error loading bills: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Shows detailed information about the selected bill.
     * 
     * @param billId The ID of the bill to display
     */
    private void showBillDetailsDialog(int billId) {
        try {
            Connection conn = dbConnection.getConnection();
            
            // Get bill header information
            String billQuery = "SELECT b.bill_id, m.first_name, m.last_name, b.bill_date, b.total_amount, " +
                    "b.due_date, b.is_paid, b.sent_email " +
                    "FROM Bills b " +
                    "JOIN Members m ON b.member_id = m.member_id " +
                    "WHERE b.bill_id = ?";

            PreparedStatement billStmt = conn.prepareStatement(billQuery);
            billStmt.setInt(1, billId);

            ResultSet billRs = billStmt.executeQuery();

            if (billRs.next()) {
                String memberName = billRs.getString("first_name") + " " + billRs.getString("last_name");
                String billDate = billRs.getString("bill_date");
                double totalAmount = billRs.getDouble("total_amount");
                String dueDate = billRs.getString("due_date");
                boolean isPaid = billRs.getBoolean("is_paid");

                // Format dates using DateUtils
                String formattedBillDate = DateUtils.formatSqliteDate(billDate);
                String formattedDueDate = DateUtils.formatSqliteDate(dueDate);

                // Create dialog
                JDialog dialog = new JDialog(this, "Bill Details - #" + billId, true);
                dialog.setSize(600, 400);
                dialog.setLocationRelativeTo(this);

                JPanel panel = new JPanel(new BorderLayout(10, 10));
                panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                // Header panel
                JPanel headerPanel = new JPanel(new GridLayout(5, 2, 5, 5));
                headerPanel.setBorder(BorderFactory.createTitledBorder("Bill Information"));

                headerPanel.add(new JLabel("Member:"));
                headerPanel.add(new JLabel(memberName));

                headerPanel.add(new JLabel("Bill Date:"));
                headerPanel.add(new JLabel(formattedBillDate));

                headerPanel.add(new JLabel("Due Date:"));
                headerPanel.add(new JLabel(formattedDueDate));

                headerPanel.add(new JLabel("Status:"));
                JLabel statusLabel = new JLabel(isPaid ? "Paid" : "Unpaid");
                statusLabel.setForeground(isPaid ? new Color(0, 128, 0) : new Color(192, 0, 0));
                headerPanel.add(statusLabel);

                headerPanel.add(new JLabel("Total Amount:"));
                headerPanel.add(new JLabel(String.format("$%.2f", totalAmount)));

                panel.add(headerPanel, BorderLayout.NORTH);

                // Get bill items
                String itemsQuery = "SELECT description, amount, item_type FROM BillItems WHERE bill_id = ?";
                PreparedStatement itemsStmt = conn.prepareStatement(itemsQuery);
                itemsStmt.setInt(1, billId);

                ResultSet itemsRs = itemsStmt.executeQuery();

                // Create table for bill items
                String[] columns = { "Description", "Type", "Amount" };
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
                    double amount = itemsRs.getDouble("amount");
                    String itemType = itemsRs.getString("item_type");

                    // Format item type for display
                    String formattedType = itemType.replace('_', ' ');
                    if (formattedType.length() > 0) {
                        formattedType = formattedType.charAt(0) + formattedType.substring(1).toLowerCase();
                    }

                    itemsModel.addRow(new Object[] {
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
                    JButton payButton = new JButton("Pay This Bill");
                    payButton.addActionListener(e -> {
                        dialog.dispose();
                        BillPaymentForm paymentForm = new BillPaymentForm(
                                (JFrame) SwingUtilities.getWindowAncestor(this), 
                                billId, totalAmount, currentUser);
                        paymentForm.setVisible(true);
                        
                        if (paymentForm.isPaymentSuccessful()) {
                            loadBills(); // Refresh bills list if payment was successful
                        }
                    });
                    buttonPanel.add(payButton);
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
    
    /**
     * Displays a summary of the user's payment history.
     */
    private void showPaymentHistoryDialog() {
        try {
            Connection conn = dbConnection.getConnection();
            
            String query = "SELECT p.payment_id, p.payment_date, p.amount, p.payment_method, b.bill_id " +
                          "FROM Payments p " +
                          "JOIN Bills b ON p.bill_id = b.bill_id " +
                          "WHERE p.member_id = ? " +
                          "ORDER BY p.payment_date DESC";
                          
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, currentUser.getMemberId());
            
            ResultSet rs = stmt.executeQuery();
            
            // Create table model for payments
            String[] columns = {"Payment ID", "Date", "Amount", "Method", "Bill ID"};
            DefaultTableModel model = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            
            while (rs.next()) {
                int paymentId = rs.getInt("payment_id");
                String paymentDate = rs.getString("payment_date");
                double amount = rs.getDouble("amount");
                String method = rs.getString("payment_method");
                int billId = rs.getInt("bill_id");
                
                // Format date
                String formattedDate = DateUtils.formatSqliteDate(paymentDate);
                
                model.addRow(new Object[] {
                    paymentId,
                    formattedDate,
                    String.format("$%.2f", amount),
                    method,
                    billId
                });
            }
            
            rs.close();
            stmt.close();
            conn.close();
            
            // Create and show dialog
            JDialog dialog = new JDialog(this, "Payment History", true);
            dialog.setSize(600, 400);
            dialog.setLocationRelativeTo(this);
            
            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            
            // Add title
            JLabel titleLabel = new JLabel("Payment History", SwingConstants.CENTER);
            titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.BOLD, 16));
            panel.add(titleLabel, BorderLayout.NORTH);
            
            // Add table
            JTable table = new JTable(model);
            JScrollPane scrollPane = new JScrollPane(table);
            panel.add(scrollPane, BorderLayout.CENTER);
            
            // Add close button
            JButton closeButton = new JButton("Close");
            closeButton.addActionListener(e -> dialog.dispose());
            
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.add(closeButton);
            panel.add(buttonPanel, BorderLayout.SOUTH);
            
            dialog.getContentPane().add(panel);
            dialog.setVisible(true);
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error loading payment history: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Test method to run this dialog directly.
     */
    public static void main(String[] args) {
        // This is for testing purposes only
        SwingUtilities.invokeLater(() -> {
            try {
                // Create a dummy user
                User testUser = new User(3, "jdoe", "MEMBER", 3, "John", "Doe");
                
                // Create and show the dialog
                UserBillsDisplay dialog = new UserBillsDisplay(null, testUser);
                dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                dialog.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}