import javax.swing.*;
import java.awt.*;
import java.util.List;

public class BillingPanel extends JPanel {
  public BillingPanel(Interface app) {
    setLayout(new BorderLayout(20, 20));
    setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    // **Top Section (Back Button & Title)**
    JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JButton backButton = new JButton("← Back");
    backButton.addActionListener(e -> app.showPage("Home"));

    JLabel titleLabel = new JLabel("Billing");
    titleLabel.setFont(new Font("Arial", Font.BOLD, 20));

    topPanel.add(backButton);
    topPanel.add(Box.createHorizontalStrut(10)); // Spacing
    topPanel.add(titleLabel);

    // **Main Panel to Hold Both Sections**
    JPanel mainPanel = new JPanel(new GridLayout(1, 2, 20, 0)); // Two side-by-side panels

    // **Totals Due Panel**
    JPanel totalsPanel = new JPanel();
    totalsPanel.setLayout(new BoxLayout(totalsPanel, BoxLayout.Y_AXIS));
    totalsPanel.setBorder(BorderFactory.createTitledBorder("Totals Due"));

    totalsPanel.add(new JLabel("Billing Dues:"));
    JLabel totalAmountLabel = new JLabel("$675.00"); // Example Total
    totalAmountLabel.setFont(new Font("Arial", Font.BOLD, 16));
    totalsPanel.add(totalAmountLabel);

    totalsPanel.add(Box.createVerticalStrut(10)); // Spacing

    // Purchases List
    java.util.List<String> purchases = List.of("Membership Fee: $600/yr", "Racquet Fee: $75");
    for (String item : purchases) {
      totalsPanel.add(new JLabel("• " + item));
    }

    // **Make a Payment Panel**
    JPanel paymentPanel = new JPanel();
    paymentPanel.setLayout(new BoxLayout(paymentPanel, BoxLayout.Y_AXIS));
    paymentPanel.setBorder(BorderFactory.createTitledBorder("Make a Payment"));

    // Amount Field
    paymentPanel.add(new JLabel("Amount:"));
    JTextField amountField = new JTextField(10);
    paymentPanel.add(amountField);

    // Payment Method Dropdown
    paymentPanel.add(Box.createVerticalStrut(10)); // Spacing
    paymentPanel.add(new JLabel("Payment Method (Choose one):"));
    String[] paymentMethods = {"Credit Card", "Debit Card", "Bank Transfer", "PayPal"};
    JComboBox<String> paymentDropdown = new JComboBox<>(paymentMethods);
    paymentPanel.add(paymentDropdown);

    // Email Field
    paymentPanel.add(Box.createVerticalStrut(10)); // Spacing
    paymentPanel.add(new JLabel("Email (for receipt):"));
    JTextField emailField = new JTextField(20);
    paymentPanel.add(emailField);

    // Comment Field
    paymentPanel.add(Box.createVerticalStrut(10)); // Spacing
    paymentPanel.add(new JLabel("Comment:"));
    JTextField commentField = new JTextField(25); // Wider input field
    paymentPanel.add(commentField);

    // Submit Button
    paymentPanel.add(Box.createVerticalStrut(10)); // Spacing
    JButton submitButton = new JButton("Submit");
    paymentPanel.add(submitButton);

    // **Add Both Panels to Main Panel**
    mainPanel.add(totalsPanel);
    mainPanel.add(paymentPanel);

    // **Add Everything to the Panel**
    add(topPanel, BorderLayout.NORTH);
    add(mainPanel, BorderLayout.CENTER);
  }
}
