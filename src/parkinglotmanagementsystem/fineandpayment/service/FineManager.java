package parkinglotmanagementsystem.fineandpayment.service;

import parkinglotmanagementsystem.fineandpayment.dao.FineDAO;
import parkinglotmanagementsystem.fineandpayment.model.Fine;
import parkinglotmanagementsystem.fineandpayment.model.FineScheme;
import parkinglotmanagementsystem.fineandpayment.model.FineType;
import parkinglotmanagementsystem.fineandpayment.strategy.*;
import parkinglotmanagementsystem.main.observer.ParkingEventListener;
import parkinglotmanagementsystem.main.observer.ParkingEventType;
import parkinglotmanagementsystem.main.util.Constants;
import parkinglotmanagementsystem.main.util.TimeUtil;
import parkinglotmanagementsystem.parking.model.ParkingSpot;
import parkinglotmanagementsystem.parking.model.SpotType;
import parkinglotmanagementsystem.vehicleandticket.model.Ticket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private void initializeStrategies() {
        strategies = new HashMap<>();
        strategies.put(FineScheme.FIXED, new FixedFineStrategy());
        strategies.put(FineScheme.PROGRESSIVE, new ProgressiveFineStrategy());
        strategies.put(FineScheme.HOURLY, new HourlyFineStrategy());

        // Set default strategy
        currentStrategy = strategies.get(FineScheme.FIXED);
    }

    public void setFineStrategy(FineScheme scheme) {
        FineCalculationStrategy strategy = strategies.get(scheme);
        if (strategy != null) {
            this.currentStrategy = strategy;
            System.out.println("Fine strategy changed to: " + strategy);
        } else {
            System.err.println("Unknown fine scheme: " + scheme);
        }
    }

    public void setFineStrategy(FineCalculationStrategy strategy) {
        this.currentStrategy = strategy;
    }

    public FineCalculationStrategy getCurrentStrategy() {
        return currentStrategy;
    }

    public List<Fine> detectAndGenerateFines(Ticket ticket, ParkingSpot spot, long hoursParked) {
        List<Fine> generatedFines = new ArrayList<>();

        // overstay
        if (hoursParked > Constants.OVERSTAY_THRESHOLD_HOURS) {
            Fine overstayFine = generateFine(ticket, FineType.OVERSTAY, hoursParked);
            if (overstayFine != null) {
                generatedFines.add(overstayFine);
            }
        }

        // reserved spot misuse
        if (spot.getSpotType() == SpotType.RESERVED) {
            Fine overstayFine = generateFine(ticket, FineType.RESERVED_MISUSE, hoursParked);
            if (overstayFine != null) {
                generatedFines.add(overstayFine);
            }
        }

        return generatedFines;
    }

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
            return null; // No fine to generate
        }

        Fine existingFine = fineDAO.getFineByTicketIdAndFineType(ticket.getTicketId(), fineType);

        if (existingFine == null) {
            // Create fine object
            Fine fine = new Fine(
                    ticket.getPlateNumber(),
                    ticket.getTicketId(),
                    fineType,
                    fineAmount,
                    ticketScheme,
                    TimeUtil.now());

            if (fineDAO.insertFine(fine)) {
                System.out.println("Fine generated: " + fine);

                // Notify observers
                notifyListeners(ParkingEventType.FINE_GENERATED, fine);

                return fine;
            } else {
                System.err.println("Failed to save fine to database");
                return null;
            }
        } else {
            existingFine.setFineAmount(fineAmount);

            if (fineDAO.updateFine(existingFine)) {
                System.out.println("Fine updated: " + existingFine);

                // Notify observers
                notifyListeners(ParkingEventType.FINE_GENERATED, existingFine);

                return existingFine;
            } else {
                System.err.println("Failed to update fine to database");
                return null;
            }
        }
    }

    public List<Fine> getUnpaidFines(String plateNumber) {
        return fineDAO.getUnpaidFines(plateNumber);
    }

    // for future use
    public double getTotalUnpaidFineAmount(String plateNumber) {
        return fineDAO.getTotalUnpaidFineAmount(plateNumber);
    }

    public boolean markFinesPaid(List<Integer> fineIds) {
        boolean success = fineDAO.markFinesPaid(fineIds);
        if (success && !fineIds.isEmpty()) {
            notifyListeners(ParkingEventType.FINE_PAID, fineIds);
        }
        return success;
    }

    // for future use
    public boolean markAllFinesPaid(String plateNumber) {
        boolean success = fineDAO.markAllFinesPaidForPlate(plateNumber);
        if (success) {
            notifyListeners(ParkingEventType.FINE_PAID, plateNumber);
        }
        return success;
    }

    public List<Fine> getAllUnpaidFines() {
        return fineDAO.getAllUnpaidFines();
    }

    // Observer Pattern Methods 

    public void addListener(ParkingEventListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(ParkingEventListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners(ParkingEventType eventType, Object eventData) {
        for (ParkingEventListener listener : listeners) {
            listener.onParkingEvent(eventType, eventData);
        }
    }
}
