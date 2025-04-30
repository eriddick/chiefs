package Main;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;

/**
 * A form for processing bill payments in the Tennis Club Management System.
 */
public class BillPaymentForm extends JDialog {
    private int billId;
    private double amount;
    private User currentUser;
    private DatabaseConnection dbConnection;
    private boolean paymentSuccessful = false;
    
    // Payment form components
    private JComboBox<String> paymentMethodComboBox;
    private JTextField cardNumberField;
    private JTextField cardNameField;
    private JTextField expiryDateField;
    private JTextField cvvField;
    private JLabel amountLabel;
    private JButton submitButton;
    private JButton cancelButton;
    
    /**
     * Constructs a new payment form dialog.
     * 
     * @param parent The parent frame
     * @param billId The ID of the bill to pay
     * @param amount The amount to be paid
     * @param currentUser The current user making the payment
     */
    public BillPaymentForm(JFrame parent, int billId, double amount, User currentUser) {
        super(parent, "Payment Form", true);
        this.billId = billId;
        this.amount = amount;
        this.currentUser = currentUser;
        this.dbConnection = new DatabaseConnection();
        
        initComponents();
        setupLayout();
        setupListeners();
        
        setSize(500, 450);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }
    
    /**
     * Initializes form components.
     */
    private void initComponents() {
        String[] paymentMethods = {"Credit Card", "Debit Card"};
        paymentMethodComboBox = new JComboBox<>(paymentMethods);
        
        cardNumberField = new JTextField(16);
        cardNameField = new JTextField(20);
        expiryDateField = new JTextField(5);
        cvvField = new JTextField(3);
        
        amountLabel = new JLabel("$" + String.format("%.2f", amount));
        amountLabel.setFont(new Font(amountLabel.getFont().getName(), Font.BOLD, 14));
        
        submitButton = new JButton("Submit Payment");
        cancelButton = new JButton("Cancel");
    }
    
