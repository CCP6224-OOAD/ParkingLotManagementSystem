package parkinglotmanagementsystem.controller;

import parkinglotmanagementsystem.model.*;
import parkinglotmanagementsystem.service.*;
import parkinglotmanagementsystem.util.PlateValidator;
import parkinglotmanagementsystem.util.TimeUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExitController {

  private ParkingService parkingService;
  private VehicleService vehicleService;
  private TicketService ticketService;
  private BillingService billingService;
  private PaymentService paymentService;

  public ExitController(ParkingService parkingService, FineManager fineManager, PaymentService paymentService) {
    this.vehicleService = new VehicleService();
    this.ticketService = new TicketService();
    this.billingService = new BillingService(fineManager);
    this.parkingService = parkingService;
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

  public Payment processExit(String plateNumber, PaymentMethod paymentMethod, double paymentAmount) {
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
      Vehicle vehicle = (Vehicle) bill.get("vehicle");
      double parkingFee = (Double) bill.get("parkingFee");
      @SuppressWarnings("unchecked")
      List<Fine> fines = (List<Fine>) bill.get("unpaidFines");

      double balance = vehicle.getBalance();
      double paidParkingFee = 0.0;
      if (balance < 0) {
        paidParkingFee = Math.min(-balance, paymentAmount);
      }
      paymentAmount += balance;

      if (paymentAmount > 0) {
        paidParkingFee += Math.min(paymentAmount, parkingFee);
      }
      paymentAmount -= parkingFee;

      double fineAmount = 0.0;
      List<Integer> paidFineIds = new ArrayList<>();
      for (Fine fine : fines) {
        if (paymentAmount < fine.getFineAmount()) {
          break;
        }
        paidFineIds.add(fine.getFineId());
        fineAmount += fine.getFineAmount();
        paymentAmount -= fine.getFineAmount();
      }

      // Process payment
      Payment payment = paymentService.processPayment(
          ticket.getTicketId(),
          paidFineIds,
          paidParkingFee,
          fineAmount,
          paymentMethod);

      if (payment == null) {
        System.err.println("Payment processing failed");
        return null;
      }

      LocalDateTime exitTime = (LocalDateTime) bill.get("exitTime");
      long hoursParked = (Long) bill.get("hoursParked");

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
        // Payment and ticket already processed
        System.err.println("Failed to release spot: " + ticket.getSpotId());
      }

      // Update balance
      vehicle.setBalance(paymentAmount);
      boolean vehicleUpdated = vehicleService.updateVehicle(vehicle);
      if (!vehicleUpdated) {
        System.err.println("Failed to update vehicle: " + vehicle.getPlateNumber());
      }

      parkingService.notifyReleaseSpot();
      paymentService.notifyProcessPayment(payment);

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
    Vehicle oldVehicle = (Vehicle) billDetails.get("vehicle");
    Vehicle newVehicle = vehicleService.getVehicle(oldVehicle.getPlateNumber());
    LocalDateTime entryTime = (LocalDateTime) billDetails.get("entryTime");
    LocalDateTime exitTime = (LocalDateTime) billDetails.get("exitTime");
    Long hoursParked = (Long) billDetails.get("hoursParked");

    return paymentService.generateReceipt(
        payment,
        newVehicle.getPlateNumber(),
        entryTime,
        exitTime,
        hoursParked,
        newVehicle.getBalance());
  }
}
