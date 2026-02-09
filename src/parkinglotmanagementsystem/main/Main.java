package parkinglotmanagementsystem.main;

import parkinglotmanagementsystem.admin.controller.AdminController;
import parkinglotmanagementsystem.fineandpayment.service.*;
import parkinglotmanagementsystem.main.ui.MainFrame;
import parkinglotmanagementsystem.parking.service.ParkingService;
import parkinglotmanagementsystem.report.controller.ReportController;
import parkinglotmanagementsystem.vehicleandticket.controller.EntryController;
import parkinglotmanagementsystem.vehicleandticket.controller.ExitController;

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

        // Initialize services
        ParkingService parkingService = new ParkingService();
        parkingService.initializeParkingLot();

        FineManager fineManager = new FineManager();
        PaymentService paymentService = new PaymentService(fineManager);

        // Initialize controllers
        AdminController adminController = new AdminController(parkingService, fineManager, paymentService);
        ReportController reportController = new ReportController(adminController);
        EntryController entryController = new EntryController(parkingService, fineManager);
        ExitController exitController = new ExitController(parkingService, fineManager, paymentService);

        // Create and show main frame
        MainFrame frame = new MainFrame(entryController, exitController,
                adminController, reportController,
                fineManager, paymentService, parkingService);
        frame.setVisible(true);
    }

}
