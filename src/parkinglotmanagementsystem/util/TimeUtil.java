package parkinglotmanagementsystem.util;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for time calculations and formatting
 */
public class TimeUtil {
    
    private static final DateTimeFormatter DISPLAY_FORMATTER = 
        DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    
    private static final DateTimeFormatter ISO_FORMATTER = 
        DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    /**
     * Calculates parking duration in hours, rounded up to nearest hour
     * @param entryTime the entry time
     * @param exitTime the exit time
     * @return duration in hours (ceiling rounded)
     */
    public static long calculateDurationHours(LocalDateTime entryTime, LocalDateTime exitTime) {
        if (entryTime == null || exitTime == null) {
            throw new IllegalArgumentException("Entry and exit times cannot be null");
        }
        
        if (exitTime.isBefore(entryTime)) {
            throw new IllegalArgumentException("Exit time cannot be before entry time");
        }
        
        Duration duration = Duration.between(entryTime, exitTime);
        long seconds = duration.getSeconds();
        
        // Ceiling division: (seconds + 3599) / 3600
        // This ensures any partial hour is counted as a full hour
        return (seconds + 3599) / 3600;
    }
    
    /**
     * Formats a LocalDateTime for display
     * @param dateTime the datetime to format
     * @return formatted string (dd-MM-yyyy HH:mm:ss)
     */
    public static String formatForDisplay(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "N/A";
        }
        return dateTime.format(DISPLAY_FORMATTER);
    }
    
    /**
     * Formats a LocalDateTime for database storage (ISO format)
     * @param dateTime the datetime to format
     * @return ISO formatted string
     */
    public static String formatForDatabase(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(ISO_FORMATTER);
    }
    
    /**
     * Parses a datetime string from database (ISO format)
     * @param dateTimeString the ISO formatted string
     * @return LocalDateTime object
     */
    public static LocalDateTime parseFromDatabase(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.trim().isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(dateTimeString, ISO_FORMATTER);
    }
    
    /**
     * Gets the current timestamp
     * @return current LocalDateTime
     */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }
    
    /**
     * Gets current timestamp in milliseconds (for ticket ID generation)
     * @return current time in milliseconds
     */
    public static long nowMillis() {
        return System.currentTimeMillis();
    }
    
    private TimeUtil() {
        // Prevent instantiation
    }
}