    /**
     * Sets up the form layout.
     */
    private void setupLayout() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Payment details panel
        JPanel paymentDetailsPanel = new JPanel(new GridBagLayout());
        paymentDetailsPanel.setBorder(BorderFactory.createTitledBorder("Payment Details"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Payment method
        gbc.gridx = 0;
        gbc.gridy = 0;
        paymentDetailsPanel.add(new JLabel("Payment Method:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        paymentDetailsPanel.add(paymentMethodComboBox, gbc);
        
        // Amount
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        paymentDetailsPanel.add(new JLabel("Amount:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        paymentDetailsPanel.add(amountLabel, gbc);
        
        // Card information panel
        JPanel cardPanel = new JPanel(new GridBagLayout());
        cardPanel.setBorder(BorderFactory.createTitledBorder("Card Information"));
        
        GridBagConstraints cardGbc = new GridBagConstraints();
        cardGbc.fill = GridBagConstraints.HORIZONTAL;
        cardGbc.insets = new Insets(5, 5, 5, 5);
        cardGbc.anchor = GridBagConstraints.WEST;
        
        // Card number
        cardGbc.gridx = 0;
        cardGbc.gridy = 0;
        cardPanel.add(new JLabel("Card Number:"), cardGbc);
        
        cardGbc.gridx = 1;
        cardGbc.gridy = 0;
        cardGbc.gridwidth = 3;
        cardPanel.add(cardNumberField, cardGbc);
        
        // Card name
        cardGbc.gridx = 0;
        cardGbc.gridy = 1;
        cardGbc.gridwidth = 1;
        cardPanel.add(new JLabel("Name on Card:"), cardGbc);
        
        cardGbc.gridx = 1;
        cardGbc.gridy = 1;
        cardGbc.gridwidth = 3;
        cardPanel.add(cardNameField, cardGbc);
        
        // Expiry date
        cardGbc.gridx = 0;
        cardGbc.gridy = 2;
        cardGbc.gridwidth = 1;
        cardPanel.add(new JLabel("Expiry Date (MM/YY):"), cardGbc);
        
        cardGbc.gridx = 1;
        cardGbc.gridy = 2;
        cardGbc.gridwidth = 1;
        cardPanel.add(expiryDateField, cardGbc);
        
        // CVV
        cardGbc.gridx = 2;
        cardGbc.gridy = 2;
        cardPanel.add(new JLabel("CVV:"), cardGbc);
        
        cardGbc.gridx = 3;
        cardGbc.gridy = 2;
        cardPanel.add(cvvField, cardGbc);
        
        // Add card panel to main panel
        mainPanel.add(paymentDetailsPanel, BorderLayout.NORTH);
        mainPanel.add(cardPanel, BorderLayout.CENTER);
        
        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(submitButton);
        buttonPanel.add(cancelButton);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Add disclaimer
        JTextArea disclaimerArea = new JTextArea(
            "Note: This is a simulated payment form for demonstration purposes only. " +
            "No actual payment processing will occur, and no real card information should be entered.");
        disclaimerArea.setEditable(false);
        disclaimerArea.setLineWrap(true);
        disclaimerArea.setWrapStyleWord(true);
        disclaimerArea.setBackground(mainPanel.getBackground());
        disclaimerArea.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 10));
        
        JPanel disclaimerPanel = new JPanel(new BorderLayout());
        disclaimerPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        disclaimerPanel.add(disclaimerArea, BorderLayout.CENTER);
        
        mainPanel.add(disclaimerPanel, BorderLayout.SOUTH);
        buttonPanel.add(submitButton);
        buttonPanel.add(cancelButton);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        getContentPane().add(mainPanel);
    }
    
    /**
     * Sets up event listeners for form components.
     */
    private void setupListeners() {
        // Submit button action
        submitButton.addActionListener(e -> {
            if (validateForm()) {
                processPayment();
            }
        });
        
        // Cancel button action
        cancelButton.addActionListener(e -> dispose());
        
        // Format the expiry date as user types
        expiryDateField.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                String text = expiryDateField.getText();
                if (text.length() == 2 && e.getKeyChar() != KeyEvent.VK_BACK_SPACE) {
                    if (!text.contains("/")) {
                        expiryDateField.setText(text + "/");
                    }
                }
            }
        });
        
        // Format the card number with spaces every 4 digits
        cardNumberField.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                String text = cardNumberField.getText();
                if (text.length() > 0 && text.length() % 5 == 4 && e.getKeyChar() != KeyEvent.VK_BACK_SPACE) {
                    if (text.charAt(text.length() - 1) != ' ') {
                        cardNumberField.setText(text + " ");
                    }
                }
            }
        });
    }
    
    /**
     * Validates the payment form input.
     * 
     * @return true if the form is valid, false otherwise
     */
    private boolean validateForm() {
        // Validate card number
        String cardNumber = cardNumberField.getText().trim().replace(" ", "");
        if (cardNumber.isEmpty() || !cardNumber.matches("\\d{16}")) {
            JOptionPane.showMessageDialog(this,
                "Please enter a valid 16-digit card number.",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Validate name on card
        if (cardNameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter the name on the card.",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Validate expiry date
        String expiryDate = expiryDateField.getText().trim();
        if (!expiryDate.matches("\\d{2}/\\d{2}")) {
            JOptionPane.showMessageDialog(this,
                "Please enter a valid expiry date in MM/YY format.",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Validate CVV
        String cvv = cvvField.getText().trim();
        if (!cvv.matches("\\d{3}")) {
            JOptionPane.showMessageDialog(this,
                "Please enter a valid 3-digit CVV.",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return true;
    }
    
    /**
     * Processes the payment and updates the database.
     */
    private boolean processPayment() {
        // In a real application, this would connect to a payment processor
        // For this simulation, we'll just mark the bill as paid in the database
        
        try {
            Connection conn = dbConnection.getConnection();
            conn.setAutoCommit(false);
            
            try {
                // Mark bill as paid
                String updateBillQuery = "UPDATE Bills SET is_paid = 1 WHERE bill_id = ?";
                PreparedStatement updateBillStmt = conn.prepareStatement(updateBillQuery);
                updateBillStmt.setInt(1, billId);
                updateBillStmt.executeUpdate();
                updateBillStmt.close();
                
                // Get member ID and bill items
                String billQuery = "SELECT member_id FROM Bills WHERE bill_id = ?";
                PreparedStatement billStmt = conn.prepareStatement(billQuery);
                billStmt.setInt(1, billId);
                
                ResultSet billRs = billStmt.executeQuery();
                int memberId = 0;
                
                if (billRs.next()) {
                    memberId = billRs.getInt("member_id");
                }
                
                billRs.close();
                billStmt.close();
                
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
                            updateQuery = "UPDATE MembershipFees SET is_paid = 1, paid_date = date('now') WHERE fee_id = ?";
                            break;
                        case "LATE_FEE":
                            updateQuery = "UPDATE LateFees SET is_paid = 1 WHERE late_fee_id = ?";
                            break;
                        case "GUEST_FEE":
                            updateQuery = "UPDATE GuestFees SET is_paid = 1 WHERE guest_fee_id = ?";
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
                if (memberId > 0) {
                    String updateMemberQuery = "UPDATE Members SET status = 'ACTIVE' WHERE member_id = ? AND status = 'LATE_PAYMENT'";
                    PreparedStatement updateMemberStmt = conn.prepareStatement(updateMemberQuery);
                    updateMemberStmt.setInt(1, memberId);
                    updateMemberStmt.executeUpdate();
                    updateMemberStmt.close();
                }
                
                // Insert payment record
                String paymentInsertQuery = "INSERT INTO Payments (bill_id, member_id, payment_date, amount, payment_method) " +
                                          "VALUES (?, ?, date('now'), ?, ?)";
                PreparedStatement paymentInsertStmt = conn.prepareStatement(paymentInsertQuery);
                paymentInsertStmt.setInt(1, billId);
                paymentInsertStmt.setInt(2, currentUser.getMemberId());
                paymentInsertStmt.setDouble(3, amount);
                paymentInsertStmt.setString(4, (String) paymentMethodComboBox.getSelectedItem());
                paymentInsertStmt.executeUpdate();
                paymentInsertStmt.close();
                
                conn.commit();
                
                // Show success message and send confirmation email
                JOptionPane.showMessageDialog(this,
                    "Payment processed successfully! A receipt has been sent to your email.",
                    "Payment Complete",
                    JOptionPane.INFORMATION_MESSAGE);
                
                sendConfirmationEmail();
                
                paymentSuccessful = true;
                dispose();
                return true;
                
            } catch (SQLException ex) {
                conn.rollback();
                JOptionPane.showMessageDialog(this,
                    "Error processing payment: " + ex.getMessage(),
                    "Payment Error",
                    JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
                return false;
            } finally {
                conn.setAutoCommit(true);
                conn.close();
            }
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Database connection error: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    /**
     * Simulates sending a confirmation email for the payment.
     */
    private void sendConfirmationEmail() {
        try {
            Connection conn = dbConnection.getConnection();
            
            // Get member email and bill details
            String query = "SELECT m.first_name, m.last_name, m.email, b.bill_date, b.total_amount " +
                         "FROM Bills b " +
                         "JOIN Members m ON b.member_id = m.member_id " +
                         "WHERE b.bill_id = ?";
            
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, billId);
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                String email = rs.getString("email");
                String billDate = rs.getString("bill_date");
                double totalAmount = rs.getDouble("total_amount");
                
                // Format date for display
                String formattedBillDate = DateUtils.formatSqliteDate(billDate);
                
                // In a real application, this would send an actual email
                // For this example, we'll just simulate it
                
                SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                String currentDate = dateFormat.format(new Date());
                
                StringBuilder emailContent = new StringBuilder();
                emailContent.append("To: ").append(email).append("\n");
                emailContent.append("From: billing@tennisclub.com\n");
                emailContent.append("Subject: Payment Confirmation - Tennis Club\n\n");
                emailContent.append("Dear ").append(firstName).append(" ").append(lastName).append(",\n\n");
                emailContent.append("Thank you for your payment. This email confirms that we have received your payment of $")
                           .append(String.format("%.2f", totalAmount)).append(" for bill #").append(billId)
                           .append(" dated ").append(formattedBillDate).append(".\n\n");
                emailContent.append("Payment Details:\n");
                emailContent.append("Date: ").append(currentDate).append("\n");
                emailContent.append("Amount: $").append(String.format("%.2f", totalAmount)).append("\n");
                emailContent.append("Method: ").append(paymentMethodComboBox.getSelectedItem()).append("\n");
                emailContent.append("Card Number: ****").append(cardNumberField.getText().trim().replace(" ", "").substring(12)).append("\n\n");
                emailContent.append("Your account has been updated to reflect this payment. If you have any questions, please contact the club treasurer.\n\n");
                emailContent.append("Thank you for your membership!\n\n");
                emailContent.append("Tennis Club Billing Department");
                
                // Show the email content in a dialog (for demonstration)
                JTextArea textArea = new JTextArea(emailContent.toString());
                textArea.setEditable(false);
                textArea.setLineWrap(true);
                textArea.setWrapStyleWord(true);
                
                JScrollPane scrollPane = new JScrollPane(textArea);
                scrollPane.setPreferredSize(new Dimension(500, 400));
                
                JOptionPane.showMessageDialog(this,
                    scrollPane,
                    "Payment Confirmation Email",
                    JOptionPane.INFORMATION_MESSAGE);
            }
            
            rs.close();
            stmt.close();
            conn.close();
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error sending confirmation email: " + ex.getMessage(),
                "Email Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Returns whether the payment was successfully processed.
     * 
     * @return true if payment was successful, false otherwise
     */
    public boolean isPaymentSuccessful() {
        return paymentSuccessful;
    }
}