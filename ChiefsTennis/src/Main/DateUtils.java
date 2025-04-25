package Main;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
    
    /**
     * Helper method to format SQLite date strings safely
     */
    public static String formatSqliteDate(String sqliteDate) {
        if (sqliteDate == null || sqliteDate.isEmpty()) {
            return "";
        }
        
        try {
            // Try to parse as ISO format (YYYY-MM-DD)
            if (sqliteDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
                String[] parts = sqliteDate.split("-");
                return parts[1] + "/" + parts[2] + "/" + parts[0];
            }
            
            // If it's a timestamp, try to extract the date part
            if (sqliteDate.matches("\\d{4}-\\d{2}-\\d{2}.*")) {
                String datePart = sqliteDate.substring(0, 10);
                String[] parts = datePart.split("-");
                return parts[1] + "/" + parts[2] + "/" + parts[0];
            }
            
            // For numeric timestamps
            try {
                long timestamp = Long.parseLong(sqliteDate);
                SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
                return dateFormat.format(new Date(timestamp));
            } catch (NumberFormatException e) {
                // Not a numeric timestamp
            }
            
            // If all else fails, return as-is
            return sqliteDate;
        } catch (Exception e) {
            System.out.println("Error formatting date: " + sqliteDate);
            return sqliteDate;
        }
    }
    
    /**
     * Add days to a date and return as SQLite-compatible date string
     */
    public static String addDaysForSQLite(Date date, int days) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(date);
        cal.add(java.util.Calendar.DAY_OF_MONTH, days);
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(cal.getTime());
    }
    
    /**
     * Parse a SQLite date string to a Java Date object
     */
    public static Date parseSQLiteDate(String sqliteDate) throws ParseException {
        if (sqliteDate == null || sqliteDate.isEmpty()) {
            return null;
        }
        
        // Try standard ISO format (YYYY-MM-DD)
        if (sqliteDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            return sdf.parse(sqliteDate);
        }
        
        // Try timestamp format
        try {
            long timestamp = Long.parseLong(sqliteDate);
            return new Date(timestamp);
        } catch (NumberFormatException e) {
            // Not a numeric timestamp
        }
        
        // Try other common formats
        String[] formats = {
            "MM/dd/yyyy",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss"
        };
        
        for (String format : formats) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format);
                return sdf.parse(sqliteDate);
            } catch (ParseException e) {
                // Try next format
            }
        }
        
        throw new ParseException("Unable to parse date: " + sqliteDate, 0);
    }
}