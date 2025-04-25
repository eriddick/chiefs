// package Main;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.contains;
// import static org.mockito.Mockito.*;

// import Main.LoginScreen;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;

// import javax.swing.*;
// import java.sql.*;

// public class LoginScreenTest {

//     private LoginScreen loginScreen;
//     private DatabaseConnection mockDbConnection;
//     private Connection mockConnection;
//     private PreparedStatement mockStatement;
//     private ResultSet mockResultSet;

//     @BeforeEach
//     public void setUp() throws Exception {
//         mockDbConnection = mock(DatabaseConnection.class);
//         mockConnection = mock(Connection.class);
//         mockStatement = mock(PreparedStatement.class);
//         mockResultSet = mock(ResultSet.class);

//         when(mockDbConnection.getConnection()).thenReturn(mockConnection);
//         when(mockConnection.prepareStatement(any())).thenReturn(mockStatement);

//         loginScreen = new LoginScreen(mockDbConnection);
//     }

//     @Test
//     @DisplayName("Login fails with empty username and password")
//     public void testEmptyFields() {
//         loginScreen.getUsernameField().setText("");
//         loginScreen.getPasswordField().setText("");

//         loginScreen.getLoginButton().doClick();

//         // This will trigger a JOptionPane, but for now we just test that no exception is thrown
//         assertEquals("", loginScreen.getUsernameField().getText());
//     }

//     @Test
//     @DisplayName("Login fails with incorrect credentials")
//     public void testInvalidCredentials() throws Exception {
//         loginScreen.getUsernameField().setText("wronguser");
//         loginScreen.getPasswordField().setText("wrongpass");

//         when(mockStatement.executeQuery()).thenReturn(mockResultSet);
//         when(mockResultSet.next()).thenReturn(false); // no match in DB

//         loginScreen.getLoginButton().doClick();

//         verify(mockStatement, times(1)).executeQuery();
//     }

//     @Test
//     @DisplayName("Login success for ADMIN user")
//     public void testValidAdminLogin() throws Exception {
//         loginScreen.getUsernameField().setText("admin");
//         loginScreen.getPasswordField().setText("password");

//         // Simulate DB user found
//         when(mockStatement.executeQuery()).thenReturn(mockResultSet);
//         when(mockResultSet.next()).thenReturn(true);

//         when(mockResultSet.getInt("user_id")).thenReturn(1);
//         when(mockResultSet.getString("role")).thenReturn("ADMIN");
//         when(mockResultSet.getInt("member_id")).thenReturn(10);
//         when(mockResultSet.getString("first_name")).thenReturn("john");
//         when(mockResultSet.getString("last_name")).thenReturn("doe");

//         PreparedStatement updateStmt = mock(PreparedStatement.class);
//         when(mockConnection.prepareStatement(contains("UPDATE Users SET"))).thenReturn(updateStmt);

//         loginScreen.getLoginButton().doClick();

//         verify(updateStmt, times(1)).executeUpdate();
//     }
// }
