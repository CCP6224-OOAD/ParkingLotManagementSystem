package parkinglotmanagementsystem.util;

import java.util.regex.Pattern;

/**
 * Utility class for validating vehicle plate numbers
 * Format: 3 uppercase letters + 4 digits (e.g., ABC1234)
 */
public class PlateValidator {
    
    private static final Pattern PLATE_PATTERN = Pattern.compile(Constants.PLATE_PATTERN);
    
    /**
     * Validates a plate number format
     * @param plateNumber the plate number to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValid(String plateNumber) {
        if (plateNumber == null || plateNumber.trim().isEmpty()) {
            return false;
        }
        return PLATE_PATTERN.matcher(plateNumber.trim()).matches();
    }
    
    /**
     * Normalizes a plate number by trimming and converting to uppercase
     * @param plateNumber the plate number to normalize
     * @return normalized plate number
     */
    public static String normalize(String plateNumber) {
        if (plateNumber == null) {
            return null;
        }
        return plateNumber.trim().toUpperCase();
    }
    
    /**
     * Validates and normalizes a plate number
     * @param plateNumber the plate number to process
     * @return normalized plate number
     * @throws IllegalArgumentException if plate number is invalid
     */
    public static String validateAndNormalize(String plateNumber) {
        String normalized = normalize(plateNumber);
        if (!isValid(normalized)) {
            throw new IllegalArgumentException(
                "Invalid plate number format. Expected: 3 letters + 4 digits (e.g., ABC1234)"
            );
        }
        return normalized;
    }
    
    private PlateValidator() {
        // Prevent instantiation
    }
}
