import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Interface {
  private JFrame frame;
  private JPanel mainPanel;
  private CardLayout cardLayout;

  public Interface() {
    frame = new JFrame("Tennis Club");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(800, 600);

    cardLayout = new CardLayout();
    mainPanel = new JPanel(cardLayout);

    // Adding different pages
    mainPanel.add(new LoginPanel(this), "Login");
    mainPanel.add(new HomePagePanel(this), "Home");
    mainPanel.add(new CreateAccountPanel(this), "CreateAccount");
    mainPanel.add(new AccountPanel(this), "Account");
    mainPanel.add(new MemberPanel(this), "MemberList");
    mainPanel.add(new MyReservationPanel(this), "MyReservations");
    mainPanel.add(new CreateGuestPanel(this), "CreateGuest");
    mainPanel.add(new BillingPanel(this), "Billing");

    frame.add(mainPanel);
    frame.setVisible(true);
    showPage("Login"); // Start with the login page
  }

  public void showPage(String pageName) {
    cardLayout.show(mainPanel, pageName);
    frame.revalidate();
    frame.repaint();
  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(Interface::new);
  }
}

// (Login Page)
class LoginPanel extends JPanel {
  public LoginPanel(Interface app) {
    setLayout(new BorderLayout());
    setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

    //_____Top Panel_____
    JPanel topPanel = new JPanel();
    topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

    JLabel siteName = new JLabel("West Hartford Tennis Club", SwingConstants.CENTER);
    JLabel signInText = new JLabel("Sign In", SwingConstants.CENTER);
    JLabel enterText = new JLabel("Enter Email and Password", SwingConstants.CENTER);

    //Fonts and Alignment
    siteName.setFont(new Font("Arial", Font.BOLD, 24));
    signInText.setFont(new Font("Arial", Font.BOLD, 18));
    enterText.setFont(new Font("Arial", Font.PLAIN, 14));
    siteName.setAlignmentX(Component.CENTER_ALIGNMENT);
    signInText.setAlignmentX(Component.CENTER_ALIGNMENT);
    enterText.setAlignmentX(Component.CENTER_ALIGNMENT);

    //Spacing
    topPanel.add(Box.createVerticalStrut(50));
    topPanel.add(siteName);
    topPanel.add(Box.createVerticalStrut(50));
    topPanel.add(signInText);
    topPanel.add(Box.createVerticalStrut(10));
    topPanel.add(enterText);
    topPanel.add(Box.createVerticalStrut(20));


    //_____Center Panel_____
    JPanel centerPanel = new JPanel();
    centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
    JTextField emailField = new JTextField();
    JPasswordField passwordField = new JPasswordField();
    JButton signInButton = new JButton("Sign In");
    JLabel forgotPasswordLabel = new JLabel("Forgot Password?");
    JButton resetPasswordButton = new JButton("Reset Password");

    //Sizing and Alignment
    emailField.setMaximumSize(new Dimension(250, 30));
    passwordField.setMaximumSize(new Dimension(250, 30));
    signInButton.setMaximumSize(new Dimension(200, 20));
    resetPasswordButton.setMaximumSize(new Dimension(200, 20));
    emailField.setAlignmentX(Component.CENTER_ALIGNMENT);
    passwordField.setAlignmentX(Component.CENTER_ALIGNMENT);
    signInButton.setAlignmentX(Component.CENTER_ALIGNMENT);
    forgotPasswordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    resetPasswordButton.setAlignmentX(Component.CENTER_ALIGNMENT);

    //Spacing
    centerPanel.add(emailField);
    centerPanel.add(Box.createVerticalStrut(10));
    centerPanel.add(passwordField);
    centerPanel.add(Box.createVerticalStrut(20));
    centerPanel.add(signInButton);
    centerPanel.add(Box.createVerticalStrut(40));
    centerPanel.add(forgotPasswordLabel);
    centerPanel.add(Box.createVerticalStrut(10));
    centerPanel.add(resetPasswordButton);


    //____Bottom Panel____
    JPanel bottomPanel = new JPanel(new GridLayout(2, 1));
    JLabel noAccountText = new JLabel("Don't have an account? Contact admin", SwingConstants.CENTER);
    JButton contactAdminButton = new JButton("Contact Admin");

    contactAdminButton.setMaximumSize(new Dimension(200, 20));

    bottomPanel.add(noAccountText);
    bottomPanel.add(contactAdminButton);

    add(topPanel, BorderLayout.NORTH);
    add(centerPanel, BorderLayout.CENTER);
    add(bottomPanel, BorderLayout.SOUTH);

    signInButton.addActionListener(e -> app.showPage("Home"));

  }
}

