package parkinglotmanagementsystem.util;

public class TicketGenerator {

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
