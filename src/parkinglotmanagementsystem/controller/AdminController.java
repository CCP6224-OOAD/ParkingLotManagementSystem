package parkinglotmanagementsystem.controller;

import parkinglotmanagementsystem.dao.SystemConfigDAO;
import parkinglotmanagementsystem.model.*;
import parkinglotmanagementsystem.service.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminController {

    private SystemConfigDAO configDAO;
    private FineManager fineManager;
    private ParkingService parkingService;
    private TicketService ticketService;
    private PaymentService paymentService;

    public AdminController(ParkingService parkingService, FineManager fineManager, PaymentService paymentService) {
        this.configDAO = new SystemConfigDAO();
        this.fineManager = fineManager;
        this.parkingService = parkingService;
        this.ticketService = new TicketService();
        this.paymentService = paymentService;
    }

    public boolean changeFineScheme(FineScheme newScheme) {
        // Update system configuration
        boolean configUpdated = configDAO.setFineScheme(newScheme);

        if (!configUpdated) {
            System.err.println("Failed to update fine scheme in database");
            return false;
        }

        // update FineManager strategy
        fineManager.setFineStrategy(newScheme);

        System.out.println("=== FINE SCHEME CHANGED ===");
        System.out.println("New Scheme: " + newScheme);
        System.out.println("Effective: FUTURE ENTRIES ONLY");
        System.out.println("Existing parked vehicles retain their original scheme");

        return true;
    }

    public FineScheme getCurrentFineScheme() {
        return configDAO.getCurrentFineScheme();
    }

    public Map<String, Object> getOccupancyStats() {
        Map<String, Object> stats = new HashMap<>();

        ParkingLot parkingLot = parkingService.getParkingLot();

        stats.put("totalSpots", parkingLot.getTotalSpots());
        stats.put("occupiedSpots", parkingLot.getTotalOccupied());
        stats.put("availableSpots", parkingLot.getTotalSpots() - parkingLot.getTotalOccupied());
        stats.put("globalOccupancyRate", parkingLot.getGlobalOccupancyRate());

        // optional: floor-by-floor breakdown
        Map<Integer, Map<String, Object>> floorStats = new HashMap<>();
        for (Floor floor : parkingLot.getAllFloors()) {
            Map<String, Object> floorData = new HashMap<>();
            floorData.put("totalSpots", floor.getTotalSpots());
            floorData.put("occupiedSpots", floor.getOccupiedCount());
            floorData.put("availableSpots", floor.getTotalSpots() - floor.getOccupiedCount());
            floorData.put("occupancyRate", floor.getOccupancyRate());
            floorStats.put(floor.getFloorNumber(), floorData);
        }
        stats.put("floorStats", floorStats);

        return stats;
    }

    public Map<String, Object> getRevenueStats() {
        Map<String, Object> stats = new HashMap<>();

        double totalRevenue = paymentService.getTotalRevenue();
        double parkingRevenue = paymentService.getTotalParkingRevenue();
        double fineRevenue = paymentService.getTotalFineRevenue();
        int paymentCount = paymentService.getPaymentCount();

        stats.put("totalRevenue", totalRevenue);
        stats.put("parkingRevenue", parkingRevenue);
        stats.put("fineRevenue", fineRevenue);
        stats.put("paymentCount", paymentCount);
        stats.put("averageTransaction", paymentCount > 0 ? totalRevenue / paymentCount : 0.0);

        return stats;
    }

    public Map<String, Object> getFineStats() {
        Map<String, Object> stats = new HashMap<>();

        List<Fine> unpaidFines = fineManager.getAllUnpaidFines();
        double totalUnpaid = unpaidFines.stream()
                .mapToDouble(Fine::getFineAmount)
                .sum();

        stats.put("unpaidFineCount", unpaidFines.size());
        stats.put("totalUnpaidAmount", totalUnpaid);
        stats.put("unpaidFines", unpaidFines);

        return stats;
    }

    public List<Ticket> getCurrentlyParkedVehicles() {
        return ticketService.getAllParkedVehicles();
    }

    public List<Fine> getAllUnpaidFines() {
        return fineManager.getAllUnpaidFines();
    }

    public Map<String, Object> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();

        // Occupancy
        stats.putAll(getOccupancyStats());

        // Revenue
        Map<String, Object> revenueStats = getRevenueStats();
        stats.put("revenue", revenueStats);

        // Fines
        Map<String, Object> fineStats = getFineStats();
        stats.put("fines", fineStats);

        // Current state
        stats.put("currentlyParked", ticketService.getParkedVehicleCount());
        stats.put("totalTicketsIssued", ticketService.getTotalTicketCount());
        // stats.put("activeReservations", reservationService.getActiveReservationCount());
        stats.put("currentFineScheme", getCurrentFineScheme());

        return stats;
    }

    public ParkingLot getParkingLot() {
        return parkingService.getParkingLot();
    }

    public boolean updateSpotType(String spotId, SpotType spotType) {
        return parkingService.updateSpotType(spotId, spotType);
    }
}
