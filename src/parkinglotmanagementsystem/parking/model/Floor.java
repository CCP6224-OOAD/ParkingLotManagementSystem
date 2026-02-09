package parkinglotmanagementsystem.parking.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Floor {

    private int floorNumber;
    private List<ParkingSpot> spots;

    public Floor(int floorNumber) {
        this.floorNumber = floorNumber;
        this.spots = new ArrayList<>();
    }

    public void addSpot(ParkingSpot spot) {
        if (spot.getFloorNumber() == this.floorNumber) {
            this.spots.add(spot);
        } else {
            throw new IllegalArgumentException(
                    "Spot floor number does not match this floor: " + spot.getSpotId());
        }
    }

    public List<ParkingSpot> getAvailableSpots(SpotType spotType) {
        return spots.stream()
                .filter(spot -> spot.isAvailable() && spot.getSpotType() == spotType)
                .collect(Collectors.toList());
    }

    public List<ParkingSpot> getAllAvailableSpots() {
        return spots.stream()
                .filter(ParkingSpot::isAvailable)
                .collect(Collectors.toList());
    }

    public double getOccupancyRate() {
        if (spots.isEmpty()) {
            return 0.0;
        }

        long occupiedCount = spots.stream()
                .filter(spot -> !spot.isAvailable())
                .count();

        return (occupiedCount * 100.0) / spots.size();
    }

    public int getOccupiedCount() {
        return (int) spots.stream()
                .filter(spot -> !spot.isAvailable())
                .count();
    }

    public int getTotalSpots() {
        return spots.size();
    }

    // Getters and setters

    public int getFloorNumber() {
        return floorNumber;
    }

    public List<ParkingSpot> getSpots() {
        return new ArrayList<>(spots);
    }

    @Override
    public String toString() {
        return String.format("Floor %d: %d/%d spots occupied (%.1f%%)",
                floorNumber, getOccupiedCount(), getTotalSpots(), getOccupancyRate());
    }
}
