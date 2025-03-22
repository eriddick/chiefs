import javax.swing.*;
import java.awt.*;

public class ReservationsPanel extends JPanel {
  public ReservationsPanel(Interface app) {
    setLayout(new BorderLayout(20, 20));
    setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    // **Top Section (Back Button & Title)**
    JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JButton backButton = new JButton("← Back");
    backButton.addActionListener(e -> app.showPage("Home"));

    JLabel titleLabel = new JLabel("My Reservations");
    titleLabel.setFont(new Font("Arial", Font.BOLD, 20));

    topPanel.add(backButton);
    topPanel.add(Box.createHorizontalStrut(10)); // Spacing
    topPanel.add(titleLabel);

    // **Main Content Panel (Two Side-by-Side Panels)**
    JPanel mainContent = new JPanel(new GridLayout(1, 2, 20, 20));

    // **Current Reservation Panel (Now Supports 3 Reservations)**
    JPanel currentReservationPanel = new JPanel();
    currentReservationPanel.setLayout(new BoxLayout(currentReservationPanel, BoxLayout.Y_AXIS));
    currentReservationPanel.setBorder(BorderFactory.createTitledBorder("Current Reservations"));

    // Add three reservation boxes
    for (int i = 1; i <= 3; i++) {
      currentReservationPanel.add(createInfoBox("Reservation " + i, "Date and Time: N/A", "Members/Guests: N/A"));
      currentReservationPanel.add(Box.createVerticalStrut(10)); // Spacing between reservations
    }

    // **New Reservation Panel**
    JPanel newReservationPanel = new JPanel();
    newReservationPanel.setLayout(new BoxLayout(newReservationPanel, BoxLayout.Y_AXIS));
    newReservationPanel.setBorder(BorderFactory.createTitledBorder("Create New Reservation"));

    // Drop-down for Date Selection
    newReservationPanel.add(new JLabel("Select a Date:"));
    JComboBox<String> dateDropdown = new JComboBox<>(new String[]{"March 23", "March 24", "March 25"});
    newReservationPanel.add(dateDropdown);

    // Drop-down for Time Selection
    newReservationPanel.add(new JLabel("Select a Time:"));
    JComboBox<String> timeDropdown = new JComboBox<>(new String[]{"10:00 AM", "11:00 AM", "12:00 PM"});
    newReservationPanel.add(timeDropdown);

    // Drop-down for Court Type
    newReservationPanel.add(new JLabel("Choose a court type (Singles, Doubles, Practice):"));
    JComboBox<String> courtDropdown = new JComboBox<>(new String[]{"Singles", "Doubles", "Practice"});
    newReservationPanel.add(courtDropdown);

    // **Members Section**
    newReservationPanel.add(new JLabel("Members:"));
    JPanel memberButtonsPanel = new JPanel(new GridLayout(2, 2, 10, 10));
    for (int i = 0; i < 4; i++) {
      memberButtonsPanel.add(new JButton("Add Member"));
    }
    newReservationPanel.add(memberButtonsPanel);

    // Buttons for adding guests and creating reservation
    newReservationPanel.add(Box.createVerticalStrut(10)); // Spacing
    JButton addGuestButton = new JButton("Create and Add Guest");
    addGuestButton.addActionListener(e -> app.showPage("CreateGuest"));
    JButton createReservationButton = new JButton("Create Reservation");

    newReservationPanel.add(addGuestButton);
    newReservationPanel.add(createReservationButton);

    // **Adding Panels to Main Content**
    mainContent.add(currentReservationPanel);
    mainContent.add(newReservationPanel);

    // **Add Everything to MyReservationsPanel**
    add(topPanel, BorderLayout.NORTH);
    add(mainContent, BorderLayout.CENTER);
  }

  // Method to create a reservation info box
  private JPanel createInfoBox(String title, String dateTime, String members) {
    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout());
    panel.setBorder(BorderFactory.createCompoundBorder(
      BorderFactory.createTitledBorder(title),
      BorderFactory.createEmptyBorder(5, 5, 5, 5)
    ));

    // Reservation details
    JPanel infoPanel = new JPanel();
    infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
    infoPanel.add(new JLabel(dateTime));
    infoPanel.add(new JLabel(members));

    // Edit button
    JButton editButton = new JButton("Edit");
    editButton.setPreferredSize(new Dimension(70, 25));

    panel.add(infoPanel, BorderLayout.CENTER);
    panel.add(editButton, BorderLayout.EAST);

    return panel;
  }
}
