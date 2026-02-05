package parkinglotmanagementsystem.controller;

import parkinglotmanagementsystem.model.*;
import parkinglotmanagementsystem.service.*;
import parkinglotmanagementsystem.util.PlateValidator;
import parkinglotmanagementsystem.util.TimeUtil;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class ExitController {

  private ParkingService parkingService;
  private VehicleService vehicleService;
  private TicketService ticketService;
  private BillingService billingService;
  private PaymentService paymentService;

  public ExitController(FineManager fineManager, PaymentService paymentService) {
    this.parkingService = new ParkingService();
    this.vehicleService = new VehicleService();
    this.ticketService = new TicketService();
    this.billingService = new BillingService(fineManager);
    this.paymentService = paymentService;
  }

  public Map<String, Object> calculateBill(String plateNumber) {
    Map<String, Object> billDetails = new HashMap<>();

    try {
      // Validate plate number
      String normalizedPlate = PlateValidator.validateAndNormalize(plateNumber);

      // Find active ticket
      Ticket ticket = ticketService.getActiveTicket(normalizedPlate);
      if (ticket == null) {
        billDetails.put("error", "No active parking session found for: " + normalizedPlate);
        return billDetails;
      }

      // Get vehicle
      Vehicle vehicle = vehicleService.getVehicle(normalizedPlate);
      if (vehicle == null) {
        billDetails.put("error", "Vehicle not found: " + normalizedPlate);
        return billDetails;
      }

      // Get parking spot
      ParkingSpot spot = parkingService.getSpotById(ticket.getSpotId());
      if (spot == null) {
        billDetails.put("error", "Parking spot not found: " + ticket.getSpotId());
        return billDetails;
      }

      // Calculate bill using BillingService
      LocalDateTime exitTime = TimeUtil.now();
      billDetails = billingService.generateBill(ticket, vehicle, spot, exitTime);

      return billDetails;

    } catch (IllegalArgumentException e) {
      billDetails.put("error", "Invalid plate number: " + e.getMessage());
      return billDetails;
    } catch (Exception e) {
      billDetails.put("error", "Error calculating bill: " + e.getMessage());
      e.printStackTrace();
      return billDetails;
    }
  }

  public Payment processExit(String plateNumber, PaymentMethod paymentMethod) {
    try {
      // Validate plate number
      String normalizedPlate = PlateValidator.validateAndNormalize(plateNumber);

      // Calculate bill
      Map<String, Object> bill = calculateBill(normalizedPlate);
      if (bill.containsKey("error")) {
        System.err.println("Cannot process exit: " + bill.get("error"));
        return null;
      }

      Ticket ticket = (Ticket) bill.get("ticket");
      LocalDateTime exitTime = (LocalDateTime) bill.get("exitTime");
      double parkingFee = (Double) bill.get("parkingFee");
      double totalFineAmount = (Double) bill.get("totalFineAmount");
      long hoursParked = (Long) bill.get("hoursParked");

      // Process payment
      Payment payment = paymentService.processPayment(
          ticket.getTicketId(),
          normalizedPlate,
          parkingFee,
          totalFineAmount,
          paymentMethod);

      if (payment == null) {
        System.err.println("Payment processing failed");
        return null;
      }

      // Close ticket
      boolean ticketClosed = ticketService.closeTicket(ticket.getTicketId(), exitTime);
      if (!ticketClosed) {
        System.err.println("Failed to close ticket: " + ticket.getTicketId());
        // Payment already processed, log this issue
        return payment;
      }

      // Release spot
      boolean spotReleased = parkingService.releaseSpot(ticket.getSpotId());
      if (!spotReleased) {
        System.err.println("Failed to release spot: " + ticket.getSpotId());
        // Payment and ticket already processed
      }

      System.out.println("=== VEHICLE EXIT SUCCESSFUL ===");
      System.out.println("Plate: " + normalizedPlate);
      System.out.println("Spot: " + ticket.getSpotId());
      System.out.println("Duration: " + hoursParked + " hours");
      System.out.println("Total Paid: RM " + String.format("%.2f", payment.getTotalAmount()));
      System.out.println("Payment Method: " + paymentMethod);

      return payment;

    } catch (IllegalArgumentException e) {
      System.err.println("Invalid input: " + e.getMessage());
      return null;
    } catch (Exception e) {
      System.err.println("Unexpected error during vehicle exit");
      e.printStackTrace();
      return null;
    }
  }

  public String getBillSummary(Map<String, Object> billDetails) {
    return billingService.formatBill(billDetails);
  }

  public String generateReceipt(Payment payment, Map<String, Object> billDetails) {
    Ticket ticket = (Ticket) billDetails.get("ticket");
    Vehicle vehicle = (Vehicle) billDetails.get("vehicle");
    LocalDateTime entryTime = (LocalDateTime) billDetails.get("entryTime");
    LocalDateTime exitTime = (LocalDateTime) billDetails.get("exitTime");
    Long hoursParked = (Long) billDetails.get("hoursParked");

    return paymentService.generateReceipt(
        payment,
        vehicle.getPlateNumber(),
        entryTime,
        exitTime,
        hoursParked);
  }

  public java.util.List<Ticket> getParkedVehicles() {
    return ticketService.getAllParkedVehicles();
  }
}
