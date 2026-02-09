package parkinglotmanagementsystem.parking.model;

import java.util.ArrayList;
import java.util.List;

import parkinglotmanagementsystem.vehicleandticket.model.VehicleType;

public class ParkingLot {

    private int totalFloors;
    private List<Floor> floors;

    public ParkingLot(int totalFloors) {
        this.totalFloors = totalFloors;
        this.floors = new ArrayList<>();

        // Initialize floors
        for (int i = 1; i <= totalFloors; i++) {
            floors.add(new Floor(i));
        }
    }

    public void addSpot(ParkingSpot spot) {
        int floorIndex = spot.getFloorNumber() - 1;
        if (floorIndex >= 0 && floorIndex < floors.size()) {
            floors.get(floorIndex).addSpot(spot);
        } else {
            throw new IllegalArgumentException(
                    "Invalid floor number: " + spot.getFloorNumber());
        }
    }

    public List<ParkingSpot> findAvailableSpots(VehicleType vehicleType) {
        List<ParkingSpot> availableSpots = new ArrayList<>();

        for (Floor floor : floors) {
            for (ParkingSpot spot : floor.getAllAvailableSpots()) {
                // Check if vehicle can park in this spot type
                if (canVehicleParkInSpot(vehicleType, spot.getSpotType())) {
                    availableSpots.add(spot);
                }
            }
        }

        return availableSpots;
    }

    public List<ParkingSpot> findAllSpots() {
        List<ParkingSpot> spots = new ArrayList<>();
        for (Floor floor : floors) {
            for (ParkingSpot spot : floor.getSpots()) {
                spots.add(spot);
            }
        }

        return spots;
    }

    private boolean canVehicleParkInSpot(VehicleType vehicleType, SpotType spotType) {
        switch (vehicleType) {
            case MOTORCYCLE:
                return spotType == SpotType.RESERVED || spotType == SpotType.COMPACT;
            case CAR:
                return spotType == SpotType.RESERVED || spotType == SpotType.COMPACT || spotType == SpotType.REGULAR;
            case SUV:
                return spotType == SpotType.RESERVED || spotType == SpotType.REGULAR;
            case HANDICAPPED:
                return true; // Can park in any spot
            default:
                return false;
        }
    }

    public Floor getFloor(int floorNumber) {
        if (floorNumber >= 1 && floorNumber <= totalFloors) {
            return floors.get(floorNumber - 1);
        }
        return null;
    }

    public double getGlobalOccupancyRate() {
        int totalSpots = 0;
        int occupiedSpots = 0;

        for (Floor floor : floors) {
            totalSpots += floor.getTotalSpots();
            occupiedSpots += floor.getOccupiedCount();
        }

        if (totalSpots == 0) {
            return 0.0;
        }

        return (occupiedSpots * 100.0) / totalSpots;
    }

    public int getTotalSpots() {
        return floors.stream()
                .mapToInt(Floor::getTotalSpots)
                .sum();
    }

    public int getTotalOccupied() {
        return floors.stream()
                .mapToInt(Floor::getOccupiedCount)
                .sum();
    }

    // Getters ans Setters

    public int getTotalFloors() {
        return totalFloors;
    }

    public List<Floor> getAllFloors() {
        return new ArrayList<>(floors);
    }

    @Override
    public String toString() {
        return String.format("Parking Lot: %d floors, %d/%d spots occupied (%.1f%%)",
                totalFloors, getTotalOccupied(), getTotalSpots(), getGlobalOccupancyRate());
    }
}
