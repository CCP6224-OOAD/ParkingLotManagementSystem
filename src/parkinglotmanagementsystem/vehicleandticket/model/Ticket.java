package parkinglotmanagementsystem.vehicleandticket.model;

import java.time.LocalDateTime;

import parkinglotmanagementsystem.fineandpayment.model.FineScheme;

public class Ticket {

  private String ticketId; // Format: T-PLATE-TIMESTAMP
  private String plateNumber;
  private String spotId;
  private LocalDateTime entryTime;
  private LocalDateTime exitTime; // null if still parked
  private FineScheme fineScheme;

  public Ticket(String ticketId, String plateNumber, String spotId,
      LocalDateTime entryTime, FineScheme fineScheme) {
    this.ticketId = ticketId;
    this.plateNumber = plateNumber;
    this.spotId = spotId;
    this.entryTime = entryTime;
    this.exitTime = null; // Still parked
    this.fineScheme = fineScheme;
  }

  public Ticket(String ticketId, String plateNumber, String spotId,
      LocalDateTime entryTime, LocalDateTime exitTime, FineScheme fineScheme) {
    this.ticketId = ticketId;
    this.plateNumber = plateNumber;
    this.spotId = spotId;
    this.entryTime = entryTime;
    this.exitTime = exitTime;
    this.fineScheme = fineScheme;
  }

  public boolean hasExited() {
    return exitTime != null;
  }

  public boolean isActive() {
    return exitTime == null;
  }

  public void recordExit(LocalDateTime exitTime) {
    if (this.exitTime != null) {
      throw new IllegalStateException("Exit time already recorded for ticket: " + ticketId);
    }
    this.exitTime = exitTime;
  }

  // Getters and Setters

  public String getTicketId() {
    return ticketId;
  }

  public String getPlateNumber() {
    return plateNumber;
  }

  public String getSpotId() {
    return spotId;
  }

  public LocalDateTime getEntryTime() {
    return entryTime;
  }

  public LocalDateTime getExitTime() {
    return exitTime;
  }

  public void setExitTime(LocalDateTime exitTime) {
    this.exitTime = exitTime;
  }

  public FineScheme getFineScheme() {
    return fineScheme;
  }

  @Override
  public String toString() {
    return String.format("Ticket[%s, Plate=%s, Spot=%s, Entry=%s, Exit=%s, Scheme=%s]",
        ticketId, plateNumber, spotId,
        entryTime != null ? entryTime.toString() : "N/A",
        exitTime != null ? exitTime.toString() : "Still Parked",
        fineScheme);
  }
}
