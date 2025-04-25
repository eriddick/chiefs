// package Main;

// import org.junit.Test;

// import static org.junit.jupiter.api.Assertions.assertTrue;

// import java.awt.event.ActionEvent;
// import java.awt.event.ActionListener;
// import java.sql.SQLException;
// import java.time.LocalDate;
// import java.time.ZoneId;
// import java.util.Calendar;
// import java.util.Date;

// import javax.swing.Timer;

// import org.junit.Before;
// import org.junit.jupiter.api.DisplayName;

// import Main.CourtReservationScreen.ParticipantEntry;

// public class CourtReservationScreenTest {

//     User user;
//     CourtReservationScreen courtReservationScreen;
//     boolean correctWindowDisplayed = false;

//     @Before
//     public void setup() {
//         user = new User(1, "jdoe", "MEMBER", 1, "John", "Doe"); // user to test with; needed to create a screen class
//         courtReservationScreen = new CourtReservationScreen(user); // create mock of the screen class
//     }

//     @Test
//     @DisplayName("Court 1 on 04/11/2025 from 14:00 to 15:00 - 2-3pm")
//     public void isCourtAvailable_Valid() throws SQLException {

//         assertTrue(
//                 courtReservationScreen.isCourtAvailable(
//                         1, LocalDate.of(2025, 04, 12), "14:00:00", "15:00:00"),
//                 "Court 1 is available");
//     }

//     @Test
//     @DisplayName("Court 1 on 13/15/2025 from 07:00 to 08:00")
//     public void isCourtAvailable_Busy() throws SQLException {
//         assertTrue(
//                 !courtReservationScreen.isCourtAvailable(
//                         1, LocalDate.of(2025, 04, 12), "07:00:00", "08:00:00"),
//                 "Court 1 is NOT available on the wrong month");
//     }

//     @Test
//     @DisplayName("Make Reservation in the past")
//     public void makeReservation_inThePast() throws SQLException {

//         correctWindowDisplayed = false;
//         courtReservationScreen.courtComboBox.setSelectedIndex(1);
//         Date d = Date.from(LocalDate.of(2025, 03, 12).atStartOfDay(ZoneId.systemDefault()).toInstant());
//         courtReservationScreen.dateChooser.setDate(d);

//         Timer timer = new Timer(3000, new ActionListener() {
//             @Override
//             public void actionPerformed(ActionEvent e) {
//                 // Close the message dialog
//                 closeMessageDialog("Validation Error");
//             }

//         });
//         timer.setRepeats(false); // Ensure the timer only runs once
//         timer.start();

//         courtReservationScreen.makeReservation();
//         assertTrue(correctWindowDisplayed, "Reservation done in the past");

//     }

//     @Test
//     @DisplayName("Make Reservation too early")
//     public void makeReservation_tooEarly() throws SQLException {

//         correctWindowDisplayed = false;
//         courtReservationScreen.courtComboBox.setSelectedIndex(1);
//         Date d = Date.from(LocalDate.of(2025, 04, 12).atStartOfDay(ZoneId.systemDefault()).toInstant());
//         courtReservationScreen.dateChooser.setDate(d);

//         Calendar defaultTime = Calendar.getInstance();
//         defaultTime.set(Calendar.HOUR_OF_DAY, 5);
//         defaultTime.set(Calendar.MINUTE, 0);
//         courtReservationScreen.timeSpinner.setValue(defaultTime.getTime());

//         Timer timer = new Timer(3000, new ActionListener() {
//             @Override
//             public void actionPerformed(ActionEvent e) {
//                 // Close the message dialog
//                 closeMessageDialog("Validation Error");
//             }

//         });
//         timer.setRepeats(false); // Ensure the timer only runs once
//         timer.start();

//         courtReservationScreen.makeReservation();
//         assertTrue(correctWindowDisplayed, "Reservation done too early");

//     }

//     @Test
//     @DisplayName("Make Reservation too far in a future")
//     public void makeReservation_inTheFuture() throws SQLException {

//         correctWindowDisplayed = false;
//         courtReservationScreen.courtComboBox.setSelectedIndex(1);
//         Date d = Date.from(LocalDate.of(2026, 03, 12).atStartOfDay(ZoneId.systemDefault()).toInstant());
//         courtReservationScreen.dateChooser.setDate(d);

//         Timer timer = new Timer(3000, new ActionListener() {
//             @Override
//             public void actionPerformed(ActionEvent e) {
//                 // Close the message dialog
//                 closeMessageDialog("Validation Error");
//             }

//         });
//         timer.setRepeats(false); // Ensure the timer only runs once
//         timer.start();

//         courtReservationScreen.makeReservation();
//         assertTrue(correctWindowDisplayed, "Reservation done too far in a future");

//     }

//     @Test
//     @DisplayName("Make Reservation without court selection")
//     public void makeReservation_noCourtSelection() throws SQLException {

//         correctWindowDisplayed = false;
//         courtReservationScreen.courtComboBox.setSelectedIndex(-1);
//         Date d = Date.from(LocalDate.of(2025, 04, 12).atStartOfDay(ZoneId.systemDefault()).toInstant());
//         courtReservationScreen.dateChooser.setDate(d);

//         Timer timer = new Timer(3000, new ActionListener() {
//             @Override
//             public void actionPerformed(ActionEvent e) {
//                 // Close the message dialog
//                 closeMessageDialog("Validation Error");
//             }

//         });
//         timer.setRepeats(false); // Ensure the timer only runs once
//         timer.start();

//         courtReservationScreen.makeReservation();
//         assertTrue(correctWindowDisplayed, "Reservation done in the past");

