package parkinglotmanagementsystem.service;

import parkinglotmanagementsystem.dao.ParkingSpotDAO;
import parkinglotmanagementsystem.model.*;
import parkinglotmanagementsystem.util.Constants;

import java.util.List;

public class ParkingService {

    private ParkingSpotDAO spotDAO;
    private ParkingLot parkingLot;

    public ParkingService() {
        this.spotDAO = new ParkingSpotDAO();
        this.parkingLot = new ParkingLot(Constants.TOTAL_FLOORS);
        loadParkingLot();
    }

    public void initializeParkingLot() {
        // Check if parking lot is already initialized
        if (spotDAO.getTotalCount() > 0) {
            System.out.println("Parking lot already initialized. Skipping...");
            return;
        }

        System.out.println("Initializing parking lot structure...");

        int spotsCreated = 0;

        for (int floor = 1; floor <= Constants.TOTAL_FLOORS; floor++) {
            for (int row = 1; row <= Constants.ROWS_PER_FLOOR; row++) {
                int spotNum = 1;

                // Create COMPACT spots
                for (int i = 0; i < Constants.COMPACT_SPOTS_PER_ROW; i++) {
                    ParkingSpot spot = new ParkingSpot(floor, row, spotNum++, SpotType.COMPACT);
                    if (spotDAO.insertSpot(spot)) {
                        spotsCreated++;
                    }
                }

                // Create REGULAR spots
                for (int i = 0; i < Constants.REGULAR_SPOTS_PER_ROW; i++) {
                    ParkingSpot spot = new ParkingSpot(floor, row, spotNum++, SpotType.REGULAR);
                    if (spotDAO.insertSpot(spot)) {
                        spotsCreated++;
                    }
                }

                // Create HANDICAPPED spots
                for (int i = 0; i < Constants.HANDICAPPED_SPOTS_PER_ROW; i++) {
                    ParkingSpot spot = new ParkingSpot(floor, row, spotNum++, SpotType.HANDICAPPED);
                    if (spotDAO.insertSpot(spot)) {
                        spotsCreated++;
                    }
                }

                // Create RESERVED spots
                for (int i = 0; i < Constants.RESERVED_SPOTS_PER_ROW; i++) {
                    ParkingSpot spot = new ParkingSpot(floor, row, spotNum++, SpotType.RESERVED);
                    if (spotDAO.insertSpot(spot)) {
                        spotsCreated++;
                    }
                }
            }
        }

        System.out.println("Parking lot initialized: " + spotsCreated + " spots created.");
        loadParkingLot();
    }

    private void loadParkingLot() {
        List<ParkingSpot> allSpots = spotDAO.getAllSpots();
        for (ParkingSpot spot : allSpots) {
            parkingLot.addSpot(spot);
        }
        System.out.println("Loaded " + allSpots.size() + " parking spots from database.");
    }

    public List<ParkingSpot> getSuitableSpots(VehicleType vehicleType) {
        return parkingLot.findAvailableSpots(vehicleType);
    }

    public List<ParkingSpot> getAvailableSpotsByType(SpotType spotType) {
        return spotDAO.findAvailableSpots(spotType);
    }

    public boolean allocateSpot(String spotId, String plateNumber) {
        ParkingSpot spot = spotDAO.findSpotById(spotId);

        if (spot == null) {
            System.err.println("Spot not found: " + spotId);
            return false;
        }

        if (!spot.isAvailable()) {
            System.err.println("Spot is already occupied: " + spotId);
            return false;
        }

        // Update spot status in database
        boolean updated = spotDAO.updateSpotStatus(spotId, SpotStatus.OCCUPIED, plateNumber);

        if (updated) {
            // Update in-memory object
            spot.occupy(plateNumber);
            System.out.println("Spot allocated: " + spotId + " to " + plateNumber);
        }

        return updated;
    }

    public boolean releaseSpot(String spotId) {
        ParkingSpot spot = spotDAO.findSpotById(spotId);

        if (spot == null) {
            System.err.println("Spot not found: " + spotId);
            return false;
        }

        // Update spot status in database
        boolean updated = spotDAO.updateSpotStatus(spotId, SpotStatus.AVAILABLE, null);

        if (updated) {
            // Update in-memory object
            spot.release();
            System.out.println("Spot released: " + spotId);
        }

        return updated;
    }

    public ParkingSpot getSpotById(String spotId) {
        return spotDAO.findSpotById(spotId);
    }

    public List<ParkingSpot> getSpotsByFloor(int floorNumber) {
        return spotDAO.getSpotsByFloor(floorNumber);
    }

    public List<ParkingSpot> getAllSpots() {
        return spotDAO.getAllSpots();
    }

    public ParkingLot getParkingLot() {
        // Reload to ensure fresh data
        parkingLot = new ParkingLot(Constants.TOTAL_FLOORS);
        loadParkingLot();
        return parkingLot;
    }

    public String getOccupancyReport() {
        ParkingLot lot = getParkingLot();
        StringBuilder report = new StringBuilder();

        report.append("=== OCCUPANCY REPORT ===\n");
        report.append(String.format("Total Occupancy: %.1f%% (%d/%d spots)\n\n",
                lot.getGlobalOccupancyRate(), lot.getTotalOccupied(), lot.getTotalSpots()));

        for (Floor floor : lot.getAllFloors()) {
            report.append(String.format("Floor %d: %.1f%% (%d/%d spots)\n",
                    floor.getFloorNumber(), floor.getOccupancyRate(),
                    floor.getOccupiedCount(), floor.getTotalSpots()));
        }

        return report.toString();
    }
}
