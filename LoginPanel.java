import javax.swing.*;
import java.awt.*;

public class LoginPanel extends JPanel {
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
