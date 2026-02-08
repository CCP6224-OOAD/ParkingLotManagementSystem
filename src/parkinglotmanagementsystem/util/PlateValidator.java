package parkinglotmanagementsystem.util;

import java.util.regex.Pattern;

public class PlateValidator {

    private static final Pattern PLATE_PATTERN = Pattern.compile(Constants.PLATE_PATTERN);

    public static boolean isValid(String plateNumber) {
        if (plateNumber == null || plateNumber.trim().isEmpty()) {
            return false;
        }
        return PLATE_PATTERN.matcher(plateNumber.trim()).matches();
    }

    public static String normalize(String plateNumber) {
        if (plateNumber == null) {
            return null;
        }
        return plateNumber.trim().toUpperCase();
    }

    public static String validateAndNormalize(String plateNumber) {
        String normalized = normalize(plateNumber);
        if (!isValid(normalized)) {
            throw new IllegalArgumentException(
                    "Invalid plate number format. Expected: 3 letters + 4 digits (e.g., ABC1234)");
        }
        return normalized;
    }

    private PlateValidator() {
        // Prevent instantiation
    }
}
