package parkinglotmanagementsystem.controller;

import parkinglotmanagementsystem.model.*;
import parkinglotmanagementsystem.service.*;
import parkinglotmanagementsystem.util.TimeUtil;

import java.util.List;
import java.util.Map;

public class ReportController {

    private AdminController adminController;
    private TicketService ticketService;

    public ReportController(AdminController adminController) {
        this.adminController = adminController;
        this.ticketService = new TicketService();
    }

    public String generateOccupancyReport() {
        Map<String, Object> stats = adminController.getOccupancyStats();

        StringBuilder sb = new StringBuilder();
        sb.append("=".repeat(60)).append("\n");
        sb.append("OCCUPANCY REPORT\n");
        sb.append("=".repeat(60)).append("\n\n");

        sb.append(String.format("Total Spots: %d%n", stats.get("totalSpots")));
        sb.append(String.format("Occupied: %d%n", stats.get("occupiedSpots")));
        sb.append(String.format("Available: %d%n", stats.get("availableSpots")));
        sb.append(String.format("Global Occupancy Rate: %.1f%%%n", stats.get("globalOccupancyRate")));
        sb.append("\n");

        sb.append("Floor-by-Floor Breakdown:\n");
        sb.append("-".repeat(60)).append("\n");
        sb.append(String.format("%-10s %-12s %-12s %-15s%n",
                "Floor", "Total", "Occupied", "Occupancy %"));
        sb.append("-".repeat(60)).append("\n");

        @SuppressWarnings("unchecked")
        Map<Integer, Map<String, Object>> floorStats = (Map<Integer, Map<String, Object>>) stats.get("floorStats");

        for (int floor = 1; floor <= floorStats.size(); floor++) {
            Map<String, Object> floorData = floorStats.get(floor);
            sb.append(String.format("%-10d %-12d %-12d %-15.1f%n",
                    floor,
                    floorData.get("totalSpots"),
                    floorData.get("occupiedSpots"),
                    floorData.get("occupancyRate")));
        }

        sb.append("=".repeat(60)).append("\n");

        return sb.toString();
    }

    public String generateRevenueReport() {
        Map<String, Object> revenueStats = adminController.getRevenueStats();

        StringBuilder sb = new StringBuilder();
        sb.append("=".repeat(60)).append("\n");
        sb.append("REVENUE REPORT\n");
        sb.append("=".repeat(60)).append("\n\n");

        sb.append(String.format("Parking Revenue: RM %.2f%n", revenueStats.get("parkingRevenue")));
        sb.append(String.format("Fine Revenue: RM %.2f%n", revenueStats.get("fineRevenue")));
        sb.append("-".repeat(60)).append("\n");
        sb.append(String.format("Total Revenue: RM %.2f%n", revenueStats.get("totalRevenue")));
        sb.append("\n");
        sb.append(String.format("Total Transactions: %d%n", revenueStats.get("paymentCount")));
        sb.append(String.format("Average Transaction: RM %.2f%n", revenueStats.get("averageTransaction")));

        sb.append("=".repeat(60)).append("\n");

        return sb.toString();
    }

    public String generateFineReport() {
        Map<String, Object> fineStats = adminController.getFineStats();

        StringBuilder sb = new StringBuilder();
        sb.append("=".repeat(60)).append("\n");
        sb.append("OUTSTANDING FINES REPORT\n");
        sb.append("=".repeat(60)).append("\n\n");

        sb.append(String.format("Total Unpaid Fines: %d%n", fineStats.get("unpaidFineCount")));
        sb.append(String.format("Total Unpaid Amount: RM %.2f%n", fineStats.get("totalUnpaidAmount")));
        sb.append("\n");

        @SuppressWarnings("unchecked")
        List<Fine> unpaidFines = (List<Fine>) fineStats.get("unpaidFines");

        if (unpaidFines.isEmpty()) {
            sb.append("No outstanding fines.\n");
        } else {
            sb.append("Details:\n");
            sb.append("-".repeat(60)).append("\n");
            sb.append(String.format("%-15s %-15s %-12s %-15s%n",
                    "Plate", "Type", "Amount", "Scheme"));
            sb.append("-".repeat(60)).append("\n");

            for (Fine fine : unpaidFines) {
                sb.append(String.format("%-15s %-15s RM %-9.2f %-15s%n",
                        fine.getPlateNumber(),
                        fine.getFineType(),
                        fine.getFineAmount(),
                        fine.getFineScheme()));
            }
        }

        sb.append("=".repeat(60)).append("\n");

        return sb.toString();
    }

