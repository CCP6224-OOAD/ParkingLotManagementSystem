package parkinglotmanagementsystem.parking.model;

public class ParkingSpot {

    private String spotId; // e.g., "F1-R1-S1"
    private int floorNumber;
    private int rowNumber;
    private int spotNumber;
    private SpotType spotType;
    private double hourlyRate;
    private SpotStatus status;
    private String currentPlate; // null if available

    public ParkingSpot(int floorNumber, int rowNumber, int spotNumber, SpotType spotType) {
        this.floorNumber = floorNumber;
        this.rowNumber = rowNumber;
        this.spotNumber = spotNumber;
        this.spotType = spotType;
        this.hourlyRate = spotType.getHourlyRate();
        this.status = SpotStatus.AVAILABLE;
        this.currentPlate = null;
        this.spotId = generateSpotId(floorNumber, rowNumber, spotNumber);
    }

    public ParkingSpot(String spotId, int floorNumber, int rowNumber, int spotNumber,
            SpotType spotType, double hourlyRate, SpotStatus status, String currentPlate) {
        this.spotId = spotId;
        this.floorNumber = floorNumber;
        this.rowNumber = rowNumber;
        this.spotNumber = spotNumber;
        this.spotType = spotType;
        this.hourlyRate = hourlyRate;
        this.status = status;
        this.currentPlate = currentPlate;
    }

    private String generateSpotId(int floor, int row, int spot) {
        return String.format("F%d-R%d-S%d", floor, row, spot);
    }

    public boolean isAvailable() {
        return status == SpotStatus.AVAILABLE;
    }

    public void occupy(String plateNumber) {
        this.status = SpotStatus.OCCUPIED;
        this.currentPlate = plateNumber;
    }

    public void release() {
        this.status = SpotStatus.AVAILABLE;
        this.currentPlate = null;
    }

    // Getters and Setters
    public String getSpotId() {
        return spotId;
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public int getSpotNumber() {
        return spotNumber;
    }

    public SpotType getSpotType() {
        return spotType;
    }

    public void setSpotType(SpotType spotType) {
        this.spotType = spotType;
    }

    public double getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(double hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public SpotStatus getStatus() {
        return status;
    }

    public void setStatus(SpotStatus status) {
        this.status = status;
    }

    public String getCurrentPlate() {
        return currentPlate;
    }

    public void setCurrentPlate(String currentPlate) {
        this.currentPlate = currentPlate;
    }

    @Override
    public String toString() {
        return String.format("Spot[%s, Type=%s, Rate=RM%.2f/hr, Status=%s, Plate=%s]",
                spotId, spotType, hourlyRate, status, currentPlate != null ? currentPlate : "None");
    }
}
