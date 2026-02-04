package parkinglotmanagementsystem.model;

public enum SpotType {
    COMPACT(2.0), // RM 2/hour - for motorcycles, bicycles
    REGULAR(5.0), // RM 5/hour - for regular cars
    HANDICAPPED(2.0), // RM 2/hour - reserved for handicapped (FREE if handicapped vehicle)
    RESERVED(10.0); // RM 10/hour - for VIP customers

    private final double hourlyRate;

    SpotType(double hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public double getHourlyRate() {
        return hourlyRate;
    }
}
