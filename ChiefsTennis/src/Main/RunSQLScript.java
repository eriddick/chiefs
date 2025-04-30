// package Main;

// import java.sql.*;

// /**
//  * A utility class to run the SQL script to create the Payments table.
//  */
// public class RunSQLScript {
//     public static void main(String[] args) {
//         DatabaseConnection dbConnection = new DatabaseConnection();
        
//         try {
//             Connection conn = dbConnection.getConnection();
//             Statement stmt = conn.createStatement();
            
//             // Create Payments table if it doesn't exist
//             String createPaymentsTable = 
//                 // "CREATE TABLE IF NOT EXISTS Payments (" +
//                 "    payment_id INTEGER PRIMARY KEY AUTOINCREMENT," +
//                 "    bill_id INTEGER NOT NULL," +
//                 "    member_id INTEGER NOT NULL," +
//                 "    payment_date TEXT NOT NULL," +
//                 "    amount REAL NOT NULL," +
//                 "    payment_method TEXT NOT NULL," +
//                 "    FOREIGN KEY (bill_id) REFERENCES Bills(bill_id)," +
//                 "    FOREIGN KEY (member_id) REFERENCES Members(member_id)" +
//                 ");";
            
//             String createIndexBillId = 
//                 "CREATE INDEX IF NOT EXISTS idx_payment_bill_id ON Payments(bill_id);";
            
//             String createIndexMemberId = 
//                 "CREATE INDEX IF NOT EXISTS idx_payment_member_id ON Payments(member_id);";
            
//             // Execute the SQL statements
//             stmt.execute(createPaymentsTable);
//             stmt.execute(createIndexBillId);
//             stmt.execute(createIndexMemberId);
            
//             System.out.println("Payments table created successfully!");
            
//             // Close resources
//             stmt.close();
//             conn.close();
            
//         } catch (SQLException e) {
//             System.err.println("Error creating Payments table: " + e.getMessage());
//             e.printStackTrace();
//         }
//     }
// }