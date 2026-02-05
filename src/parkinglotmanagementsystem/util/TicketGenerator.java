package parkinglotmanagementsystem.util;

/**
 * Utility class for generating unique ticket IDs
 * Format: T-PLATE-TIMESTAMP
 * Example: T-ABC1234-1706789123456
 */
public class TicketGenerator {
    
    /**
     * Generates a unique ticket ID
     * @param plateNumber the vehicle's plate number
     * @return ticket ID in format T-PLATE-TIMESTAMP
     */
    public static String generateTicketId(String plateNumber) {
        if (plateNumber == null || plateNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Plate number cannot be null or empty");
        }
        
        String normalizedPlate = PlateValidator.validateAndNormalize(plateNumber);
        long timestamp = TimeUtil.nowMillis();
        
        return Constants.TICKET_PREFIX + normalizedPlate + "-" + timestamp;
    }
    
    private TicketGenerator() {
        // Prevent instantiation
    }
}
