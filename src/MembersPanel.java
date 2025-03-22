import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MembersPanel extends JPanel {
  private DefaultTableModel tableModel;
  private JTable table;
  public MembersPanel(Interface app) {
    setLayout(new BorderLayout(20, 20));
    setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    // **Top Section (Back Button & Title)**
    JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JButton backButton = new JButton("← Back");
    backButton.addActionListener(e -> app.showPage("Home"));

    JLabel titleLabel = new JLabel("Member List");
    titleLabel.setFont(new Font("Arial", Font.BOLD, 20));

    topPanel.add(backButton);
    topPanel.add(Box.createHorizontalStrut(10)); // Spacing
    topPanel.add(titleLabel);

    // **Search Bar**
    JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JTextField searchField = new JTextField(20);
    JButton searchButton = new JButton("Search");

    searchPanel.add(new JLabel("Search:"));
    searchPanel.add(searchField);
    searchPanel.add(searchButton);

    // **Table for Member List**
    String[] columnNames = {"ID Number", "Member", "Email", "Availability", "Profile"};
    tableModel = new DefaultTableModel(columnNames, 0) {
      @Override
      public boolean isCellEditable(int row, int column) {
        return false; // Make table non-editable
      }
    };

    table = new JTable(tableModel);
    JScrollPane scrollPane = new JScrollPane(table);
    table.setFillsViewportHeight(true);

    add(topPanel, BorderLayout.NORTH);
    add(searchPanel, BorderLayout.CENTER);
    add(scrollPane, BorderLayout.SOUTH);
  }

  // **Method to Open Add Member Form**
  public void loadMembers(List<Member> members) {
    tableModel.setRowCount(0); // Clear existing data

    for (Member member : members) {
      tableModel.addRow(new Object[]{
        member.getId(),
        member.getName(),
        member.getEmail(),
        member.getAvailability(),
        "Profile Pic" // Placeholder for profile image
      });
    }
  }
}