    public String generateCurrentlyParkedReport() {
        List<Ticket> parkedVehicles = adminController.getCurrentlyParkedVehicles();

        StringBuilder sb = new StringBuilder();
        sb.append("=".repeat(60)).append("\n");
        sb.append("CURRENTLY PARKED VEHICLES\n");
        sb.append("=".repeat(60)).append("\n\n");

        sb.append(String.format("Total Parked: %d%n", parkedVehicles.size()));
        sb.append("\n");

        if (parkedVehicles.isEmpty()) {
            sb.append("No vehicles currently parked.\n");
        } else {
            sb.append("-".repeat(60)).append("\n");
            sb.append(String.format("%-15s %-12s %-20s %-12s%n",
                    "Plate", "Spot", "Entry Time", "Fine Scheme"));
            sb.append("-".repeat(60)).append("\n");

            for (Ticket ticket : parkedVehicles) {
                sb.append(String.format("%-15s %-12s %-20s %-12s%n",
                        ticket.getPlateNumber(),
                        ticket.getSpotId(),
                        TimeUtil.formatForDisplay(ticket.getEntryTime()),
                        ticket.getFineScheme()));
            }
        }

        sb.append("=".repeat(60)).append("\n");

        return sb.toString();
    }

    public String generateReservationReport() {
        List<Reservation> activeReservations = adminController.getActiveReservations();

        StringBuilder sb = new StringBuilder();
        sb.append("=".repeat(60)).append("\n");
        sb.append("ACTIVE RESERVATIONS REPORT\n");
        sb.append("=".repeat(60)).append("\n\n");

        sb.append(String.format("Total Active Reservations: %d%n", activeReservations.size()));
        sb.append("\n");

        if (activeReservations.isEmpty()) {
            sb.append("No active reservations.\n");
        } else {
            sb.append("-".repeat(60)).append("\n");
            sb.append(String.format("%-10s %-12s %-15s %-20s%n",
                    "Res ID", "Spot", "Plate", "Reserved At"));
            sb.append("-".repeat(60)).append("\n");

            for (Reservation reservation : activeReservations) {
                sb.append(String.format("%-10d %-12s %-15s %-20s%n",
                        reservation.getReservationId(),
                        reservation.getSpotId(),
                        reservation.getPlateNumber() != null ? reservation.getPlateNumber() : "Unassigned",
                        TimeUtil.formatForDisplay(reservation.getReservedAt())));
            }
        }

        sb.append("=".repeat(60)).append("\n");

        return sb.toString();
    }

    public String generateSystemSummary() {
        Map<String, Object> systemStats = adminController.getSystemStats();

        StringBuilder sb = new StringBuilder();
        sb.append("=".repeat(60)).append("\n");
        sb.append("SYSTEM SUMMARY\n");
        sb.append("=".repeat(60)).append("\n\n");

        sb.append("OCCUPANCY:\n");
        sb.append(String.format("  Total Spots: %d%n", systemStats.get("totalSpots")));
        sb.append(String.format("  Currently Parked: %d%n", systemStats.get("currentlyParked")));
        sb.append(String.format("  Available: %d%n", systemStats.get("availableSpots")));
        sb.append(String.format("  Occupancy Rate: %.1f%%%n", systemStats.get("globalOccupancyRate")));
        sb.append("\n");

        @SuppressWarnings("unchecked")
        Map<String, Object> revenue = (Map<String, Object>) systemStats.get("revenue");
        sb.append("REVENUE:\n");
        sb.append(String.format("  Total Revenue: RM %.2f%n", revenue.get("totalRevenue")));
        sb.append(String.format("  Parking Revenue: RM %.2f%n", revenue.get("parkingRevenue")));
        sb.append(String.format("  Fine Revenue: RM %.2f%n", revenue.get("fineRevenue")));
        sb.append(String.format("  Total Transactions: %d%n", revenue.get("paymentCount")));
        sb.append("\n");

        @SuppressWarnings("unchecked")
        Map<String, Object> fines = (Map<String, Object>) systemStats.get("fines");
        sb.append("FINES:\n");
        sb.append(String.format("  Unpaid Fines: %d%n", fines.get("unpaidFineCount")));
        sb.append(String.format("  Unpaid Amount: RM %.2f%n", fines.get("totalUnpaidAmount")));
        sb.append("\n");

        sb.append("SYSTEM CONFIGURATION:\n");
        sb.append(String.format("  Current Fine Scheme: %s%n", systemStats.get("currentFineScheme")));
        sb.append(String.format("  Total Tickets Issued: %d%n", systemStats.get("totalTicketsIssued")));
        sb.append(String.format("  Active Reservations: %d%n", systemStats.get("activeReservations")));

        sb.append("=".repeat(60)).append("\n");

        return sb.toString();
    }
}
