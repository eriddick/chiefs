package Main;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

public class CourtReservationScreen extends JFrame {
    private User currentUser;
    private DatabaseConnection dbConnection;
    
    private JComboBox<String> courtComboBox;
    private JComboBox<String> reservationTypeComboBox;
    private JSpinner durationSpinner;
    private JDateChooser dateChooser;
    private JSpinner timeSpinner;
    private JTable participantsTable;
    private DefaultTableModel participantsTableModel;
    private JButton addParticipantButton;
    private JButton addGuestButton;
    private JButton makeReservationButton;
    
    private ArrayList<ParticipantEntry> participants = new ArrayList<>();
    
    public CourtReservationScreen(User currentUser) {
        this.currentUser = currentUser;
        this.dbConnection = new DatabaseConnection();
        
        initComponents();
        setupLayout();
        setupListeners();
        loadCourts();
        
        setTitle("Court Reservation");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
    
    private void initComponents() {
        // Court selection
        courtComboBox = new JComboBox<>();
        
        // Reservation type (singles/doubles)
        reservationTypeComboBox = new JComboBox<>(new String[]{"Singles", "Doubles"});
        reservationTypeComboBox.addActionListener(e -> updateDurationLimits());
        
        // Duration spinner (in minutes)
        SpinnerNumberModel durationModel = new SpinnerNumberModel(60, 60, 120, 15);
        durationSpinner = new JSpinner(durationModel);
        
        // Date picker for reservation date
        dateChooser = new JDateChooser();
        dateChooser.setDate(new Date()); // Default to today
        dateChooser.setMinSelectableDate(new Date()); // Can't select dates in the past
        
        // Set max date to 1 week from now
        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.DAY_OF_MONTH, 7);
        dateChooser.setMaxSelectableDate(maxDate.getTime());
        
        // Time spinner for reservation start time (7:00 AM - 9:00 PM in 30-min increments)
        SpinnerDateModel timeModel = new SpinnerDateModel();
        timeModel.setCalendarField(Calendar.MINUTE);
        timeSpinner = new JSpinner(timeModel);
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "h:mm a");
        timeSpinner.setEditor(timeEditor);
        
        // Set default time to 7:00 AM
        Calendar defaultTime = Calendar.getInstance();
        defaultTime.set(Calendar.HOUR_OF_DAY, 7);
        defaultTime.set(Calendar.MINUTE, 0);
        timeSpinner.setValue(defaultTime.getTime());
        
