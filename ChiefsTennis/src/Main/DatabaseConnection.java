package Main;

import java.sql.*;
import java.io.File;

// Database connection class for SQLite
 
public class DatabaseConnection {
    private String dbPath;
    
    // Default constructor using default database path
    
    public DatabaseConnection() {
        this.dbPath = "jdbc:sqlite:tennis_club.db";
    }
    
    //Constructor with custom database path
    
    public DatabaseConnection(String dbPath) {
        if (dbPath == null || dbPath.isEmpty()) {
            this.dbPath = "jdbc:sqlite:tennis_club.db"; // Fallback to default
        } else {
            this.dbPath = dbPath;
        }
    }
    
    // Get a database connection
    
    public Connection getConnection() throws SQLException {
        // Ensure SQLite JDBC driver is loaded
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC Driver not found", e);
        }
        
        // Create database directory if it doesn't exist
        File dbFile = new File(dbPath.replace("jdbc:sqlite:", ""));
        if (!dbFile.exists()) {
            File parentDir = dbFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
        }
        
        // Create and return the connection
        return DriverManager.getConnection(dbPath);
    }
    
    // Executes a query and returns a ResultSet
   
    public ResultSet executeQuery(String sql, Object... params) throws SQLException {
        Connection conn = getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);
        
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
        
        ResultSet rs = stmt.executeQuery();
        
      
        
        return rs;
    }
    
    // Executes an update query (INSERT, UPDATE, DELETE)
   
    public int executeUpdate(String sql, Object... params) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            
            return stmt.executeUpdate();
        } finally {
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        }
    }
    
    //Executes an insert query and returns the generated key
     
    public int executeInsert(String sql, Object... params) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            
            stmt.executeUpdate();
            
            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                // If we can't get a generated key, try to get the last inserted rowid
                Statement lastIdStmt = conn.createStatement();
                ResultSet lastIdRs = lastIdStmt.executeQuery("SELECT last_insert_rowid()");
                
                if (lastIdRs.next()) {
                    int lastId = lastIdRs.getInt(1);
                    lastIdRs.close();
                    lastIdStmt.close();
                    return lastId;
                }
                
                return -1;
            }
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        }
    }
    
    // Executes a transaction with multiple statements
    
    public boolean executeTransaction(Transaction transaction) {
        Connection conn = null;
        
        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            
            transaction.execute(conn);
            
            conn.commit();
            return true;
        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException rollbackEx) {
                System.err.println("Error rolling back transaction: " + rollbackEx.getMessage());
            }
            
            System.err.println("Transaction failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException closeEx) {
                System.err.println("Error closing connection: " + closeEx.getMessage());
            }
        }
    }
    
    //Functional interface for transactions
     
    public interface Transaction {
        void execute(Connection conn) throws SQLException;
    }
    
    // Initialize the database with tables and sample data
   
    public boolean initializeDatabase() {
        Connection conn = null;
        
        try {
            conn = getConnection();
            Statement stmt = conn.createStatement();
            
            // Execute the initialization script
           
            String[] initStatements = getInitializationScript();
            
            conn.setAutoCommit(false);
            
            ResultSet tables = conn.getMetaData().getTables(null, null, "Members", null);
            boolean tablesExist = tables.next();
            tables.close();
            
            if (tablesExist) {
                System.out.println("Database tables already exist. Skipping initialization.");
                conn.commit();
                return true;
            }
            
            
         // If tables don't exist, create them and add sample data
            System.out.println("Creating database tables and adding sample data...");
            for (String statement : initStatements) {
                if (!statement.trim().isEmpty()) {
                    try {
                        stmt.execute(statement);
                    } catch (SQLException e) {
                        System.err.println("Error executing statement: " + statement);
                        System.err.println("Error message: " + e.getMessage());
                    
                    }
                }
            }
            
            
            
            
            
            for (String statement : initStatements) {
                if (!statement.trim().isEmpty()) {
                    stmt.execute(statement);
                }
            }
            
            conn.commit();
            
            System.out.println("Database initialized successfully.");
            return true;
            
        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException rollbackEx) {
                System.err.println("Error rolling back transaction: " + rollbackEx.getMessage());
            }
            
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException closeEx) {
                System.err.println("Error closing connection: " + closeEx.getMessage());
            }
        }
    }
    
    
    private String[] getInitializationScript() {
    	
        // can be loaded from a file
        return new String[] {
            // Create the Members table
            "CREATE TABLE IF NOT EXISTS Members (" +
            "    member_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    first_name TEXT NOT NULL," +
            "    last_name TEXT NOT NULL," +
            "    email TEXT UNIQUE NOT NULL," +
            "    phone TEXT," +
            "    join_date TEXT NOT NULL," +  // SQLite doesn't have a DATE type, we use TEXT
            "    status TEXT NOT NULL," +      // SQLite doesn't have ENUM, we use TEXT
            "    show_email INTEGER DEFAULT 0," + // SQLite uses INTEGER for BOOLEAN (0=false, 1=true)
            "    show_phone INTEGER DEFAULT 0" +
            ");",
            
            // Create the Users table
            "CREATE TABLE IF NOT EXISTS Users (" +
            "    user_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    username TEXT UNIQUE NOT NULL," +
            "    password TEXT NOT NULL," +
            "    role TEXT NOT NULL," +        
            "    member_id INTEGER," +
            "    last_login TEXT," +          
            "    FOREIGN KEY (member_id) REFERENCES Members(member_id)" +
            ");",
            
            // Create the Courts table
            "CREATE TABLE IF NOT EXISTS Courts (" +
            "    court_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    court_number INTEGER NOT NULL," +
            "    court_type TEXT NOT NULL" +
            ");",
            
            // Create the Reservations table
            "CREATE TABLE IF NOT EXISTS Reservations (" +
            "    reservation_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    court_id INTEGER NOT NULL," +
            "    member_id INTEGER NOT NULL," +
            "    reservation_date TEXT NOT NULL," +  
            "    start_time TEXT NOT NULL," +       
            "    end_time TEXT NOT NULL," +          
            "    reservation_type TEXT NOT NULL," +  
            "    created_at TEXT NOT NULL," +        
            "    FOREIGN KEY (court_id) REFERENCES Courts(court_id)," +
            "    FOREIGN KEY (member_id) REFERENCES Members(member_id)" +
            ");",
            
            // Create the Guests table
            "CREATE TABLE IF NOT EXISTS Guests (" +
            "    guest_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    first_name TEXT NOT NULL," +
            "    last_name TEXT NOT NULL," +
            "    email TEXT NOT NULL," +
            "    host_member_id INTEGER NOT NULL," +
            "    visit_date TEXT NOT NULL," +  
            "    FOREIGN KEY (host_member_id) REFERENCES Members(member_id)" +
            ");",
            
            // Create the ReservationParticipants table
            "CREATE TABLE IF NOT EXISTS ReservationParticipants (" +
            "    participant_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    reservation_id INTEGER NOT NULL," +
            "    member_id INTEGER," +
            "    guest_id INTEGER," +
            "    FOREIGN KEY (reservation_id) REFERENCES Reservations(reservation_id)," +
            "    FOREIGN KEY (member_id) REFERENCES Members(member_id)," +
            "    FOREIGN KEY (guest_id) REFERENCES Guests(guest_id)" +
            ");",
            
            // Create the MembershipFees table
            "CREATE TABLE IF NOT EXISTS MembershipFees (" +
            "    fee_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    member_id INTEGER NOT NULL," +
            "    fee_year INTEGER NOT NULL," +
            "    amount REAL NOT NULL," +      
            "    due_date TEXT NOT NULL," +    
            "    paid_date TEXT," +            
            "    is_paid INTEGER DEFAULT 0," + 
            "    FOREIGN KEY (member_id) REFERENCES Members(member_id)" +
            ");",
            
            // Create the LateFees table
            "CREATE TABLE IF NOT EXISTS LateFees (" +
            "    late_fee_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    member_id INTEGER NOT NULL," +
            "    fee_id INTEGER NOT NULL," +
            "    amount REAL NOT NULL," +      
            "    month_applied TEXT NOT NULL," + 
            "    is_paid INTEGER DEFAULT 0," +   
            "    FOREIGN KEY (member_id) REFERENCES Members(member_id)," +
            "    FOREIGN KEY (fee_id) REFERENCES MembershipFees(fee_id)" +
            ");",
            
            // Create the GuestFees table
            "CREATE TABLE IF NOT EXISTS GuestFees (" +
            "    guest_fee_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    member_id INTEGER NOT NULL," +
            "    guest_id INTEGER NOT NULL," +
            "    amount REAL NOT NULL DEFAULT 5.00," +  
            "    date_applied TEXT NOT NULL," +         
            "    is_paid INTEGER DEFAULT 0," +          
            "    FOREIGN KEY (member_id) REFERENCES Members(member_id)," +
            "    FOREIGN KEY (guest_id) REFERENCES Guests(guest_id)" +
            ");",
            
            // Create the MemberHistory table
            "CREATE TABLE IF NOT EXISTS MemberHistory (" +
            "    history_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    member_id INTEGER NOT NULL," +
            "    start_date TEXT NOT NULL," +  // SQLite doesn't have a DATE type, we use TEXT
            "    end_date TEXT," +             
            "    reason TEXT," +
            "    FOREIGN KEY (member_id) REFERENCES Members(member_id)" +
            ");",
            
            // Create the Bills table
            "CREATE TABLE IF NOT EXISTS Bills (" +
            "    bill_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    member_id INTEGER NOT NULL," +
            "    bill_date TEXT NOT NULL," +   
            "    total_amount REAL NOT NULL," + 
            "    due_date TEXT NOT NULL," +     
            "    is_paid INTEGER DEFAULT 0," +  // SQLite uses INTEGER for BOOLEAN (0=false, 1=true)
            "    sent_email INTEGER DEFAULT 0," + 
            "    FOREIGN KEY (member_id) REFERENCES Members(member_id)" +
            ");",
            
            // Create the BillItems table
            "CREATE TABLE IF NOT EXISTS BillItems (" +
            "    item_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    bill_id INTEGER NOT NULL," +
            "    description TEXT NOT NULL," +
            "    amount REAL NOT NULL," +      // SQLite uses REAL for DECIMAL
            "    item_type TEXT NOT NULL," +   
            "    reference_id INTEGER," +
            "    FOREIGN KEY (bill_id) REFERENCES Bills(bill_id)" +
            ");",
            
            // Create the GuestPassCount table
            "CREATE TABLE IF NOT EXISTS GuestPassCount (" +
            "    count_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    member_id INTEGER NOT NULL," +
            "    month INTEGER NOT NULL," +
            "    year INTEGER NOT NULL," +
            "    count INTEGER NOT NULL DEFAULT 0," +
            "    FOREIGN KEY (member_id) REFERENCES Members(member_id)" +
            ");",
            
            // Create indexes
            "CREATE INDEX IF NOT EXISTS idx_member_status ON Members(status);",
            "CREATE INDEX IF NOT EXISTS idx_reservation_date ON Reservations(reservation_date);",
            "CREATE INDEX IF NOT EXISTS idx_bill_due_date ON Bills(due_date);",
            "CREATE INDEX IF NOT EXISTS idx_bill_paid ON Bills(is_paid);",
            "CREATE INDEX IF NOT EXISTS idx_membership_fee_paid ON MembershipFees(is_paid);",
            
            // Insert initial sample data - Courts
            "INSERT INTO Courts (court_number, court_type) VALUES (1, 'Clay');",
            "INSERT INTO Courts (court_number, court_type) VALUES (2, 'Clay');",
            "INSERT INTO Courts (court_number, court_type) VALUES (3, 'Clay');",
            "INSERT INTO Courts (court_number, court_type) VALUES (4, 'Clay');",
            "INSERT INTO Courts (court_number, court_type) VALUES (5, 'Hard');",
            "INSERT INTO Courts (court_number, court_type) VALUES (6, 'Hard');",
            "INSERT INTO Courts (court_number, court_type) VALUES (7, 'Hard');",
            "INSERT INTO Courts (court_number, court_type) VALUES (8, 'Hard');",
            "INSERT INTO Courts (court_number, court_type) VALUES (9, 'Hard');",
            "INSERT INTO Courts (court_number, court_type) VALUES (10, 'Hard');",
            "INSERT INTO Courts (court_number, court_type) VALUES (11, 'Grass');",
            "INSERT INTO Courts (court_number, court_type) VALUES (12, 'Grass');",
            
            // Insert initial admin members
            "INSERT INTO Members (first_name, last_name, email, phone, join_date, status, show_email, show_phone) VALUES " +
            "('Admin', 'User', 'admin@tennisclub.com', '555-123-4567', '2020-01-01', 'ACTIVE', 1, 1);",
            
            "INSERT INTO Members (first_name, last_name, email, phone, join_date, status, show_email, show_phone) VALUES " +
            "('Treasurer', 'User', 'treasurer@tennisclub.com', '555-987-6543', '2020-01-01', 'ACTIVE', 1, 1);",
            
            // Insert admin users
            "INSERT INTO Users (username, password, role, member_id) VALUES " +
            "('admin', 'password', 'ADMIN', 1);",
            
            "INSERT INTO Users (username, password, role, member_id) VALUES " +
            "('treasurer', 'password', 'TREASURER', 2);",
            
            // Insert sample members
            "INSERT INTO Members (first_name, last_name, email, phone, join_date, status, show_email, show_phone) VALUES " +
            "('John', 'Doe', 'john.doe@example.com', '555-111-2222', '2022-03-15', 'ACTIVE', 1, 0);",
            
            "INSERT INTO Members (first_name, last_name, email, phone, join_date, status, show_email, show_phone) VALUES " +
            "('Jane', 'Smith', 'jane.smith@example.com', '555-222-3333', '2022-04-10', 'ACTIVE', 0, 0);",
            
            // Insert user accounts for members
            "INSERT INTO Users (username, password, role, member_id) VALUES " +
            "('jdoe', 'tennis3', 'MEMBER', 3);",
            
            "INSERT INTO Users (username, password, role, member_id) VALUES " +
            "('jsmith', 'tennis4', 'MEMBER', 4);"
        };
    }

    public String getDbPath() {
        return this.dbPath;
    }
    public File getDatabaseFile() {
        if (this.dbPath == null) {
            return new File("tennis_club.db");
        }
        String filePath = this.dbPath.replace("jdbc:sqlite:", "");
        return new File(filePath);
    }
}