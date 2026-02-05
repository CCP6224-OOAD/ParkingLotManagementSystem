package parkinglotmanagementsystem.model;

import java.time.LocalDateTime;

public class Reservation {

    private int reservationId;
    private String spotId;
    private String plateNumber; // null if not yet used
    private LocalDateTime reservedAt;
    private boolean isActive;

    public Reservation(String spotId, LocalDateTime reservedAt) {
        this.spotId = spotId;
        this.plateNumber = null; // Not yet assigned to a vehicle
        this.reservedAt = reservedAt;
        this.isActive = true;
    }

    public Reservation(int reservationId, String spotId, String plateNumber,
            LocalDateTime reservedAt, boolean isActive) {
        this.reservationId = reservationId;
        this.spotId = spotId;
        this.plateNumber = plateNumber;
        this.reservedAt = reservedAt;
        this.isActive = isActive;
    }

    public void assignToVehicle(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public void cancel() {
        this.isActive = false;
    }

    public void markAsUsed() {
        this.isActive = false;
    }

    public boolean isAvailable() {
        return isActive && plateNumber == null;
    }

    // Getters and Setters

    public int getReservationId() {
        return reservationId;
    }

    public void setReservationId(int reservationId) {
        this.reservationId = reservationId;
    }

    public String getSpotId() {
        return spotId;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public LocalDateTime getReservedAt() {
        return reservedAt;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public String toString() {
        return String.format("Reservation[ID=%d, Spot=%s, Plate=%s, Active=%s]",
                reservationId, spotId, plateNumber != null ? plateNumber : "Unassigned", isActive);
    }
}