        // Participants table
        String[] participantColumns = {"Type", "Name", "Email", "Remove"};
        participantsTableModel = new DefaultTableModel(participantColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3; // Only "Remove" column is editable
            }
        };
        participantsTable = new JTable(participantsTableModel);
        
        // Add buttons
        addParticipantButton = new JButton("Add Member");
        addGuestButton = new JButton("Add Guest");
        makeReservationButton = new JButton("Make Reservation");
        
        // Add current user as first participant
        try {
            Connection conn = dbConnection.getConnection();
            String query = "SELECT first_name, last_name, email FROM Members WHERE member_id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, currentUser.getMemberId());
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                String email = rs.getString("email");
                
                ParticipantEntry entry = new ParticipantEntry(
                    "Member", 
                    currentUser.getMemberId(),
                    0,  // Guest ID is 0 for members
                    firstName + " " + lastName,
                    email
                );
                
                participants.add(entry);
                participantsTableModel.addRow(new Object[]{
                    "Member",
                    firstName + " " + lastName,
                    email,
                    "Remove"
                });
            }
            
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, 
                "Error loading current user information: " + ex.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
        
        // Set up the "Remove" button in the table
        Action removeAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                int modelRow = Integer.parseInt(e.getActionCommand());
                if (modelRow == 0) {
                    JOptionPane.showMessageDialog(CourtReservationScreen.this, 
                        "Cannot remove yourself from the reservation.", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                participants.remove(modelRow);
                participantsTableModel.removeRow(modelRow);
            }
        };
        
        ButtonColumn buttonColumn = new ButtonColumn(participantsTable, removeAction, 3);
    }
    
    private void setupLayout() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Form panel at the top
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Reservation Details"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // First row
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Court:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        formPanel.add(courtComboBox, gbc);
        
        gbc.gridx = 2;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Type:"), gbc);
        
        gbc.gridx = 3;
        gbc.gridy = 0;
        formPanel.add(reservationTypeComboBox, gbc);
        
        // Second row
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Date:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        formPanel.add(dateChooser, gbc);
        
        gbc.gridx = 2;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Start Time:"), gbc);
        
        gbc.gridx = 3;
        gbc.gridy = 1;
        formPanel.add(timeSpinner, gbc);
        
        // Third row
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Duration (min):"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 2;
        formPanel.add(durationSpinner, gbc);
        
        mainPanel.add(formPanel, BorderLayout.NORTH);
        
        // Participants panel in the center
        JPanel participantsPanel = new JPanel(new BorderLayout(5, 5));
        participantsPanel.setBorder(BorderFactory.createTitledBorder("Participants"));
        
        JScrollPane scrollPane = new JScrollPane(participantsTable);
        participantsPanel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel participantButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        participantButtonsPanel.add(addParticipantButton);
        participantButtonsPanel.add(addGuestButton);
        participantsPanel.add(participantButtonsPanel, BorderLayout.SOUTH);
        
        mainPanel.add(participantsPanel, BorderLayout.CENTER);
        
        // Button panel at the bottom
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(makeReservationButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        getContentPane().add(mainPanel);
    }
    
    private void setupListeners() {
        addParticipantButton.addActionListener(e -> showAddParticipantDialog());
        
        addGuestButton.addActionListener(e -> showAddGuestDialog());
        
        makeReservationButton.addActionListener(e -> makeReservation());
        
        // Update duration limits when reservation type changes
        reservationTypeComboBox.addActionListener(e -> updateDurationLimits());
    }
    
    private void updateDurationLimits() {
        String type = (String) reservationTypeComboBox.getSelectedItem();
        SpinnerNumberModel model = (SpinnerNumberModel) durationSpinner.getModel();
        
        if ("Singles".equals(type)) {
            model.setMinimum(60);
            model.setMaximum(90);
            if ((int)durationSpinner.getValue() > 90) {
                durationSpinner.setValue(90);
            }
        } else { // Doubles
            model.setMinimum(90);
            model.setMaximum(120);
            if ((int)durationSpinner.getValue() < 90) {
                durationSpinner.setValue(90);
            }
        }
    }
    
    private void loadCourts() {
        try {
            Connection conn = dbConnection.getConnection();
            String query = "SELECT court_id, court_number, court_type FROM Courts ORDER BY court_number";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                int courtId = rs.getInt("court_id");
                int courtNumber = rs.getInt("court_number");
                String courtType = rs.getString("court_type");
                
                courtComboBox.addItem("Court " + courtNumber + " (" + courtType + ")");
            }
            
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, 
                "Error loading courts: " + ex.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showAddParticipantDialog() {
        JDialog dialog = new JDialog(this, "Add Member Participant", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Search field
        JTextField searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");
        
        JPanel searchPanel = new JPanel(new BorderLayout(5, 5));
        searchPanel.add(new JLabel("Search Member:"), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);
        
        panel.add(searchPanel, BorderLayout.NORTH);
        
        // Results table
        String[] columns = {"Member ID", "Name", "Email"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable resultsTable = new JTable(model);
        resultsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(resultsTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Add button
        JButton addButton = new JButton("Add Selected Member");
        addButton.setEnabled(false);
        
        resultsTable.getSelectionModel().addListSelectionListener(e -> {
            addButton.setEnabled(resultsTable.getSelectedRow() != -1);
        });
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(addButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.getContentPane().add(panel);
        
        // Search button action
        searchButton.addActionListener(e -> {
            String searchTerm = searchField.getText().trim();
            if (searchTerm.isEmpty()) {
                return;
            }
            
            model.setRowCount(0);
            
            try {
                Connection conn = dbConnection.getConnection();
                String query = "SELECT member_id, first_name, last_name, email FROM Members " +
                              "WHERE (first_name LIKE ? OR last_name LIKE ? OR email LIKE ?) " +
                              "AND member_id != ?";
                
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, "%" + searchTerm + "%");
                stmt.setString(2, "%" + searchTerm + "%");
                stmt.setString(3, "%" + searchTerm + "%");
                stmt.setInt(4, currentUser.getMemberId());  // Exclude current user
                
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    int memberId = rs.getInt("member_id");
                    String firstName = rs.getString("first_name");
                    String lastName = rs.getString("last_name");
                    String email = rs.getString("email");
                    
                    // Check if this member is already in the participants list
                    boolean alreadyAdded = false;
                    for (ParticipantEntry entry : participants) {
                        if ("Member".equals(entry.getType()) && entry.getMemberId() == memberId) {
                            alreadyAdded = true;
                            break;
                        }
                    }
                    
                    if (!alreadyAdded) {
                        model.addRow(new Object[]{
                            memberId,
                            firstName + " " + lastName,
                            email
                        });
                    }
                }
                
                rs.close();
                stmt.close();
                conn.close();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, 
                    "Error searching members: " + ex.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        // Add button action
        addButton.addActionListener(e -> {
            int selectedRow = resultsTable.getSelectedRow();
            if (selectedRow != -1) {
                int memberId = (int) model.getValueAt(selectedRow, 0);
                String name = (String) model.getValueAt(selectedRow, 1);
                String email = (String) model.getValueAt(selectedRow, 2);
                
                ParticipantEntry entry = new ParticipantEntry(
                    "Member", 
                    memberId,
                    0,  // Guest ID is 0 for members
                    name,
                    email
                );
                
                participants.add(entry);
                participantsTableModel.addRow(new Object[]{
                    "Member",
                    name,
                    email,
                    "Remove"
                });
                
                dialog.dispose();
            }
        });
        
        dialog.setVisible(true);
    }
    
    private void showAddGuestDialog() {
        // First check if user has used all guest passes for the month
        try {
            Connection conn = dbConnection.getConnection();
            
            // Get current month and year
            Calendar cal = Calendar.getInstance();
            int month = cal.get(Calendar.MONTH) + 1; // Calendar months are 0-based
            int year = cal.get(Calendar.YEAR);
            
            // Check guest pass count
            String countQuery = "SELECT count FROM GuestPassCount WHERE member_id = ? AND month = ? AND year = ?";
            PreparedStatement countStmt = conn.prepareStatement(countQuery);
            countStmt.setInt(1, currentUser.getMemberId());
            countStmt.setInt(2, month);
            countStmt.setInt(3, year);
            
            ResultSet countRs = countStmt.executeQuery();
            
            int currentCount = 0;
            if (countRs.next()) {
                currentCount = countRs.getInt("count");
            }
            
            countRs.close();
            countStmt.close();
            
            if (currentCount >= 4) {
                JOptionPane.showMessageDialog(this, 
                    "You have already used all 4 guest passes for this month.", 
                    "Guest Pass Limit Reached", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            conn.close();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, 
                "Error checking guest pass count: " + ex.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // If we get here, user can add a guest
        JDialog dialog = new JDialog(this, "Add Guest", true);
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // First name
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("First Name:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        JTextField firstNameField = new JTextField(20);
        panel.add(firstNameField, gbc);
        
        // Last name
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Last Name:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        JTextField lastNameField = new JTextField(20);
        panel.add(lastNameField, gbc);
        
        // Email
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Email:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 2;
        JTextField emailField = new JTextField(20);
        panel.add(emailField, gbc);
        
        // Fee notice
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        panel.add(new JLabel("Note: A $5 guest fee will be charged to your account."), gbc);
        
        // Add button
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        JButton addButton = new JButton("Add Guest");
        panel.add(addButton, gbc);
        
        dialog.getContentPane().add(panel);
        
        // Add button action
        addButton.addActionListener(e -> {
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String email = emailField.getText().trim();
            
            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, 
                    "Please fill in all fields.", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // This will be finalized when the reservation is made
            // The guest ID will be assigned at that time
            ParticipantEntry entry = new ParticipantEntry(
                "Guest", 
                currentUser.getMemberId(),  // Host member ID
                -1,  // Temporary guest ID
                firstName + " " + lastName,
                email
            );
            
            participants.add(entry);
            participantsTableModel.addRow(new Object[]{
                "Guest",
                firstName + " " + lastName,
                email,
                "Remove"
            });
            
            dialog.dispose();
        });
        
        dialog.setVisible(true);
    }
    
    private void makeReservation() {
        // Validate reservation
        if (courtComboBox.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(this, 
                "Please select a court.", 
                "Validation Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Validate date (must be within 1 week)
        Date selectedDate = dateChooser.getDate();
        if (selectedDate == null) {
            JOptionPane.showMessageDialog(this, 
                "Please select a date.", 
                "Validation Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        LocalDate today = LocalDate.now();
        LocalDate selectedLocalDate = selectedDate.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDate();
        
        long daysBetween = ChronoUnit.DAYS.between(today, selectedLocalDate);
        if (daysBetween < 0 || daysBetween > 7) {
            JOptionPane.showMessageDialog(this, 
                "Reservations must be made between today and 1 week in advance.", 
                "Validation Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Validate time (must be after 7am)
        Date selectedTime = (Date) timeSpinner.getValue();
        Calendar cal = Calendar.getInstance();
        cal.setTime(selectedTime);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        
        if (hour < 7) {
            JOptionPane.showMessageDialog(this, 
                "Reservations must be after 7:00 AM.", 
                "Validation Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Validate participants based on type
        String type = (String) reservationTypeComboBox.getSelectedItem();
        int minParticipants = "Singles".equals(type) ? 2 : 3;
        
        if (participants.size() < minParticipants) {
            JOptionPane.showMessageDialog(this, 
                "Singles requires at least 2 participants, and Doubles requires at least 3 participants.", 
                "Validation Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Calculate end time
        int durationMinutes = (Integer) durationSpinner.getValue();
        Calendar endCal = (Calendar) cal.clone();
        endCal.add(Calendar.MINUTE, durationMinutes);
        
        // Check if court is available
        int courtIndex = courtComboBox.getSelectedIndex();
        int courtId = courtIndex + 1; // Simplified - in real app, get actual court ID
        
        if (!isCourtAvailable(courtId, selectedLocalDate, 
                            String.format("%02d:%02d:00", hour, minute), 
                            String.format("%02d:%02d:00", endCal.get(Calendar.HOUR_OF_DAY), endCal.get(Calendar.MINUTE)))) {
            JOptionPane.showMessageDialog(this, 
                "The selected court is not available for this time slot.", 
                "Court Unavailable", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Now make the reservation
        try {
            Connection conn = dbConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction
            
            try {
                // 1. Insert into Reservations table
                String reservationQuery = "INSERT INTO Reservations " +
                	    "(court_id, member_id, reservation_date, start_time, end_time, reservation_type, created_at) " +
                	    "VALUES (?, ?, ?, ?, ?, ?, datetime('now'))";
                
                PreparedStatement reservationStmt = conn.prepareStatement(reservationQuery, Statement.RETURN_GENERATED_KEYS);
                reservationStmt.setInt(1, courtId);
                reservationStmt.setInt(2, currentUser.getMemberId());
                
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String formattedDate = dateFormat.format(selectedDate);
                reservationStmt.setString(3, formattedDate);
                
                Time startTime = new Time(selectedTime.getTime());
                reservationStmt.setTime(4, startTime);
                
                Time endTime = new Time(endCal.getTimeInMillis());
                reservationStmt.setTime(5, endTime);
                
                reservationStmt.setString(6, type);
                
                reservationStmt.executeUpdate();
                
                ResultSet reservationKeys = reservationStmt.getGeneratedKeys();
                if (!reservationKeys.next()) {
                    throw new SQLException("Failed to get reservation ID");
                }
                
                int reservationId = reservationKeys.getInt(1);
                reservationKeys.close();
                reservationStmt.close();
                
                // 2. Add participants
                for (ParticipantEntry participant : participants) {
                    if ("Member".equals(participant.getType())) {
                        // Add member participant
                        String memberQuery = "INSERT INTO ReservationParticipants " +
                            "(reservation_id, member_id, guest_id) VALUES (?, ?, NULL)";
                        
                        PreparedStatement memberStmt = conn.prepareStatement(memberQuery);
                        memberStmt.setInt(1, reservationId);
                        memberStmt.setInt(2, participant.getMemberId());
                        memberStmt.executeUpdate();
                        memberStmt.close();
                    } else {
                        // Add guest - first create guest record
                        String guestQuery = "INSERT INTO Guests " +
                        	    "(first_name, last_name, email, host_member_id, visit_date) " +
                        	    "VALUES (?, ?, ?, ?, ?)";
                        
                 
                        
                     
                        
                        String[] nameParts = participant.getName().split(" ", 2);
                        String firstName = nameParts[0];
                        String lastName = nameParts.length > 1 ? nameParts[1] : "";

                        // Format date as string in YYYY-MM-DD format for SQLite compatibility
                        SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd");
                        String formattedDate2 = dateFormat2.format(selectedDate);

                        PreparedStatement guestStmt = conn.prepareStatement(guestQuery, Statement.RETURN_GENERATED_KEYS);
                        guestStmt.setString(1, firstName);
                        guestStmt.setString(2, lastName);
                        guestStmt.setString(3, participant.getEmail());
                        guestStmt.setInt(4, currentUser.getMemberId());
                        guestStmt.setString(5, formattedDate); // Use setString instead of setDate

                        guestStmt.executeUpdate();
                        
                        ResultSet guestKeys = guestStmt.getGeneratedKeys();
                        if (!guestKeys.next()) {
                            throw new SQLException("Failed to get guest ID");
                        }
                        
                        int guestId = guestKeys.getInt(1);
                        guestKeys.close();
                        guestStmt.close();
                        
                        // Add guest participant
                        String participantQuery = "INSERT INTO ReservationParticipants " +
                            "(reservation_id, member_id, guest_id) VALUES (?, NULL, ?)";
                        
                        PreparedStatement participantStmt = conn.prepareStatement(participantQuery);
                        participantStmt.setInt(1, reservationId);
                        participantStmt.setInt(2, guestId);
                        participantStmt.executeUpdate();
                        participantStmt.close();
                        
                        // Add guest fee
                        String feeQuery = "INSERT INTO GuestFees " +
                                "(member_id, guest_id, amount, date_applied, is_paid) " +
                                "VALUES (?, ?, 5.00, date('now'), 0)";
                        
                        PreparedStatement feeStmt = conn.prepareStatement(feeQuery);
                        feeStmt.setInt(1, currentUser.getMemberId());
                        feeStmt.setInt(2, guestId);
                        feeStmt.executeUpdate();
                        feeStmt.close();
                        
                        // Update guest pass count
                        Calendar currentCal = Calendar.getInstance();
                        int month = currentCal.get(Calendar.MONTH) + 1; // Calendar months are 0-based
                        int year = currentCal.get(Calendar.YEAR);
                        
                        String insertQuery = "INSERT OR IGNORE INTO GuestPassCount (member_id, month, year, count) " +
                                "VALUES (?, ?, ?, 1)";
                                
            PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
            insertStmt.setInt(1, currentUser.getMemberId());
            insertStmt.setInt(2, month);
            insertStmt.setInt(3, year);
            insertStmt.executeUpdate();
            insertStmt.close();

            // Then update the count if the record already existed
            String updateQuery = "UPDATE GuestPassCount SET count = count + 1 " +
                               "WHERE member_id = ? AND month = ? AND year = ? " +
                               "AND count < 4"; // Optional: ensure we don't exceed 4 passes
                               
            PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
            updateStmt.setInt(1, currentUser.getMemberId());
            updateStmt.setInt(2, month);
            updateStmt.setInt(3, year);
            updateStmt.executeUpdate();
            updateStmt.close();
                        
                      
                    }
                }
                
                conn.commit();
                JOptionPane.showMessageDialog(this, 
                    "Reservation successfully created!", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
                
                dispose(); // Close the reservation screen
                
            } catch (SQLException ex) {
                conn.rollback();
                JOptionPane.showMessageDialog(this, 
                    "Error creating reservation: " + ex.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            } finally {
                conn.setAutoCommit(true);
                conn.close();
            }
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, 
                "Database error: " + ex.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private boolean isCourtAvailable(int courtId, LocalDate date, String startTime, String endTime) {
        try {
            Connection conn = dbConnection.getConnection();
            
            String query = "SELECT COUNT(*) FROM Reservations " +
                          "WHERE court_id = ? AND reservation_date = ? " +
                          "AND ((start_time <= ? AND end_time > ?) OR " +
                          "(start_time < ? AND end_time >= ?) OR " +
                          "(start_time >= ? AND end_time <= ?))";
            
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, courtId);
            stmt.setDate(2, java.sql.Date.valueOf(date));
            stmt.setString(3, startTime);
            stmt.setString(4, startTime);
            stmt.setString(5, endTime);
            stmt.setString(6, endTime);
            stmt.setString(7, startTime);
            stmt.setString(8, endTime);
            
            ResultSet rs = stmt.executeQuery();
            rs.next();
            int count = rs.getInt(1);
            
            rs.close();
            stmt.close();
            conn.close();
            
            return count == 0;
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, 
                "Error checking court availability: " + ex.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    // class to represent a participant entry
    private class ParticipantEntry {
        private String type;
        private int memberId;
        private int guestId;
        private String name;
        private String email;
        
        public ParticipantEntry(String type, int memberId, int guestId, String name, String email) {
            this.type = type;
            this.memberId = memberId;
            this.guestId = guestId;
            this.name = name;
            this.email = email;
        }
        
        public String getType() { return type; }
        public int getMemberId() { return memberId; }
        public int getGuestId() { return guestId; }
        public String getName() { return name; }
        public String getEmail() { return email; }
    }
    
    // Helper class for buttons in JTable
    private class ButtonColumn extends AbstractCellEditor implements TableCellRenderer, TableCellEditor, ActionListener {
        private JTable table;
        private Action action;
        private JButton renderButton;
        private JButton editButton;
        private String text;
        
        public ButtonColumn(JTable table, Action action, int column) {
            this.table = table;
            this.action = action;
            
            renderButton = new JButton();
            editButton = new JButton();
            editButton.setFocusPainted(false);
            editButton.addActionListener(this);
            
            TableColumnModel columnModel = table.getColumnModel();
            columnModel.getColumn(column).setCellRenderer(this);
            columnModel.getColumn(column).setCellEditor(this);
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value == null) {
                renderButton.setText("");
            } else {
                renderButton.setText(value.toString());
            }
            return renderButton;
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            text = (value == null) ? "" : value.toString();
            editButton.setText(text);
            return editButton;
        }
        
        @Override
        public Object getCellEditorValue() {
            return text;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            int row = table.convertRowIndexToModel(table.getEditingRow());
            fireEditingStopped();
            
            ActionEvent event = new ActionEvent(
                table,
                ActionEvent.ACTION_PERFORMED,
                "" + row);
            
            action.actionPerformed(event);
        }
    }
    
    
    private class JDateChooser extends JPanel {
        private JTextField dateField;
        private JButton dateButton;
        private Date date;
        private Date minSelectableDate;
        private Date maxSelectableDate;
        private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        
        public JDateChooser() {
            setLayout(new BorderLayout(5, 0));
            
            dateField = new JTextField();
            dateField.setEditable(false);
            dateButton = new JButton("...");
            
            add(dateField, BorderLayout.CENTER);
            add(dateButton, BorderLayout.EAST);
            
            dateButton.addActionListener(e -> showDatePickerDialog());
        }
        
        public void setDate(Date date) {
            this.date = date;
            if (date != null) {
                dateField.setText(dateFormat.format(date));
            } else {
                dateField.setText("");
            }
        }
        
        public Date getDate() {
            return date;
        }
        
        public void setMinSelectableDate(Date date) {
            this.minSelectableDate = date;
        }
        
        public void setMaxSelectableDate(Date date) {
            this.maxSelectableDate = date;
        }
        
        private void showDatePickerDialog() {
            
            String input = JOptionPane.showInputDialog(this, 
                "Enter date (MM/dd/yyyy):", 
                dateField.getText());
            
            if (input != null && !input.trim().isEmpty()) {
                try {
                    Date selectedDate = dateFormat.parse(input);
                    
                    // Validate against min/max dates
                    if (minSelectableDate != null && selectedDate.before(minSelectableDate)) {
                        JOptionPane.showMessageDialog(this, 
                            "Date cannot be before " + dateFormat.format(minSelectableDate), 
                            "Invalid Date", 
                            JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    if (maxSelectableDate != null && selectedDate.after(maxSelectableDate)) {
                        JOptionPane.showMessageDialog(this, 
                            "Date cannot be after " + dateFormat.format(maxSelectableDate), 
                            "Invalid Date", 
                            JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    setDate(selectedDate);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, 
                        "Invalid date format. Please use MM/dd/yyyy.", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}