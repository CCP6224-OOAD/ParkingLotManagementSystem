package parkinglotmanagementsystem.ui;

import parkinglotmanagementsystem.controller.*;
import parkinglotmanagementsystem.service.*;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private JTabbedPane tabbedPane;
    private EntryPanel entryPanel;
    private ExitPanel exitPanel;
    private AdminPanel adminPanel;
    private ParkingLotPanel parkingLotPanel;
    private ReportPanel reportPanel;

    // controllers
    private EntryController entryController;
    private ExitController exitController;
    private AdminController adminController;
    private ReportController reportController;

    // services (for observer pattern)
    private FineManager fineManager;
    private PaymentService paymentService;
    private ParkingService parkingService;

    public MainFrame(EntryController entryController, ExitController exitController,
            AdminController adminController, ReportController reportController,
            FineManager fineManager, PaymentService paymentService, ParkingService parkingService) {
        this.entryController = entryController;
        this.exitController = exitController;
        this.adminController = adminController;
        this.reportController = reportController;
        this.fineManager = fineManager;
        this.paymentService = paymentService;
        this.parkingService = parkingService;

        initializeUI();
    }

    private void initializeUI() {
        setTitle("Parking Lot Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        // create menu bar
        createMenuBar();

        // create tabbed pane
        tabbedPane = new JTabbedPane();

        entryPanel = new EntryPanel(entryController);
        exitPanel = new ExitPanel(exitController);
        adminPanel = new AdminPanel(adminController);
        parkingLotPanel = new ParkingLotPanel(adminController);
        reportPanel = new ReportPanel(reportController);

        // register observers AFTER panel creation
        fineManager.addListener(adminPanel);
        fineManager.addListener(reportPanel);
        paymentService.addListener(adminPanel);
        paymentService.addListener(reportPanel);
        paymentService.addListener(exitPanel);
        parkingService.addListener(adminPanel);
        parkingService.addListener(parkingLotPanel);

        tabbedPane.addTab("Vehicle Entry", entryPanel);
        tabbedPane.addTab("Vehicle Exit", exitPanel);
        tabbedPane.addTab("Admin", adminPanel);
        tabbedPane.addTab("Parking Lot", parkingLotPanel);
        tabbedPane.addTab("Reports", reportPanel);

        add(tabbedPane, BorderLayout.CENTER);

    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");

        JMenuItem refreshItem = new JMenuItem("Refresh All");
        refreshItem.addActionListener(e -> refreshAllPanels());
        fileMenu.add(refreshItem);

        fileMenu.addSeparator();

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to exit?",
                    "Confirm Exit",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });
        fileMenu.add(exitItem);

        menuBar.add(fileMenu);

        setJMenuBar(menuBar);
    }

    private void refreshAllPanels() {
        entryPanel.refresh();
        exitPanel.refresh();
        adminPanel.refresh();
        parkingLotPanel.refresh();
        reportPanel.refresh();
        JOptionPane.showMessageDialog(this, "All panels refreshed successfully!");
    }

}
