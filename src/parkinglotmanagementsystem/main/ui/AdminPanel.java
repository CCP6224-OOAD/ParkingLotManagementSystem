package parkinglotmanagementsystem.main.ui;

import parkinglotmanagementsystem.admin.controller.AdminController;
import parkinglotmanagementsystem.fineandpayment.model.Fine;
import parkinglotmanagementsystem.fineandpayment.model.FineScheme;
import parkinglotmanagementsystem.main.observer.ParkingEventListener;
import parkinglotmanagementsystem.main.observer.ParkingEventType;
import parkinglotmanagementsystem.main.util.TimeUtil;
import parkinglotmanagementsystem.vehicleandticket.model.Ticket;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class AdminPanel extends JPanel implements ParkingEventListener {

    private AdminController adminController;

    // UI Components
    private JComboBox<FineScheme> fineSchemeCombo;
    private JLabel currentSchemeLabel;
    private JLabel occupancyLabel;
    private JLabel revenueLabel;
    private JLabel finesLabel;
    private JTable parkedVehiclesTable;
    private DefaultTableModel parkedTableModel;
    private JTable unpaidFinesTable;
    private DefaultTableModel finesTableModel;

    public AdminPanel(AdminController adminController) {
        this.adminController = adminController;

        initializeUI();
        refresh();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topPanel = createFineSchemePanel();
        add(topPanel, BorderLayout.NORTH);

        JPanel centerPanel = createCenterPanel();
        add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createFineSchemePanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("Fine Scheme Management"));

        panel.add(new JLabel("Current Fine Scheme:"));

        currentSchemeLabel = new JLabel("FIXED");
        currentSchemeLabel.setFont(currentSchemeLabel.getFont().deriveFont(Font.BOLD));
        panel.add(currentSchemeLabel);

        panel.add(Box.createHorizontalStrut(20));

        panel.add(new JLabel("Change to:"));

        fineSchemeCombo = new JComboBox<>(FineScheme.values());
        panel.add(fineSchemeCombo);

        JButton applyButton = new JButton("Apply Scheme");
        applyButton.addActionListener(e -> changeFineScheme());
        panel.add(applyButton);

        return panel;
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 10, 10));

        // Top section - Statistics
        JPanel statsPanel = createStatsPanel();
        panel.add(statsPanel);

        // Bottom section - Tables
        JPanel tablesPanel = createTablesPanel();
        panel.add(tablesPanel);

        return panel;
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("System Statistics"));

        // Occupancy stats
        JPanel occupancyPanel = new JPanel(new BorderLayout());
        occupancyPanel.setBorder(BorderFactory.createTitledBorder("Occupancy"));
        occupancyLabel = new JLabel("Loading...", SwingConstants.CENTER);
        occupancyLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        occupancyPanel.add(occupancyLabel, BorderLayout.CENTER);
        panel.add(occupancyPanel);

        // Revenue stats
        JPanel revenuePanel = new JPanel(new BorderLayout());
        revenuePanel.setBorder(BorderFactory.createTitledBorder("Revenue"));
        revenueLabel = new JLabel("Loading...", SwingConstants.CENTER);
        revenueLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        revenuePanel.add(revenueLabel, BorderLayout.CENTER);
        panel.add(revenuePanel);

        // Fines stats
        JPanel finesPanel = new JPanel(new BorderLayout());
        finesPanel.setBorder(BorderFactory.createTitledBorder("Unpaid Fines"));
        finesLabel = new JLabel("Loading...", SwingConstants.CENTER);
        finesLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        finesPanel.add(finesLabel, BorderLayout.CENTER);
        panel.add(finesPanel);

        return panel;
    }

    private JPanel createTablesPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 10, 10));

        // Parked vehicles table
        JPanel parkedPanel = new JPanel(new BorderLayout());
        parkedPanel.setBorder(BorderFactory.createTitledBorder("Currently Parked Vehicles"));

        String[] parkedColumns = { "Plate", "Spot", "Entry Time" };
        parkedTableModel = new DefaultTableModel(parkedColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        parkedVehiclesTable = new JTable(parkedTableModel);
        JScrollPane parkedScroll = new JScrollPane(parkedVehiclesTable);
        parkedPanel.add(parkedScroll, BorderLayout.CENTER);
        panel.add(parkedPanel);

        // Unpaid fines table
        JPanel finesPanel = new JPanel(new BorderLayout());
        finesPanel.setBorder(BorderFactory.createTitledBorder("Unpaid Fines"));

        String[] finesColumns = { "Plate", "Type", "Amount (RM)" };
        finesTableModel = new DefaultTableModel(finesColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        unpaidFinesTable = new JTable(finesTableModel);
        JScrollPane finesScroll = new JScrollPane(unpaidFinesTable);
        finesPanel.add(finesScroll, BorderLayout.CENTER);
        panel.add(finesPanel);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refresh());
        panel.add(refreshButton);

        return panel;
    }

    private void changeFineScheme() {
        FineScheme selectedScheme = (FineScheme) fineSchemeCombo.getSelectedItem();
        FineScheme currentScheme = adminController.getCurrentFineScheme();

        if (selectedScheme == currentScheme) {
            JOptionPane.showMessageDialog(this,
                    "The selected scheme is already active!",
                    "No Change",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                String.format("Change fine scheme from %s to %s?\n\n" +
                        "This will affect FUTURE vehicle entries only.\n" +
                        "Existing parked vehicles will retain their current scheme.",
                        currentScheme, selectedScheme),
                "Confirm Scheme Change",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = adminController.changeFineScheme(selectedScheme);
            if (success) {
                currentSchemeLabel.setText(selectedScheme.toString());
                JOptionPane.showMessageDialog(this,
                        "Fine scheme changed successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to change fine scheme!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void refresh() {
        // Update current scheme
        FineScheme currentScheme = adminController.getCurrentFineScheme();
        currentSchemeLabel.setText(currentScheme.toString());
        fineSchemeCombo.setSelectedItem(currentScheme);

        // Update statistics
        updateStatistics();

        // Update tables
        updateTables();
    }

    private void updateStatistics() {
        Map<String, Object> stats = adminController.getOccupancyStats();
        Map<String, Object> revenueStats = adminController.getRevenueStats();
        Map<String, Object> fineStats = adminController.getFineStats();

        String occupancyText = String.format("<html><center>%d / %d spots<br>%.1f%%</center></html>",
                stats.get("occupiedSpots"),
                stats.get("totalSpots"),
                stats.get("globalOccupancyRate"));
        occupancyLabel.setText(occupancyText);

        String revenueText = String.format("<html><center>RM %.2f<br>(%d transactions)</center></html>",
                revenueStats.get("totalRevenue"),
                revenueStats.get("paymentCount"));
        revenueLabel.setText(revenueText);

        String finesText = String.format("<html><center>%d fines<br>RM %.2f</center></html>",
                fineStats.get("unpaidFineCount"),
                fineStats.get("totalUnpaidAmount"));
        finesLabel.setText(finesText);
    }

    private void updateTables() {
        // Update parked vehicles
        parkedTableModel.setRowCount(0);
        List<Ticket> parkedVehicles = adminController.getCurrentlyParkedVehicles();
        for (Ticket ticket : parkedVehicles) {
            parkedTableModel.addRow(new Object[] {
                    ticket.getPlateNumber(),
                    ticket.getSpotId(),
                    TimeUtil.formatForDisplay(ticket.getEntryTime())
            });
        }

        // Update unpaid fines
        finesTableModel.setRowCount(0);
        List<Fine> unpaidFines = adminController.getAllUnpaidFines();
        for (Fine fine : unpaidFines) {
            finesTableModel.addRow(new Object[] {
                    fine.getPlateNumber(),
                    fine.getFineType(),
                    String.format("%.2f", fine.getFineAmount())
            });
        }
    }

    // Observer Pattern Implementation
    @Override
    public void onParkingEvent(ParkingEventType eventType, Object eventData) {
        switch (eventType) {
            case FINE_GENERATED:
            case FINE_PAID:
            case PAYMENT_PROCESSED:
            case REVENUE_UPDATED:
            case VEHICLE_ENTERED:
            case VEHICLE_EXITED:
                refresh();
                break;
            default:
                break;
        }
    }
}
