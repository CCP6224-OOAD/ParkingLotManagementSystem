package parkinglotmanagementsystem.ui;

import parkinglotmanagementsystem.controller.*;
import parkinglotmanagementsystem.service.*;

import javax.swing.*;
import java.awt.*;

/**
 * Main application frame with tabbed interface
 * Contains panels for Entry, Exit, Admin, and Reports
 */
public class MainFrame extends JFrame {
    
    private JTabbedPane tabbedPane;
    private EntryPanel entryPanel;
    private ExitPanel exitPanel;
    private AdminPanel adminPanel;
    private ReportPanel reportPanel;
    
    // Controllers
    private EntryController entryController;
    private ExitController exitController;
    private AdminController adminController;
    private ReportController reportController;
    
    // Services (for observer pattern)
    private FineManager fineManager;
    private PaymentService paymentService;
    
    public MainFrame(EntryController entryController, ExitController exitController,
                    AdminController adminController, ReportController reportController,
                    FineManager fineManager, PaymentService paymentService) {
        this.entryController = entryController;
        this.exitController = exitController;
        this.adminController = adminController;
        this.reportController = reportController;
        this.fineManager = fineManager;
        this.paymentService = paymentService;
        
        initializeUI();
    }
    
    /**
     * Initializes the user interface
     */
    private void initializeUI() {
        setTitle("Parking Lot Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        
        // Create menu bar
        createMenuBar();
        
        // Create tabbed pane
        tabbedPane = new JTabbedPane();
        
        // Create panels with correct constructors
        entryPanel = new EntryPanel(entryController);
        exitPanel = new ExitPanel(exitController);
        adminPanel = new AdminPanel(adminController, reportController);
        reportPanel = new ReportPanel(reportController);
        
        // Register observers AFTER panel creation
        fineManager.addListener(adminPanel);
        fineManager.addListener(reportPanel);
        paymentService.addListener(adminPanel);
        paymentService.addListener(reportPanel);
        paymentService.addListener(exitPanel);
        
        // Add panels to tabbed pane
        tabbedPane.addTab("Vehicle Entry", entryPanel);
        tabbedPane.addTab("Vehicle Exit", exitPanel);
        tabbedPane.addTab("Admin", adminPanel);
        tabbedPane.addTab("Reports", reportPanel);
        
        // Add tabbed pane to frame
        add(tabbedPane, BorderLayout.CENTER);
        
        // Add status bar
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel statusLabel = new JLabel("Parking Lot Management System - Ready");
        statusBar.add(statusLabel);
        add(statusBar, BorderLayout.SOUTH);
    }
    
    /**
     * Creates the menu bar
     */
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // File menu
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
                JOptionPane.YES_NO_OPTION
            );
            if (confirm == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });
        fileMenu.add(exitItem);
        
        menuBar.add(fileMenu);
        
        // Help menu
        JMenu helpMenu = new JMenu("Help");
        
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);
        
        menuBar.add(helpMenu);
        
        setJMenuBar(menuBar);
    }
    
    /**
     * Refreshes all panels
     */
    private void refreshAllPanels() {
        entryPanel.refresh();
        exitPanel.refresh();
        adminPanel.refresh();
        reportPanel.refresh();
        JOptionPane.showMessageDialog(this, "All panels refreshed successfully!");
    }
    
    /**
     * Shows about dialog
     */
    private void showAboutDialog() {
        String message = """
            Parking Lot Management System
            Version 1.0
            
            Developed for CCP6224
            Object-Oriented Analysis and Design
            
            Features:
            - Multi-level parking management
            - Fine calculation strategies
            - Payment processing
            - Comprehensive reporting
            - Observer pattern for real-time updates
            """;
        
        JOptionPane.showMessageDialog(
            this,
            message,
            "About",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
}
