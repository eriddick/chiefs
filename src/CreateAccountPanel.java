import javax.swing.*;
import java.awt.*;

public class CreateAccountPanel extends JPanel {
  public CreateAccountPanel(Interface app) {
    setLayout(new BorderLayout());
    JLabel label = new JLabel("Create Account Page", SwingConstants.CENTER);
    JButton button = new JButton("Go to Login");

    button.addActionListener(e -> app.showPage("Login"));

    add(label, BorderLayout.CENTER);
    add(button, BorderLayout.SOUTH);
  }
}
