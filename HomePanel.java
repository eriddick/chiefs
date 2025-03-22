import javax.swing.*;
import java.awt.*;

public class HomePanel extends JPanel {
  public HomePanel(Interface app) {
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
