import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AccountPanel extends JPanel {
  public AccountPanel(Interface app) {
    setLayout(new BorderLayout(20, 20));
    setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    // **Top Section (Back Button & Title)**
    JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JButton backButton = new JButton("← Back");
    backButton.addActionListener(e -> app.showPage("Home"));

    JLabel titleLabel = new JLabel("Account", SwingConstants.CENTER);
    titleLabel.setFont(new Font("Arial", Font.BOLD, 20));

    topPanel.add(backButton);
    topPanel.add(Box.createHorizontalStrut(10));
    topPanel.add(titleLabel);

    // **Main Section (Two Side-by-Side Panels)**
    JPanel mainPanel = new JPanel(new GridLayout(1, 2, 20, 0));

    // **Left Panel - Account Information**
    JPanel accountInfoPanel = createInfoPanel("Account Information",
      new String[]{"Email", "Password", "Contact Information"},
      new String[]{"user@example.com", "••••••", "123-456-7890"});

    // **Right Panel - Billing Information**
    JPanel billingInfoPanel = createInfoPanel("Billing Information",
      new String[]{"Payment Method", "Alternate Payment", "Billing Address"},
      new String[]{"Visa - 1234", "PayPal - user@example.com", "123 Main St"});

    mainPanel.add(accountInfoPanel);
    mainPanel.add(billingInfoPanel);

    // **Add everything to the main layout**
    add(topPanel, BorderLayout.NORTH);
    add(mainPanel, BorderLayout.CENTER);
  }

  // Helper method to create a panel with labeled text fields and Edit buttons
  private JPanel createInfoPanel(String title, String[] labels, String[] data) {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setBorder(BorderFactory.createTitledBorder(title));

    for (int i = 0; i < labels.length; i++) {
      panel.add(createInfoBox(labels[i], data[i]));
      panel.add(Box.createVerticalStrut(10));
    }

    return panel;
  }

  // Helper method to create a labeled box with a text field and an "Edit" button
  private JPanel createInfoBox(String labelText, String textFieldValue) {
    JPanel box = new JPanel(new BorderLayout());
    box.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
    box.setPreferredSize(new Dimension(250, 70));

    JPanel labelFieldPanel = new JPanel(new GridLayout(2, 1));
    JLabel label = new JLabel(labelText);
    JTextField textField = new JTextField(textFieldValue);
    textField.setEditable(false); // Default to non-editable
    textField.setHorizontalAlignment(JTextField.CENTER);
    textField.setPreferredSize(new Dimension(200, 25));

    labelFieldPanel.add(label);
    labelFieldPanel.add(textField);

    JButton editButton = new JButton("Edit");
    editButton.setPreferredSize(new Dimension(60, 20));
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    buttonPanel.add(editButton);

    editButton.addActionListener(new ActionListener() {
      private boolean isEditing = false;

      @Override
      public void actionPerformed(ActionEvent e) {
        if (!isEditing) {
          textField.setEditable(true);
          editButton.setText("Save");
        } else {
          textField.setEditable(false);
          editButton.setText("Edit");
          // Here you can add code to save the updated value
        }
        isEditing = !isEditing;
      }
    });

    box.add(labelFieldPanel, BorderLayout.CENTER);
    box.add(buttonPanel, BorderLayout.EAST);

    return box;
  }
}
