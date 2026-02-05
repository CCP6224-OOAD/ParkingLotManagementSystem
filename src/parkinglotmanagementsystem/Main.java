package parkinglotmanagementsystem;

import parkinglotmanagementsystem.controller.*;
import parkinglotmanagementsystem.service.*;
import parkinglotmanagementsystem.ui.MainFrame;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        System.out.println("=".repeat(60));
        System.out.println("PARKING LOT MANAGEMENT SYSTEM - GUI MODE");
        System.out.println("=".repeat(60));
        System.out.println("Launching graphical user interface...");
        System.out.println();

        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Launch GUI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            // Initialize services
            ParkingService parkingService = new ParkingService();
            parkingService.initializeParkingLot();

            FineManager fineManager = new FineManager();
            PaymentService paymentService = new PaymentService(fineManager);
            ReservationService reservationService = new ReservationService(parkingService);

            // Initialize controllers
            AdminController adminController = new AdminController(fineManager, paymentService, reservationService);
            ReportController reportController = new ReportController(adminController);
            EntryController entryController = new EntryController(reservationService, fineManager);
            ExitController exitController = new ExitController(fineManager, paymentService);

            // Create and show main frame
            MainFrame frame = new MainFrame(entryController, exitController,
                    adminController, reportController,
                    fineManager, paymentService);
            frame.setVisible(true);
        });
    }

}
