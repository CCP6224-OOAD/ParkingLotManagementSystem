package parkinglotmanagementsystem.service;

import parkinglotmanagementsystem.dao.PaymentDAO;
import parkinglotmanagementsystem.model.Payment;
import parkinglotmanagementsystem.model.PaymentMethod;
import parkinglotmanagementsystem.observer.ParkingEventListener;
import parkinglotmanagementsystem.observer.ParkingEventType;
import parkinglotmanagementsystem.util.TimeUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Payment Service with Observer Pattern
 * Handles payment processing and receipt generation
 */
public class PaymentService {
    
    private PaymentDAO paymentDAO;
    private FineManager fineManager;
    private List<ParkingEventListener> listeners;
    
    public PaymentService(FineManager fineManager) {
        this.paymentDAO = new PaymentDAO();
        this.fineManager = fineManager;
        this.listeners = new ArrayList<>();
    }
    
    /**
     * Processes a payment
     * Marks associated fines as paid and saves payment record
     */
    public Payment processPayment(String ticketId, String plateNumber, 
                                 double parkingFee, double fineAmount, 
                                 PaymentMethod paymentMethod) {
        // Create payment record
        Payment payment = new Payment(
            ticketId,
            parkingFee,
            fineAmount,
            paymentMethod,
            TimeUtil.now()
        );
        
        // Save payment to database
        if (!paymentDAO.insertPayment(payment)) {
            System.err.println("Failed to save payment");
            return null;
        }
        
        // Mark all unpaid fines for this plate as paid
        if (fineAmount > 0) {
            fineManager.markAllFinesPaid(plateNumber);
        }
        
        System.out.println("Payment processed successfully: " + payment);
        
        // Notify observers
        notifyListeners(ParkingEventType.PAYMENT_PROCESSED, payment);
        notifyListeners(ParkingEventType.REVENUE_UPDATED, payment.getTotalAmount());
        
        return payment;
    }
    
    /**
     * Generates a formatted receipt
     */
    public String generateReceipt(Payment payment, String plateNumber, 
                                  LocalDateTime entryTime, LocalDateTime exitTime, 
                                  long hoursParked) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("=".repeat(50)).append("\n");
        sb.append("PAYMENT RECEIPT\n");
        sb.append("=".repeat(50)).append("\n");
        
        sb.append(String.format("Payment ID: %d%n", payment.getPaymentId()));
        sb.append(String.format("Ticket ID: %s%n", payment.getTicketId()));
        sb.append(String.format("Plate Number: %s%n", plateNumber));
        sb.append("\n");
        
        sb.append(String.format("Entry Time: %s%n", TimeUtil.formatForDisplay(entryTime)));
        sb.append(String.format("Exit Time: %s%n", TimeUtil.formatForDisplay(exitTime)));
        sb.append(String.format("Duration: %d hours%n", hoursParked));
        sb.append("\n");
        
        sb.append("-".repeat(50)).append("\n");
        sb.append(String.format("Parking Fee: RM %.2f%n", payment.getParkingFee()));
        sb.append(String.format("Fine Amount: RM %.2f%n", payment.getFineAmount()));
        sb.append("-".repeat(50)).append("\n");
        sb.append(String.format("Total Paid: RM %.2f%n", payment.getTotalAmount()));
        sb.append("-".repeat(50)).append("\n");
        
        sb.append(String.format("Payment Method: %s%n", payment.getPaymentMethod()));
        sb.append(String.format("Payment Time: %s%n", 
            TimeUtil.formatForDisplay(payment.getPaymentTime())));
        sb.append("\n");
        
        sb.append("Thank you for using our parking facility!\n");
        sb.append("=".repeat(50)).append("\n");
        
        return sb.toString();
    }
    
    /**
     * Gets total revenue
     */
    public double getTotalRevenue() {
        return paymentDAO.getTotalRevenue();
    }
    
    /**
     * Gets total parking revenue
     */
    public double getTotalParkingRevenue() {
        return paymentDAO.getTotalParkingRevenue();
    }
    
    /**
     * Gets total fine revenue
     */
    public double getTotalFineRevenue() {
        return paymentDAO.getTotalFineRevenueFromPayments();
    }
    
    /**
     * Gets payment count
     */
    public int getPaymentCount() {
        return paymentDAO.getTotalPaymentCount();
    }
    
    // ========== Observer Pattern Methods ==========
    
    /**
     * Adds a listener to be notified of payment events
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
