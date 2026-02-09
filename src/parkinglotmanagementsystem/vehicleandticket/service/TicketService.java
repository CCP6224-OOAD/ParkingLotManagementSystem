package parkinglotmanagementsystem.vehicleandticket.service;

import parkinglotmanagementsystem.fineandpayment.model.FineScheme;
import parkinglotmanagementsystem.main.dao.SystemConfigDAO;
import parkinglotmanagementsystem.main.util.TicketGenerator;
import parkinglotmanagementsystem.main.util.TimeUtil;
import parkinglotmanagementsystem.vehicleandticket.dao.TicketDAO;
import parkinglotmanagementsystem.vehicleandticket.model.Ticket;

import java.time.LocalDateTime;
import java.util.List;

public class TicketService {

  private TicketDAO ticketDAO;
  private SystemConfigDAO configDAO;

  public TicketService() {
    this.ticketDAO = new TicketDAO();
    this.configDAO = new SystemConfigDAO();
  }

  public Ticket createTicket(String plateNumber, String spotId) {
    // Check if vehicle is already parked
    if (isVehicleParked(plateNumber)) {
      System.err.println("Vehicle is already parked: " + plateNumber);
      return null;
    }

    String ticketId = TicketGenerator.generateTicketId(plateNumber);

    LocalDateTime entryTime = TimeUtil.now();

    FineScheme currentScheme = configDAO.getCurrentFineScheme();

    Ticket ticket = new Ticket(ticketId, plateNumber, spotId, entryTime, currentScheme);

    if (ticketDAO.insertTicket(ticket)) {
      System.out.println("Ticket created: " + ticketId);
      return ticket;
    } else {
      System.err.println("Failed to create ticket for: " + plateNumber);
      return null;
    }
  }

  public Ticket getActiveTicket(String plateNumber) {
    return ticketDAO.findActiveTicket(plateNumber);
  }

  public boolean closeTicket(String ticketId, LocalDateTime exitTime) {
    // Update exit time in database
    boolean updated = ticketDAO.updateExitTime(ticketId, exitTime);

    if (updated) {
      System.out.println("Ticket closed: " + ticketId);
    } else {
      System.err.println("Failed to close ticket: " + ticketId);
    }

    return updated;
  }

  public boolean closeTicket(String ticketId) {
    return closeTicket(ticketId, TimeUtil.now());
  }

  public List<Ticket> getAllParkedVehicles() {
    return ticketDAO.getAllActiveTickets();
  }

  public boolean isVehicleParked(String plateNumber) {
    return ticketDAO.isVehicleParked(plateNumber);
  }

  public int getParkedVehicleCount() {
    return ticketDAO.getActiveTicketCount();
  }

  public int getTotalTicketCount() {
    return ticketDAO.getTotalTicketCount();
  }

}
