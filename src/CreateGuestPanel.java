import javax.swing.*;
import java.awt.*;

public class CreateGuestPanel extends JPanel {
  public CreateGuestPanel(Interface app) {
    setLayout(new BorderLayout(20, 20));
    setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    // **Top Section (Back Button & Title)**
    JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JButton backButton = new JButton("← Back");
    backButton.addActionListener(e -> app.showPage("Home"));

    JLabel titleLabel = new JLabel("Create Guest");
    titleLabel.setFont(new Font("Arial", Font.BOLD, 20));

    topPanel.add(backButton);
    topPanel.add(Box.createHorizontalStrut(10)); // Spacing
    topPanel.add(titleLabel);

    // **Centered Form Panel**
    JPanel centerPanel = new JPanel();
    centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
    centerPanel.setBorder(BorderFactory.createTitledBorder("Guest Information"));

    // Name Field
    centerPanel.add(new JLabel("Name:"));
    JTextField nameField = new JTextField(20);
    centerPanel.add(nameField);

    // Email Field
    centerPanel.add(Box.createVerticalStrut(10)); // Spacing
    centerPanel.add(new JLabel("Email:"));
    JTextField emailField = new JTextField(20);
    centerPanel.add(emailField);

    // Phone Number Field
    centerPanel.add(Box.createVerticalStrut(10)); // Spacing
    centerPanel.add(new JLabel("Phone Number:"));
    JTextField phoneField = new JTextField(20);
    centerPanel.add(phoneField);

    // Number of Guest Passes Field
    centerPanel.add(Box.createVerticalStrut(10)); // Spacing
    centerPanel.add(new JLabel("Number of Guest Passes:"));
    JTextField guestPassField = new JTextField(5);
    centerPanel.add(guestPassField);

    // Info Label about Guest Pass Limit
    centerPanel.add(Box.createVerticalStrut(10)); // Spacing
    JLabel infoLabel = new JLabel("<html><center>Note: Members only receive 3 guest passes per month</center></html>");
    infoLabel.setForeground(Color.RED);
    centerPanel.add(infoLabel);

    // Use Guest Pass Button
    centerPanel.add(Box.createVerticalStrut(10)); // Spacing
    JButton usePassButton = new JButton("Use Guest Pass");
    centerPanel.add(usePassButton);

    // Centering the panel
    JPanel wrapperPanel = new JPanel(new GridBagLayout());
    wrapperPanel.add(centerPanel);

    // **Add Everything to the Panel**
    add(topPanel, BorderLayout.NORTH);
    add(wrapperPanel, BorderLayout.CENTER);
  }
}
