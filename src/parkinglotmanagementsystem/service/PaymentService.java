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

public class PaymentService {

    private PaymentDAO paymentDAO;
    private FineManager fineManager;
    private List<ParkingEventListener> listeners;

    public PaymentService(FineManager fineManager) {
        this.paymentDAO = new PaymentDAO();
        this.fineManager = fineManager;
        this.listeners = new ArrayList<>();
    }

    public Payment processPayment(String ticketId, List<Integer> fineIds,
            double parkingFee, double fineAmount,
            PaymentMethod paymentMethod) {
        Payment payment = new Payment(
                ticketId,
                parkingFee,
                fineAmount,
                paymentMethod,
                TimeUtil.now());

        if (!paymentDAO.insertPayment(payment)) {
            System.err.println("Failed to save payment");
            return null;
        }

        if (!fineIds.isEmpty()) {
            fineManager.markFinesPaid(fineIds);
        }

        System.out.println("Payment processed successfully: " + payment);

        return payment;
    }

    public void notifyProcessPayment(Payment payment) {
        // Notify observers
        notifyListeners(ParkingEventType.PAYMENT_PROCESSED, payment);
        notifyListeners(ParkingEventType.REVENUE_UPDATED, payment.getTotalAmount());
    }

    public String generateReceipt(Payment payment, String plateNumber,
            LocalDateTime entryTime, LocalDateTime exitTime,
            long hoursParked, double balance) {
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
        sb.append(String.format("New Balance: RM %.2f%n", balance));
        sb.append("-".repeat(50)).append("\n");

        sb.append(String.format("Payment Method: %s%n", payment.getPaymentMethod()));
        sb.append(String.format("Payment Time: %s%n",
                TimeUtil.formatForDisplay(payment.getPaymentTime())));
        sb.append("\n");

        sb.append("Thank you for using our parking facility!\n");
        sb.append("=".repeat(50)).append("\n");

        return sb.toString();
    }

    public double getTotalRevenue() {
        return paymentDAO.getTotalRevenue();
    }

    public double getTotalParkingRevenue() {
        return paymentDAO.getTotalParkingRevenue();
    }

    public double getTotalFineRevenue() {
        return paymentDAO.getTotalFineRevenueFromPayments();
    }

    public int getPaymentCount() {
        return paymentDAO.getTotalPaymentCount();
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
