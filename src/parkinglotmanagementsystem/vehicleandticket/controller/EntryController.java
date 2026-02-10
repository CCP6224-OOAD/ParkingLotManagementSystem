package parkinglotmanagementsystem.vehicleandticket.controller;

import parkinglotmanagementsystem.main.util.PlateValidator;
import parkinglotmanagementsystem.main.util.TimeUtil;
import parkinglotmanagementsystem.parking.model.ParkingSpot;
import parkinglotmanagementsystem.parking.model.SpotType;
import parkinglotmanagementsystem.parking.service.ParkingService;
import parkinglotmanagementsystem.vehicleandticket.model.Ticket;
import parkinglotmanagementsystem.vehicleandticket.model.Vehicle;
import parkinglotmanagementsystem.vehicleandticket.model.VehicleType;
import parkinglotmanagementsystem.vehicleandticket.service.TicketService;
import parkinglotmanagementsystem.vehicleandticket.service.VehicleService;

import java.util.List;

public class EntryController {

  private ParkingService parkingService;
  private VehicleService vehicleService;
  private TicketService ticketService;

  public EntryController(ParkingService parkingService) {
    this.parkingService = parkingService;
    this.vehicleService = new VehicleService();
    this.ticketService = new TicketService();
  }

  public List<ParkingSpot> findSuitableSpots(VehicleType vehicleType) {
    return parkingService.getSuitableSpots(vehicleType);
  }

  /**
   * Parks a vehicle in a specific spot
   * Complete entry workflow:
   * 1. Validate plate number
   * 2. Register vehicle (if not already registered)
   * 3. Check if vehicle is already parked
   * 4. Validate spot availability and compatibility
   * 5. Check reservation (if RESERVED spot)
   * 6. Allocate spot
   * 7. Create parking ticket
   */
  public Ticket parkVehicle(String plateNumber, VehicleType vehicleType, String spotId) {
    try {
      // Step 1: Validate plate number
      String normalizedPlate = PlateValidator.validateAndNormalize(plateNumber);

      // Step 2: Register vehicle if not already registered
      Vehicle vehicle = vehicleService.getVehicle(normalizedPlate);
      if (vehicle == null) {
        vehicle = vehicleService.registerVehicle(normalizedPlate, vehicleType);
        if (vehicle == null) {
          System.err.println("Failed to register vehicle");
          return null;
        }
      } else {
        // Verify vehicle type matches
        if (vehicle.getVehicleType() != vehicleType) {
          System.err.println("Vehicle type mismatch! Registered as " +
              vehicle.getVehicleType() + " but trying to park as " + vehicleType);
          return null;
        }
      }

      // Step 3: Check if vehicle is already parked
      if (ticketService.isVehicleParked(normalizedPlate)) {
        System.err.println("Vehicle is already parked: " + normalizedPlate);
        Ticket existingTicket = ticketService.getActiveTicket(normalizedPlate);
        System.err.println("Active ticket: " + existingTicket);
        return null;
      }

      // Step 4: Validate spot
      ParkingSpot spot = parkingService.getSpotById(spotId);
      if (spot == null) {
        System.err.println("Spot not found: " + spotId);
        return null;
      }

      if (!spot.isAvailable()) {
        System.err.println("Spot is not available: " + spotId);
        return null;
      }

      // Check vehicle-spot compatibility
      if (!vehicle.canParkIn(spot.getSpotType()) && spot.getSpotType() != SpotType.RESERVED) {
        System.err.println("Vehicle cannot park in this spot type. " +
            "Vehicle: " + vehicleType + ", Spot: " + spot.getSpotType());
        return null;
      }

      // Step 5: Check reservation for RESERVED spots
      if (spot.getSpotType() == SpotType.RESERVED) {
        System.out.println("A Misuse of Reserved Spot Fine will be generated later");

      }

      // Step 6: Allocate spot
      boolean allocated = parkingService.allocateSpot(spotId, normalizedPlate);
      if (!allocated) {
        System.err.println("Failed to allocate spot: " + spotId);
        return null;
      }

      // Step 7: Create ticket
      Ticket ticket = ticketService.createTicket(normalizedPlate, spotId);
      if (ticket == null) {
        // Rollback spot allocation
        parkingService.releaseSpot(spotId);
        System.err.println("Failed to create ticket, spot released");
        return null;
      }

      parkingService.loadParkingLot();
      parkingService.notifyAllocateSpot();

      System.out.println("=== VEHICLE ENTRY SUCCESSFUL ===");
      System.out.println("Plate: " + normalizedPlate);
      System.out.println("Spot: " + spotId);
      System.out.println("Ticket: " + ticket.getTicketId());
      System.out.println("Entry Time: " + TimeUtil.formatForDisplay(ticket.getEntryTime()));
      System.out.println("Fine Scheme: " + ticket.getFineScheme());

      return ticket;

    } catch (IllegalArgumentException e) {
      System.err.println("Invalid input: " + e.getMessage());
      return null;
    } catch (Exception e) {
      System.err.println("Unexpected error during vehicle entry");
      e.printStackTrace();
      return null;
    }
  }
}
