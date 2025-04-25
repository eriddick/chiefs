// package Main;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.Mockito.*;

// import java.sql.Connection;
// import java.sql.PreparedStatement;
// import java.sql.ResultSet;
// import java.sql.SQLException;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.mockito.Mock;
// import org.mockito.MockitoAnnotations;

// import org.junit.jupiter.api.Test;

// public class DatabaseConnectionTest {
    
//     private DatabaseConnection dbConnection;
    
//     @Mock
//     private Connection mockConnection;
    
//     @Mock
//     private PreparedStatement mockStatement;
    
//     @Mock
//     private ResultSet mockResultSet;
    
//     @BeforeEach
//     public void setUp() throws SQLException {
//         MockitoAnnotations.openMocks(this);
        
//         // create test instance with path for testing
//         dbConnection = new DatabaseConnection("jdbc:sqlite:test_tennis_club.db");
        
//         // mock behavior
//         when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
//         when(mockStatement.executeQuery()).thenReturn(mockResultSet);
//     }
//     //integration testing
//     @Test
//     public void testGetConnection() throws SQLException {
//         // This test requires a real database connection
//         Connection conn = dbConnection.getConnection();
//         assertNotNull(conn);
//         conn.close();
//     }
    
//     //unit testing
//     @Test
//     public void testExecuteQuery() throws SQLException {
//         //create an implementation for testing
//         DatabaseConnection testDb = new DatabaseConnection() {
//             @Override
//             public Connection getConnection() throws SQLException {
//                 return mockConnection;
//             }
//         };
        
//         when(mockResultSet.next()).thenReturn(true, false);
//         when(mockResultSet.getInt(1)).thenReturn(42);
        
//         ResultSet rs = testDb.executeQuery("SELECT COUNT(*) FROM Members");
//         assertTrue(rs.next());
//         assertEquals(42, rs.getInt(1));
//     }
// }