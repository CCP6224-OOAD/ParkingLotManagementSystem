package parkinglotmanagementsystem.service;

import parkinglotmanagementsystem.dao.ReservationDAO;
import parkinglotmanagementsystem.model.ParkingSpot;
import parkinglotmanagementsystem.model.Reservation;
import parkinglotmanagementsystem.model.SpotType;
import parkinglotmanagementsystem.util.TimeUtil;

import java.util.List;

public class ReservationService {

    private ReservationDAO reservationDAO;
    private ParkingService parkingService;

    public ReservationService(ParkingService parkingService) {
        this.reservationDAO = new ReservationDAO();
        this.parkingService = parkingService;
    }

    public Reservation createReservation(String spotId) {
        // Verify spot exists and is of type RESERVED
        ParkingSpot spot = parkingService.getSpotById(spotId);
        if (spot == null) {
            System.err.println("Spot not found: " + spotId);
            return null;
        }

        if (spot.getSpotType() != SpotType.RESERVED) {
            System.err.println("Spot is not a RESERVED type: " + spotId);
            return null;
        }

        // Check if spot already has an active reservation
        Reservation existingReservation = reservationDAO.findActiveReservation(spotId);
        if (existingReservation != null) {
            System.err.println("Spot already has an active reservation: " + spotId);
            return existingReservation;
        }

        // Create new reservation
        Reservation reservation = new Reservation(spotId, TimeUtil.now());

        if (reservationDAO.insertReservation(reservation)) {
            System.out.println("Reservation created: " + reservation);
            return reservation;
        } else {
            System.err.println("Failed to create reservation for spot: " + spotId);
            return null;
        }
    }

    public boolean assignReservation(int reservationId, String plateNumber) {
        Reservation reservation = reservationDAO.findReservationById(reservationId);

        if (reservation == null) {
            System.err.println("Reservation not found: " + reservationId);
            return false;
        }

        if (!reservation.isActive()) {
            System.err.println("Reservation is not active: " + reservationId);
            return false;
        }

        if (reservation.getPlateNumber() != null) {
            System.err.println("Reservation already assigned to: " + reservation.getPlateNumber());
            return false;
        }

        reservation.assignToVehicle(plateNumber);

        if (reservationDAO.updateReservation(reservation)) {
            System.out.println("Reservation assigned to vehicle: " + plateNumber);
            return true;
        } else {
            return false;
        }
    }

    public boolean useReservation(String spotId, String plateNumber) {
        Reservation reservation = reservationDAO.findActiveReservation(spotId);

        if (reservation == null) {
            System.err.println("No active reservation for spot: " + spotId);
            return false;
        }

        // Mark as used
        reservation.markAsUsed();
        reservation.setPlateNumber(plateNumber);

        return reservationDAO.updateReservation(reservation);
    }

    public boolean hasActiveReservation(String spotId) {
        return reservationDAO.findActiveReservation(spotId) != null;
    }

    public boolean hasReservation(String spotId, String plateNumber) {
        Reservation reservation = reservationDAO.findActiveReservation(spotId);

        if (reservation == null) {
            return false;
        }

        // Reservation exists and either unassigned or assigned to this plate
        return reservation.getPlateNumber() == null ||
                reservation.getPlateNumber().equals(plateNumber);
    }

    public Reservation getActiveReservation(String spotId) {
        return reservationDAO.findActiveReservation(spotId);
    }

    public boolean cancelReservation(int reservationId) {
        return reservationDAO.cancelReservation(reservationId);
    }

    public List<Reservation> getAllActiveReservations() {
        return reservationDAO.getAllActiveReservations();
    }

    public List<Reservation> getReservationHistory(String spotId) {
        return reservationDAO.getReservationsBySpot(spotId);
    }

    public int getActiveReservationCount() {
        return reservationDAO.getActiveReservationCount();
    }
}
