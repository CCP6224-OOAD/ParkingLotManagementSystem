package parkinglotmanagementsystem.model;

public enum FineScheme {
    FIXED, // Flat RM 50 fine for overstaying
    PROGRESSIVE, // Tiered fines based on duration (24-48h: RM50, 48-72h: RM150, etc.)
    HOURLY // RM 20 per hour for overstaying
}
