package parkinglotmanagementsystem.service;

import parkinglotmanagementsystem.dao.FineDAO;
import parkinglotmanagementsystem.model.*;
import parkinglotmanagementsystem.observer.ParkingEventListener;
import parkinglotmanagementsystem.observer.ParkingEventType;
import parkinglotmanagementsystem.strategy.*;
import parkinglotmanagementsystem.util.Constants;
import parkinglotmanagementsystem.util.TimeUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fine Manager Service with Observer Pattern
 * Manages fine detection, calculation, and notification
 */
public class FineManager {
    
    private FineDAO fineDAO;
    private FineCalculationStrategy currentStrategy;
    private Map<FineScheme, FineCalculationStrategy> strategies;
    private List<ParkingEventListener> listeners;
    
    public FineManager() {
        this.fineDAO = new FineDAO();
        this.listeners = new ArrayList<>();
        initializeStrategies();
    }
    
    /**
     * Initializes all available fine calculation strategies
     */
    private void initializeStrategies() {
        strategies = new HashMap<>();
        strategies.put(FineScheme.FIXED, new FixedFineStrategy());
        strategies.put(FineScheme.PROGRESSIVE, new ProgressiveFineStrategy());
        strategies.put(FineScheme.HOURLY, new HourlyFineStrategy());
        
        // Set default strategy
        currentStrategy = strategies.get(FineScheme.FIXED);
    }
    
    /**
     * Sets the fine calculation strategy
     */
    public void setFineStrategy(FineScheme scheme) {
        FineCalculationStrategy strategy = strategies.get(scheme);
        if (strategy != null) {
            this.currentStrategy = strategy;
            System.out.println("Fine strategy changed to: " + strategy);
        } else {
            System.err.println("Unknown fine scheme: " + scheme);
        }
    }
    
    /**
     * Sets strategy directly (for testing)
     */
    public void setFineStrategy(FineCalculationStrategy strategy) {
        this.currentStrategy = strategy;
    }
    
    /**
     * Gets the current strategy
     */
    public FineCalculationStrategy getCurrentStrategy() {
        return currentStrategy;
    }
    
    /**
     * Detects and generates fines for a ticket
     * Returns list of fines generated (may be empty)
     */
    public List<Fine> detectAndGenerateFines(Ticket ticket, ParkingSpot spot, long hoursParked) {
        List<Fine> generatedFines = new ArrayList<>();
        
        // Check for overstay (> 24 hours)
        if (hoursParked > Constants.OVERSTAY_THRESHOLD_HOURS) {
            Fine overstayFine = generateFine(ticket, FineType.OVERSTAY, hoursParked);
            if (overstayFine != null) {
                generatedFines.add(overstayFine);
            }
        }
        
        // Check for reserved spot misuse
        // (This will be enhanced in Phase 5 with reservation checking)
        if (spot.getSpotType() == SpotType.RESERVED) {
            // For now, we assume any parking in RESERVED without explicit validation is misuse
            // In Phase 5, we'll check against reservation records
            // Commenting out for now to avoid false positives
            // Fine reservedFine = generateFine(ticket, FineType.RESERVED_MISUSE, hoursParked);
            // if (reservedFine != null) {
            //     generatedFines.add(reservedFine);
            // }
        }
        
        return generatedFines;
    }
    
    /**
     * Generates a fine using the ticket's locked fine scheme
     */
    private Fine generateFine(Ticket ticket, FineType fineType, long hoursParked) {
        // IMPORTANT: Use the fine scheme from the ticket (locked at entry)
        // NOT the current system scheme
        FineScheme ticketScheme = ticket.getFineScheme();
        FineCalculationStrategy strategy = strategies.get(ticketScheme);
        
        if (strategy == null) {
            System.err.println("No strategy found for scheme: " + ticketScheme);
            return null;
        }
        
        // Calculate fine amount using the appropriate strategy
        double fineAmount = strategy.calculateFine(hoursParked, fineType);
        
        if (fineAmount <= 0) {
            return null;  // No fine to generate
        }
        
        // Create fine object
        Fine fine = new Fine(
            ticket.getPlateNumber(),
            ticket.getTicketId(),
            fineType,
            fineAmount,
            ticketScheme,
            TimeUtil.now()
        );
        
        // Save to database
        if (fineDAO.insertFine(fine)) {
            System.out.println("Fine generated: " + fine);
            
            // Notify observers
            notifyListeners(ParkingEventType.FINE_GENERATED, fine);
            
            return fine;
        } else {
            System.err.println("Failed to save fine to database");
            return null;
        }
    }
    
    /**
     * Gets all unpaid fines for a plate
     */
    public List<Fine> getUnpaidFines(String plateNumber) {
        return fineDAO.getUnpaidFines(plateNumber);
    }
    
    /**
     * Gets total unpaid fine amount for a plate
     */
    public double getTotalUnpaidFineAmount(String plateNumber) {
        return fineDAO.getTotalUnpaidFineAmount(plateNumber);
    }
    
    /**
     * Marks fines as paid
     */
    public boolean markFinesPaid(List<Integer> fineIds) {
        boolean success = fineDAO.markFinesPaid(fineIds);
        if (success && !fineIds.isEmpty()) {
            notifyListeners(ParkingEventType.FINE_PAID, fineIds);
        }
        return success;
    }
    
    /**
     * Marks all unpaid fines for a plate as paid
     */
    public boolean markAllFinesPaid(String plateNumber) {
        boolean success = fineDAO.markAllFinesPaidForPlate(plateNumber);
        if (success) {
            notifyListeners(ParkingEventType.FINE_PAID, plateNumber);
        }
        return success;
    }
    
    /**
     * Gets all unpaid fines in the system
     */
    public List<Fine> getAllUnpaidFines() {
        return fineDAO.getAllUnpaidFines();
    }
    
    // ========== Observer Pattern Methods ==========
    
    /**
     * Adds a listener to be notified of fine events
     */
    public void addListener(ParkingEventListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    /**
     * Removes a listener
     */
    public void removeListener(ParkingEventListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Notifies all listeners of an event
     */
    private void notifyListeners(ParkingEventType eventType, Object eventData) {
        for (ParkingEventListener listener : listeners) {
            listener.onParkingEvent(eventType, eventData);
        }
    }
}
