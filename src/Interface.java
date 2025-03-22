import javax.swing.*;
import java.awt.*;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;

public class Interface {
  private JFrame frame;
  private JPanel mainPanel;
  private CardLayout cardLayout;
  private MembersPanel membersPanel;

  public Interface() {
    frame = new JFrame("Tennis Club");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(800, 600);

    cardLayout = new CardLayout();
    mainPanel = new JPanel(cardLayout);
    membersPanel = new MembersPanel(this);

    // Adding different pages
    mainPanel.add(new LoginPanel(this), "Login");
    mainPanel.add(new HomePanel(this), "Home");
    mainPanel.add(new CreateAccountPanel(this), "CreateAccount");
    mainPanel.add(new AccountPanel(this), "Account");
    mainPanel.add(new MembersPanel(this), "MemberList");
    mainPanel.add(membersPanel, "MemberList");
    mainPanel.add(new ReservationsPanel(this), "MyReservations");
    mainPanel.add(new CreateGuestPanel(this), "CreateGuest");
    mainPanel.add(new BillingPanel(this), "Billing");

    frame.add(mainPanel);
    frame.setVisible(true);
    loadTestData();
    showPage("Login"); // Start with the login page
  }

  public void showPage(String pageName) {
    cardLayout.show(mainPanel, pageName);
    frame.revalidate();
    frame.repaint();
  }

  private void loadTestData() {
    // Creating test members
    List<Member> testMembers = new ArrayList<>();
    testMembers.add(new Member(1, "John Doe", "john@example.com", "Available"));
    testMembers.add(new Member(2, "Jane Smith", "jane@example.com", "Busy"));
    testMembers.add(new Member(3, "Alice Brown", "alice@example.com", "Available"));

    // Load data into MembersPanel
    membersPanel.loadMembers(testMembers);
  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(Interface::new);
  }
}
