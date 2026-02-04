package parkinglotmanagementsystem.service;

import parkinglotmanagementsystem.model.*;
import parkinglotmanagementsystem.util.TimeUtil;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Billing Service
 * Handles parking fee calculation and bill generation
 */
public class BillingService {
    
    private FineManager fineManager;
    
    public BillingService(FineManager fineManager) {
        this.fineManager = fineManager;
    }
    
    /**
     * Calculates parking fee based on vehicle, spot, and duration
     */
    public double calculateParkingFee(Vehicle vehicle, ParkingSpot spot, long hoursParked) {
        if (vehicle == null || spot == null) {
            return 0.0;
        }
        
        // Get hourly rate based on vehicle type and spot type
        double hourlyRate = vehicle.getParkingRate(spot);
        
        // Calculate total fee
        return hoursParked * hourlyRate;
    }
    
    /**
     * Generates complete bill for a ticket
     * Includes parking fee, new fines, and unpaid fines
     */
    public Map<String, Object> generateBill(Ticket ticket, Vehicle vehicle, 
                                           ParkingSpot spot, LocalDateTime exitTime) {
        Map<String, Object> bill = new HashMap<>();
        
        // Calculate duration
        long hoursParked = TimeUtil.calculateDurationHours(ticket.getEntryTime(), exitTime);
        
        // Calculate parking fee
        double parkingFee = calculateParkingFee(vehicle, spot, hoursParked);
        
        // Detect and generate new fines for this session
        List<Fine> newFines = fineManager.detectAndGenerateFines(ticket, spot, hoursParked);
        double newFineAmount = newFines.stream()
            .mapToDouble(Fine::getFineAmount)
            .sum();
        
        // Get existing unpaid fines
        List<Fine> unpaidFines = fineManager.getUnpaidFines(ticket.getPlateNumber());
        double existingFineAmount = unpaidFines.stream()
            .mapToDouble(Fine::getFineAmount)
            .sum();
        
        // Calculate total
        double totalFineAmount = newFineAmount + existingFineAmount;
        double totalDue = parkingFee + totalFineAmount;
        
        // Populate bill details
        bill.put("ticket", ticket);
        bill.put("vehicle", vehicle);
        bill.put("spot", spot);
        bill.put("entryTime", ticket.getEntryTime());
        bill.put("exitTime", exitTime);
        bill.put("hoursParked", hoursParked);
        bill.put("hourlyRate", vehicle.getParkingRate(spot));
        bill.put("parkingFee", parkingFee);
        bill.put("newFines", newFines);
        bill.put("newFineAmount", newFineAmount);
        bill.put("unpaidFines", unpaidFines);
        bill.put("existingFineAmount", existingFineAmount);
        bill.put("totalFineAmount", totalFineAmount);
        bill.put("totalDue", totalDue);
        bill.put("fineScheme", ticket.getFineScheme());
        
        return bill;
    }
    
    /**
     * Formats bill as a string
     */
    public String formatBill(Map<String, Object> bill) {
        if (bill.containsKey("error")) {
            return "ERROR: " + bill.get("error");
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("=".repeat(50)).append("\n");
        sb.append("PARKING BILL\n");
        sb.append("=".repeat(50)).append("\n");
        
        Ticket ticket = (Ticket) bill.get("ticket");
        Vehicle vehicle = (Vehicle) bill.get("vehicle");
        ParkingSpot spot = (ParkingSpot) bill.get("spot");
        
        sb.append(String.format("Ticket ID: %s%n", ticket.getTicketId()));
        sb.append(String.format("Plate Number: %s%n", vehicle.getPlateNumber()));
        sb.append(String.format("Vehicle Type: %s%n", vehicle.getVehicleType()));
        sb.append(String.format("Spot: %s (%s)%n", spot.getSpotId(), spot.getSpotType()));
        sb.append("\n");
        
        sb.append(String.format("Entry Time: %s%n", 
            TimeUtil.formatForDisplay((LocalDateTime) bill.get("entryTime"))));
        sb.append(String.format("Exit Time: %s%n", 
            TimeUtil.formatForDisplay((LocalDateTime) bill.get("exitTime"))));
        sb.append(String.format("Duration: %d hours%n", bill.get("hoursParked")));
        sb.append("\n");
        
        sb.append("-".repeat(50)).append("\n");
        sb.append(String.format("Hourly Rate: RM %.2f/hr%n", bill.get("hourlyRate")));
        sb.append(String.format("Parking Fee: RM %.2f%n", bill.get("parkingFee")));
        sb.append("\n");
        
        // New fines
        @SuppressWarnings("unchecked")
        List<Fine> newFines = (List<Fine>) bill.get("newFines");
        if (!newFines.isEmpty()) {
            sb.append("NEW FINES:%n");
            for (Fine fine : newFines) {
                sb.append(String.format("  - %s: RM %.2f (Scheme: %s)%n", 
                    fine.getFineType(), fine.getFineAmount(), fine.getFineScheme()));
            }
            sb.append(String.format("New Fine Total: RM %.2f%n", bill.get("newFineAmount")));
            sb.append("\n");
        }
        
        // Existing unpaid fines
        @SuppressWarnings("unchecked")
        List<Fine> unpaidFines = (List<Fine>) bill.get("unpaidFines");
        if (!unpaidFines.isEmpty()) {
            sb.append("UNPAID FINES FROM PREVIOUS VISITS:%n");
            for (Fine fine : unpaidFines) {
                sb.append(String.format("  - %s: RM %.2f (From: %s)%n",
                    fine.getFineType(), fine.getFineAmount(), 
                    TimeUtil.formatForDisplay(fine.getCreatedAt())));
            }
            sb.append(String.format("Existing Fine Total: RM %.2f%n", bill.get("existingFineAmount")));
            sb.append("\n");
        }
        
        sb.append("-".repeat(50)).append("\n");
        sb.append(String.format("TOTAL DUE: RM %.2f%n", bill.get("totalDue")));
        sb.append("-".repeat(50)).append("\n");
        sb.append(String.format("Fine Scheme (This Session): %s%n", bill.get("fineScheme")));
        sb.append("=".repeat(50)).append("\n");
        
        return sb.toString();
    }
}
