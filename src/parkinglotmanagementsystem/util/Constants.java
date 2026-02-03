package parkinglotmanagementsystem.util;

/**
 * Application-wide constants
 */
public class Constants {
    
    // Database Configuration
    public static final String DB_FILE = "parking_lot.db";
    public static final String DB_URL = "jdbc:sqlite:" + DB_FILE;
    
    // Parking Lot Configuration
    public static final int TOTAL_FLOORS = 5;
    public static final int ROWS_PER_FLOOR = 4;
    public static final int SPOTS_PER_ROW = 10;
    
    // Spot Distribution per Row (total 10 spots per row)
    public static final int COMPACT_SPOTS_PER_ROW = 3;
    public static final int REGULAR_SPOTS_PER_ROW = 5;
    public static final int HANDICAPPED_SPOTS_PER_ROW = 1;
    public static final int RESERVED_SPOTS_PER_ROW = 1;
    
    // Fine Configuration
    public static final long OVERSTAY_THRESHOLD_HOURS = 24;
    public static final double FIXED_FINE_AMOUNT = 50.0;
    public static final double HOURLY_FINE_RATE = 20.0;
    
    // Progressive Fine Tiers
    public static final double PROGRESSIVE_TIER_1 = 50.0;   // 24-48 hours
    public static final double PROGRESSIVE_TIER_2 = 150.0;  // 48-72 hours (50 + 100)
    public static final double PROGRESSIVE_TIER_3 = 300.0;  // 72-96 hours (50 + 100 + 150)
    public static final double PROGRESSIVE_TIER_4 = 500.0;  // 96+ hours (50 + 100 + 150 + 200)
    
    // Plate Number Validation
    public static final String PLATE_PATTERN = "^[A-Z]{3}\\d{4}$";
    
    // Ticket ID Format
    public static final String TICKET_PREFIX = "T-";
    
    // Default Fine Scheme
    public static final String DEFAULT_FINE_SCHEME = "FIXED";
    
    private Constants() {
        // Prevent instantiation
    }
}