//     }

//     @Test
//     @DisplayName("Make Reservation without date selection")
//     public void makeReservation_noDateSelection() throws SQLException {

//         correctWindowDisplayed = false;
//         courtReservationScreen.courtComboBox.setSelectedIndex(1);
//         Date d = Date.from(LocalDate.of(2025, 04, 12).atStartOfDay(ZoneId.systemDefault()).toInstant());
//         courtReservationScreen.dateChooser.setDate(null);

//         Timer timer = new Timer(3000, new ActionListener() {
//             @Override
//             public void actionPerformed(ActionEvent e) {
//                 // Close the message dialog
//                 closeMessageDialog("Validation Error");
//             }

//         });
//         timer.setRepeats(false); // Ensure the timer only runs once
//         timer.start();

//         courtReservationScreen.makeReservation();
//         assertTrue(correctWindowDisplayed, "Reservation done in the past");

//     }

//     @Test
//     @DisplayName("Make Reservation with too few participans")
//     public void makeReservation_tooFewParticipants() throws SQLException {

//         correctWindowDisplayed = false;
//         courtReservationScreen.courtComboBox.setSelectedIndex(1);
//         Date d = Date.from(LocalDate.of(2025, 04, 12).atStartOfDay(ZoneId.systemDefault()).toInstant());
//         courtReservationScreen.dateChooser.setDate(d);

//         Calendar defaultTime = Calendar.getInstance();
//         defaultTime.set(Calendar.HOUR_OF_DAY, 15);
//         defaultTime.set(Calendar.MINUTE, 0);
//         courtReservationScreen.timeSpinner.setValue(defaultTime.getTime());

//         Timer timer = new Timer(3000, new ActionListener() {
//             @Override
//             public void actionPerformed(ActionEvent e) {
//                 // Close the message dialog
//                 closeMessageDialog("Validation Error");
//             }

//         });
//         timer.setRepeats(false); // Ensure the timer only runs once
//         timer.start();

//         courtReservationScreen.makeReservation();
//         assertTrue(correctWindowDisplayed, "Make Reservation with too few participants");

//     }

//     @Test
//     @DisplayName("Make Reservation when court is unavailable")
//     public void makeReservation_courtUnavailable() throws SQLException {

//         correctWindowDisplayed = false;
//         courtReservationScreen.courtComboBox.setSelectedIndex(1);
//         Date d = Date.from(LocalDate.of(2025, 04, 12).atStartOfDay(ZoneId.systemDefault()).toInstant());
//         courtReservationScreen.dateChooser.setDate(d);

//         Calendar defaultTime = Calendar.getInstance();
//         defaultTime.set(Calendar.HOUR_OF_DAY, 7);
//         defaultTime.set(Calendar.MINUTE, 0);
//         courtReservationScreen.timeSpinner.setValue(defaultTime.getTime());

//         ParticipantEntry entry = courtReservationScreen.new ParticipantEntry(
//                 "Member",
//                 3,
//                 0,
//                 "John" + " " + "Doe",
//                 "jdoe@gmail.com");

//         courtReservationScreen.participants.add(entry);

//         Timer timer = new Timer(3000, new ActionListener() {
//             @Override
//             public void actionPerformed(ActionEvent e) {
//                 // Close the message dialog
//                 closeMessageDialog("Court Unavailable");
//             }

//         });
//         timer.setRepeats(false); // Ensure the timer only runs once
//         timer.start();

//         courtReservationScreen.makeReservation();
//         assertTrue(correctWindowDisplayed, "Make Reservation when court is unavailable");

//     }

//     @Test
//     @DisplayName("Make Successfull Reservation")
//     public void makeReservation_success() throws SQLException {

//         correctWindowDisplayed = false;
//         courtReservationScreen.courtComboBox.setSelectedIndex(1);
//         Date d = Date.from(LocalDate.of(2025, 04, 12).atStartOfDay(ZoneId.systemDefault()).toInstant());
//         courtReservationScreen.dateChooser.setDate(d);

//         Calendar defaultTime = Calendar.getInstance();
//         defaultTime.set(Calendar.HOUR_OF_DAY, 15);
//         defaultTime.set(Calendar.MINUTE, 0);
//         courtReservationScreen.timeSpinner.setValue(defaultTime.getTime());

//         ParticipantEntry entry = courtReservationScreen.new ParticipantEntry(
//                 "Member",
//                 3,
//                 0,
//                 "John" + " " + "Doe",
//                 "jdoe@gmail.com");

//         courtReservationScreen.participants.add(entry);

//         Timer timer = new Timer(3000, new ActionListener() {
//             @Override
//             public void actionPerformed(ActionEvent e) {
//                 // Close the message dialog
//                 closeMessageDialog("Success");
//             }

//         });
//         timer.setRepeats(false); // Ensure the timer only runs once
//         timer.start();

//         courtReservationScreen.makeReservation();
//         assertTrue(correctWindowDisplayed, "Success");

//     }

//     private void closeMessageDialog(String title) {
//         // Iterate through all windows to find the message dialog and close it
//         java.awt.Window[] windows = java.awt.Window.getWindows();
//         for (java.awt.Window window : windows) {
//             if (window instanceof javax.swing.JDialog) {
//                 javax.swing.JDialog dialog = (javax.swing.JDialog) window;
//                 if (dialog.getTitle().equals(title)) {
//                     correctWindowDisplayed = true;
//                 }
//                 // if (dialog.get)
//                 dialog.dispose();
//                 // return;
//                 // }
//             }
//         }
//     }

// }
