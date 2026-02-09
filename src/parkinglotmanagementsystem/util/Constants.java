package parkinglotmanagementsystem.util;

public class Constants {

    // database configuration
    public static final String DB_FILE = "parking_lot.db";
    public static final String DB_URL = "jdbc:sqlite:" + DB_FILE;

    // parking lot configuration
    public static final int TOTAL_FLOORS = 5;
    public static final int ROWS_PER_FLOOR = 4;
    public static final int SPOTS_PER_ROW = 10;

    // spot distribution per pow (total 10 spots per row)
    public static final int COMPACT_SPOTS_PER_ROW = 3;
    public static final int REGULAR_SPOTS_PER_ROW = 5;
    public static final int HANDICAPPED_SPOTS_PER_ROW = 1;
    public static final int RESERVED_SPOTS_PER_ROW = 1;

    // fine configuration
    public static final long OVERSTAY_THRESHOLD_HOURS = 1;
    public static final double FIXED_FINE_AMOUNT = 50.0;
    public static final double HOURLY_FINE_RATE = 20.0;

    // progressive fine tiers
    public static final double PROGRESSIVE_TIER_1 = 50.0; // 24-48 hours
    public static final double PROGRESSIVE_TIER_2 = 150.0; // 48-72 hours (50 + 100)
    public static final double PROGRESSIVE_TIER_3 = 300.0; // 72-96 hours (50 + 100 + 150)
    public static final double PROGRESSIVE_TIER_4 = 500.0; // 96+ hours (50 + 100 + 150 + 200)

    // plate number validation
    public static final String PLATE_PATTERN = "^[A-Z]{3}\\d{4}$";

    // ticket ID format
    public static final String TICKET_PREFIX = "T-";

    // default fine scheme
    public static final String DEFAULT_FINE_SCHEME = "FIXED";

    private Constants() {
        // prevent instantiation
    }
}
