package Main;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

public class TimeParser {
    
    // Parse a time string from any reasonable format to a formatted display string
	//this method was needed after converting database from MYSQL to SQLite 
     
    public static String parseTimeToDisplay(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return "";
        }
        
        // Try all possible formats
        try {
            // Format 1: HH:MM:SS
            if (timeStr.matches("\\d{1,2}:\\d{2}(:\\d{2})?")) {
                String format = timeStr.contains(":") ? 
                    (timeStr.length() > 5 ? "HH:mm:ss" : "HH:mm") : "HHmm";
                LocalTime time = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern(format));
                return time.format(DateTimeFormatter.ofPattern("h:mm a"));
            }
            
            // Format 2: Unix timestamp
            try {
                long millis = Long.parseLong(timeStr);
                SimpleDateFormat displayFormat = new SimpleDateFormat("h:mm a");
                // Only use the time portion of the date
                Date date = new Date(millis);
                return displayFormat.format(date);
            } catch (NumberFormatException e) {
                
            }
            
            // Format 3: 12-hour format (e.g., "2:30 PM")
            if (timeStr.matches("\\d{1,2}:\\d{2}\\s*(AM|PM|am|pm)")) {
                SimpleDateFormat parser = new SimpleDateFormat("h:mm a");
                Date date = parser.parse(timeStr);
                return parser.format(date);
            }
            
            //  none of the formats matched - return original
            System.out.println("Time format not recognized: " + timeStr);
            return timeStr;
            
        } catch (Exception e) {
            System.out.println("Error parsing time '" + timeStr + "': " + e.getMessage());
            return timeStr; // Return original on any error
        }
    }
    
    // Parse a time string to get just the hour component
    
    public static int parseHour(String timeStr) {
        try {
            // Try direct hour extraction for HH:MM:SS format
            if (timeStr.matches("\\d{1,2}:\\d{2}(:\\d{2})?")) {
                return Integer.parseInt(timeStr.split(":")[0]);
            }
            
            // Try parsing as Unix timestamp
            try {
                long millis = Long.parseLong(timeStr);
                SimpleDateFormat hourFormat = new SimpleDateFormat("H"); // 24-hour format
                return Integer.parseInt(hourFormat.format(new Date(millis)));
            } catch (NumberFormatException e) {
                // Not a number, try other formats
            }
            
            // Try 12-hour format
            if (timeStr.matches("\\d{1,2}:\\d{2}\\s*(AM|PM|am|pm)")) {
                SimpleDateFormat parser = new SimpleDateFormat("h:mm a");
                SimpleDateFormat hourFormat = new SimpleDateFormat("H");
                return Integer.parseInt(hourFormat.format(parser.parse(timeStr)));
            }
            
            // Default
            return 0;
        } catch (Exception e) {
            System.out.println("Error parsing hour from '" + timeStr + "': " + e.getMessage());
            return 0;
        }
    }
    
    // Parse a time string to get just the minutes component
     
    public static int parseMinutes(String timeStr) {
        try {
            // Try direct minutes extraction for HH:MM:SS format
            if (timeStr.matches("\\d{1,2}:\\d{2}(:\\d{2})?")) {
                return Integer.parseInt(timeStr.split(":")[1]);
            }
            
            // Try parsing as Unix timestamp
            try {
                long millis = Long.parseLong(timeStr);
                SimpleDateFormat minuteFormat = new SimpleDateFormat("m");
                return Integer.parseInt(minuteFormat.format(new Date(millis)));
            } catch (NumberFormatException e) {
                // Not a number, try other formats
            }
            
            // Try 12-hour format
            if (timeStr.matches("\\d{1,2}:\\d{2}\\s*(AM|PM|am|pm)")) {
                SimpleDateFormat parser = new SimpleDateFormat("h:mm a");
                SimpleDateFormat minuteFormat = new SimpleDateFormat("m");
                return Integer.parseInt(minuteFormat.format(parser.parse(timeStr)));
            }
            
            // Default
            return 0;
        } catch (Exception e) {
            System.out.println("Error parsing minutes from '" + timeStr + "': " + e.getMessage());
            return 0;
        }
    }
    
    // Standardize a time string to HH:MM:SS format
     
    public static String standardizeTimeFormat(String timeStr) {
        try {
            int hour = parseHour(timeStr);
            int minute = parseMinutes(timeStr);
            return String.format("%02d:%02d:00", hour, minute);
        } catch (Exception e) {
            System.out.println("Error standardizing time '" + timeStr + "': " + e.getMessage());
            return "00:00:00"; // Default on error
        }
    }
}