// (Home Page)
class HomePagePanel extends JPanel {
  public HomePagePanel(Interface app) {
    setLayout(new BorderLayout());
    setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

    //____Top Panel____
    JPanel topPanelH = new JPanel();
    topPanelH.setLayout(new BoxLayout(topPanelH, BoxLayout.Y_AXIS));

    JLabel welcomeText = new JLabel("Welcome to the West Hartford Tennis Club", SwingConstants.CENTER);
    JLabel homeText = new JLabel("Home", SwingConstants.CENTER);

    //Fonts and Alignment
    welcomeText.setFont(new Font("Arial", Font.PLAIN, 14));
    homeText.setFont(new Font("Arial", Font.BOLD, 24));
    welcomeText.setAlignmentX(Component.CENTER_ALIGNMENT);
    homeText.setAlignmentX(Component.CENTER_ALIGNMENT);

    //Spacing
    topPanelH.add(Box.createVerticalStrut(30));
    topPanelH.add(welcomeText);
    topPanelH.add(Box.createVerticalStrut(40));
    topPanelH.add(homeText);
    topPanelH.add(Box.createVerticalStrut(60));


    //____Center Panel____
    JPanel centerPanelH = new JPanel();
    centerPanelH.setLayout(new BoxLayout(centerPanelH, BoxLayout.Y_AXIS));

    JButton accountButton = new JButton("Account");
    JButton memberButton = new JButton("Member List");
    JButton reservationButton = new JButton("My Reservations");
    JButton billingButton = new JButton("Billing");

    //Sizing and Alignment
    accountButton.setMaximumSize(new Dimension(300, 30));
    memberButton.setMaximumSize(new Dimension(300, 30));
    reservationButton.setMaximumSize(new Dimension(300, 30));
    billingButton.setMaximumSize(new Dimension(300, 30));
    accountButton.setAlignmentX(Component.CENTER_ALIGNMENT);
    memberButton.setAlignmentX(Component.CENTER_ALIGNMENT);
    reservationButton.setAlignmentX(Component.CENTER_ALIGNMENT);
    billingButton.setAlignmentX(Component.CENTER_ALIGNMENT);

    //Spacing
    centerPanelH.add(accountButton);
    centerPanelH.add(Box.createVerticalStrut(5));
    centerPanelH.add(memberButton);
    centerPanelH.add(Box.createVerticalStrut(5));
    centerPanelH.add(reservationButton);
    centerPanelH.add(Box.createVerticalStrut(5));
    centerPanelH.add(billingButton);
    centerPanelH.add(Box.createVerticalStrut(50));

    //____Bottom Panel____
    JPanel bottomPanelH = new JPanel();
    bottomPanelH.setLayout(new BoxLayout(bottomPanelH, BoxLayout.Y_AXIS));

    JLabel supportText = new JLabel("Contact Customer Support: 873-202-3957", SwingConstants.CENTER);
    JButton logoutButton = new JButton("Log Out");

    //Spacing, Sizing, and Alignment
    supportText.setFont(new Font("Arial", Font.PLAIN, 14));
    logoutButton.setMaximumSize(new Dimension(200, 20));
    supportText.setAlignmentX(Component.CENTER_ALIGNMENT);
    logoutButton.setAlignmentX(Component.CENTER_ALIGNMENT);
    bottomPanelH.add(supportText);
    bottomPanelH.add(logoutButton);

    add(topPanelH, BorderLayout.NORTH);
    add(centerPanelH, BorderLayout.CENTER);
    add(bottomPanelH, BorderLayout.SOUTH);

    //Actions
    accountButton.addActionListener(e -> app.showPage("Account"));
    memberButton.addActionListener(e -> app.showPage("MemberList"));
    reservationButton.addActionListener(e -> app.showPage("MyReservations"));
    billingButton.addActionListener(e -> app.showPage("Billing"));
    logoutButton.addActionListener(e -> app.showPage("Login"));
  }
}

// (Create Account Page)
class CreateAccountPanel extends JPanel {
  public CreateAccountPanel(Interface app) {
    setLayout(new BorderLayout());
    JLabel label = new JLabel("Create Account Page", SwingConstants.CENTER);
    JButton button = new JButton("Go to Login");

    button.addActionListener(e -> app.showPage("Login"));

    add(label, BorderLayout.CENTER);
    add(button, BorderLayout.SOUTH);
  }
}

// (Account Page)
class AccountPanel extends JPanel {
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

// (Member List)
class MemberPanel extends JPanel {
  public MemberPanel(Interface app) {}
}

// (My Reservations)
class MyReservationPanel extends JPanel {
  public MyReservationPanel(Interface app) {}
}

// (Create Guest)
class CreateGuestPanel extends JPanel {
  public CreateGuestPanel(Interface app) {}
}

// (Billing)
class BillingPanel extends JPanel {
  public BillingPanel(Interface app) {}
}

