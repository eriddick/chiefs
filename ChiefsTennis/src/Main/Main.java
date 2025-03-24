package Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;

/**
 * Main application class for the Tennis Club Management System with SQLite
 * This serves as the entry point and initializes all necessary components
 */
public class Main {
    private static final String CONFIG_FILE = "config.properties";
    private static DatabaseConnection dbConnection;
    
    public static void main(String[] args) {
        // Set look and feel to match the system
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Could not set system look and feel: " + e.getMessage());
        }
        
        // Load configuration and initialize database
        if (!initializeApplication()) {
            JOptionPane.showMessageDialog(null,
                "Failed to initialize the application. Please check the configuration and database connection.",
                "Initialization Error",
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        
        // Launch the application on the EDT
        SwingUtilities.invokeLater(() -> {
            showLoginScreen();
        });
    }
    
    /**
     * Initializes the application, loading configuration and setting up database
     * @return true if initialization succeeded, false otherwise
     */
    private static boolean initializeApplication() {
        try {
            // Load configuration
            Properties prop = new Properties();
            File configFile = new File(CONFIG_FILE);
            
            if (configFile.exists()) {
                FileInputStream fis = new FileInputStream(configFile);
                prop.load(fis);
                fis.close();
                
                // Initialize database connection with path from config
                String dbPath = prop.getProperty("db.path", "jdbc:sqlite:tennis_club.db");
                dbConnection = new DatabaseConnection(dbPath);
            } else {
                // Use default connection if config doesn't exist
                System.out.println("Config file not found. Using default SQLite database path.");
                dbConnection = new DatabaseConnection();
            }
            
            // Check if database needs initialization
            boolean needsInit = !databaseExists(dbConnection.getDbPath());
            
            // Test database connection
            Connection conn = dbConnection.getConnection();
            if (conn != null) {
                conn.close();
                
                // Initialize database if needed
                if (needsInit) {
                    System.out.println("Database does not exist. Initializing...");
                    return dbConnection.initializeDatabase();
                }
                
                return true;
            } else {
                return false;
            }
        } catch (IOException | SQLException e) {
            System.err.println("Error initializing application: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Check if the SQLite database file exists
     * @param dbPath JDBC connection string for SQLite
     * @return true if the database file exists, false otherwise
     */
    private static boolean databaseExists(String dbPath) {
        // Extract file path from JDBC URL
        String filePath = dbPath.replace("jdbc:sqlite:", "jdbc:sqlite:tennis_club.db");
        File dbFile = new File(filePath);
        return dbFile.exists() && dbFile.length() > 0;
    }
    
    /**
     * Creates and displays the login screen
     */
    private static void showLoginScreen() {
        LoginScreen loginScreen = new LoginScreen(dbConnection);
        loginScreen.setVisible(true);
    }
    
    /**
     * Creates a default configuration file if it doesn't exist
     */
    private static void createDefaultConfig() {
        File configFile = new File(CONFIG_FILE);
        if (!configFile.exists()) {
            try {
                Properties prop = new Properties();
                prop.setProperty("db.path", "jdbc:sqlite:tennis_club.db");
                prop.setProperty("app.title", "Tennis Club Management System");
                prop.setProperty("app.version", "1.0.0");
                prop.setProperty("app.debug", "false");
                
                // Email settings (disabled by default)
                prop.setProperty("email.enabled", "false");
                prop.setProperty("email.smtp.host", "smtp.example.com");
                prop.setProperty("email.smtp.port", "587");
                prop.setProperty("email.username", "notifications@example.com");
                prop.setProperty("email.password", "email_password");
                
                // Save properties to config file
                java.io.FileOutputStream fos = new java.io.FileOutputStream(configFile);
                prop.store(fos, "Tennis Club Management System Configuration");
                fos.close();
                
                System.out.println("Created default configuration file: " + configFile.getAbsolutePath());
            } catch (IOException e) {
                System.err.println("Error creating default configuration: " + e.getMessage());
            }
        }
    }
}