package Main;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;

/**
 * class for handling date and time conversions for SQLite
 * since SQLite doesn't have native DATE, TIME, or DATETIME types,
 * this helps with formatting.
 */
public class DateUtils {
    
    // Date format for SQLite storage: YYYY-MM-DD
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    
    // Time format for SQLite storage: HH:MM:SS
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
    
    // Datetime format for SQLite storage: YYYY-MM-DD HH:MM:SS
    private static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    // Display formats (for UI)
    private static final SimpleDateFormat DISPLAY_DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy");
    private static final SimpleDateFormat DISPLAY_TIME_FORMAT = new SimpleDateFormat("h:mm a");
    private static final SimpleDateFormat DISPLAY_DATETIME_FORMAT = new SimpleDateFormat("MM/dd/yyyy h:mm a");
    
    // Convert a java.util.Date to SQLite date string
  
    public static String formatDateForSQLite(Date date) {
        if (date == null) return null;
        return DATE_FORMAT.format(date);
    }
    
    //Convert a java.util.Date to SQLite time string
    
    public static String formatTimeForSQLite(Date date) {
        if (date == null) return null;
        return TIME_FORMAT.format(date);
    }
    
    // Convert a java.util.Date to SQLite datetime string
    
    public static String formatDateTimeForSQLite(Date date) {
        if (date == null) return null;
        return DATETIME_FORMAT.format(date);
    }
    
    //
    public static Date parseSQLiteDate(String sqliteDate) throws ParseException {
        if (sqliteDate == null || sqliteDate.isEmpty()) return null;
        return DATE_FORMAT.parse(sqliteDate);
    }
    
    // Parse a SQLite time string to java.util.Date (with today's date)
    
    public static Date parseSQLiteTime(String sqliteTime) throws ParseException {
        if (sqliteTime == null || sqliteTime.isEmpty()) return null;
        
        // Parse the time components
        Date timeOnly = TIME_FORMAT.parse(sqliteTime);
        
        // Add the time to today's date
        Calendar cal = Calendar.getInstance();
        Calendar timeCal = Calendar.getInstance();
        timeCal.setTime(timeOnly);
        
        cal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
        cal.set(Calendar.SECOND, timeCal.get(Calendar.SECOND));
        cal.set(Calendar.MILLISECOND, 0);
        
        return cal.getTime();
    }
    
    // Parse a SQLite datetime string to java.util.Date
    
    public static Date parseSQLiteDateTime(String sqliteDateTime) throws ParseException {
        if (sqliteDateTime == null || sqliteDateTime.isEmpty()) return null;
        return DATETIME_FORMAT.parse(sqliteDateTime);
    }
    
    // Format a date for display in the UI
    
    public static String formatDateForDisplay(Date date) {
        if (date == null) return "";
        return DISPLAY_DATE_FORMAT.format(date);
    }
    
    // Format a time for display in the UI
     
    public static String formatTimeForDisplay(Date date) {
        if (date == null) return "";
        return DISPLAY_TIME_FORMAT.format(date);
    }
    
    //Format a datetime for display in the UI
    
    public static String formatDateTimeForDisplay(Date date) {
        if (date == null) return "";
        return DISPLAY_DATETIME_FORMAT.format(date);
    }
    
    // Get the current date as a SQLite date string
     
    public static String getCurrentDateForSQLite() {
        return formatDateForSQLite(new Date());
    }
    
    /// Get the current time as a SQLite time string
     
    public static String getCurrentTimeForSQLite() {
        return formatTimeForSQLite(new Date());
    }
    
    // Get the current datetime as a SQLite datetime string
    
    public static String getCurrentDateTimeForSQLite() {
        return formatDateTimeForSQLite(new Date());
    }
    
    //Add days to a date and return the result as a SQLite date string
    
    public static String addDaysForSQLite(Date date, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_MONTH, days);
        return formatDateForSQLite(cal.getTime());
    }
    
    
     //Add months to a date and return the result as a SQLite date string
     
    public static String addMonthsForSQLite(Date date, int months) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MONTH, months);
        return formatDateForSQLite(cal.getTime());
    }
    
    // Check if a string is a valid SQLite date
    
    public static boolean isValidSQLiteDate(String dateStr) {
        try {
            if (dateStr == null || dateStr.isEmpty()) return false;
            DATE_FORMAT.parse(dateStr);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }
    
    
     // Calculate the difference in days between two SQLite date strings
   
    public static int daysBetween(String date1, String date2) throws ParseException {
        Date d1 = parseSQLiteDate(date1);
        Date d2 = parseSQLiteDate(date2);
        
        long diffMillis = d2.getTime() - d1.getTime();
        return (int) (diffMillis / (1000 * 60 * 60 * 24));
    }
}