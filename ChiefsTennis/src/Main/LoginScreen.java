package Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;

import Main.AdminDashboard;
import Main.TreasurerDashboard;
import Main.MemberDashboard;

public class LoginScreen extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private DatabaseConnection dbConnection;

    public LoginScreen(DatabaseConnection dbConnection) {
        this.dbConnection = dbConnection;
        initComponents();
        setupLayout();
        setupListeners();

        setTitle("Tennis Club Management System - Login");
        setSize(400, 250);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
    }

    private void initComponents() {
        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);
        loginButton = new JButton("Login");
    }

    private void setupLayout() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Logo at the top
        JLabel logoLabel = new JLabel("Tennis Club Management System", SwingConstants.CENTER);
        logoLabel.setFont(new Font("Arial", Font.BOLD, 18));
        mainPanel.add(logoLabel, BorderLayout.NORTH);

        // Login form in the center
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Username:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        formPanel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        formPanel.add(passwordField, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Login button at the bottom
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(loginButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        getContentPane().add(mainPanel);
    }

    private void setupListeners() {
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                attemptLogin();
            }
        });

        // Also allow pressing Enter to login
        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    attemptLogin();
                }
            }
        });
    }

    private void attemptLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter both username and password.",
                    "Login Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Connection conn = dbConnection.getConnection();

            // In a real application, passwords should be hashed
            String query = "SELECT u.user_id, u.role, u.member_id, m.first_name, m.last_name " +
                    "FROM Users u " +
                    "LEFT JOIN Members m ON u.member_id = m.member_id " +
                    "WHERE u.username = ? AND u.password = ?";

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, password); // In reality, you'd compare with hashed password

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int userId = rs.getInt("user_id");
                String role = rs.getString("role");
                int memberId = rs.getInt("member_id");
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");

                // Update last login timestamp
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String currentTimestamp = dateFormat.format(new java.util.Date());

                PreparedStatement updateStmt = conn.prepareStatement(
                        "UPDATE Users SET last_login = ? WHERE user_id = ?");
                updateStmt.setString(1, currentTimestamp);
                updateStmt.setInt(2, userId);
                updateStmt.executeUpdate();

                User currentUser = new User(userId, username, role, memberId, firstName, lastName);

                // Open appropriate dashboard based on role
                openDashboard(currentUser);

                // Close login screen
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Invalid username or password.",
                        "Login Failed",
                        JOptionPane.ERROR_MESSAGE);
            }

            rs.close();
            stmt.close();
            conn.close();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Database error: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void openDashboard(User user) {
        switch (user.getRole()) {
            case "ADMIN":

                // alex test
                if (user == null) {
                    System.out.println("CCOD-N");
                } else {
                    System.out.println("CCOD-NOT-N");
                }
                AdminDashboard adminDash = new AdminDashboard(user, dbConnection);
                adminDash.setVisible(true);
                break;
            case "TREASURER":
                TreasurerDashboard treasurerDash = new TreasurerDashboard(user, dbConnection);
                treasurerDash.setVisible(true);
                break;
            case "MEMBER":
                MemberDashboard memberDash = new MemberDashboard(user, dbConnection);
                memberDash.setVisible(true);
                break;
            default:
                JOptionPane.showMessageDialog(this,
                        "Unknown user role: " + user.getRole(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
        }
    }
}

class User {
    private int userId;
    private String username;
    public String role;
    private int memberId;
    private String firstName;
    private String lastName;

    public User(int userId, String username, String role, int memberId, String firstName, String lastName) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.memberId = memberId;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {

        return username;

    }

    public String getRole() {
        // test
        System.out.println("Uid:" + userId + " mId:" + memberId + " r:" + role);

        return role;
    }

    public int getMemberId() {
        return memberId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}