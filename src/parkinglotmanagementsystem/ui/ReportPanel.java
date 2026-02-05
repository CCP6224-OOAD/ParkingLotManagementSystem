package parkinglotmanagementsystem.ui;

import parkinglotmanagementsystem.controller.ReportController;
import parkinglotmanagementsystem.observer.ParkingEventListener;
import parkinglotmanagementsystem.observer.ParkingEventType;

import javax.swing.*;
import java.awt.*;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 * Report panel with Observer pattern for auto-refresh
 * Displays various system reports
 */
public class ReportPanel extends JPanel implements ParkingEventListener {

    private ReportController reportController;

    // UI Components
    private JComboBox<String> reportTypeCombo;
    private JTextArea reportArea;
    private JButton generateButton;
    private JButton refreshButton;

    public ReportPanel(ReportController reportController) {
        this.reportController = reportController;

        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top panel - Report selection
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);

        // Center panel - Report display
        JPanel centerPanel = createCenterPanel();
        add(centerPanel, BorderLayout.CENTER);

        // Bottom panel - Actions
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("Report Selection"));

        panel.add(new JLabel("Select Report:"));

        String[] reportTypes = {
                "System Summary",
                "Occupancy Report",
                "Revenue Report",
                "Fine Report",
                "Currently Parked Vehicles",
                "Reservations Report"
        };

        reportTypeCombo = new JComboBox<>(reportTypes);
        panel.add(reportTypeCombo);

        generateButton = new JButton("Generate Report");
        generateButton.addActionListener(e -> generateReport());
        panel.add(generateButton);

        return panel;
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Report Display"));

        reportArea = new JTextArea(25, 70);
        reportArea.setEditable(false);
        reportArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        reportArea.setText("Select a report type and click 'Generate Report' to view.");

        JScrollPane scrollPane = new JScrollPane(reportArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refresh());
        panel.add(refreshButton);

        JButton printButton = new JButton("Print Report");
        printButton.addActionListener(e -> printReport());
        panel.add(printButton);

        return panel;
    }

    private void generateReport() {
        String selectedReport = (String) reportTypeCombo.getSelectedItem();
        String report = "";

        try {
            switch (selectedReport) {
                case "System Summary":
                    report = reportController.generateSystemSummary();
                    break;
                case "Occupancy Report":
                    report = reportController.generateOccupancyReport();
                    break;
                case "Revenue Report":
                    report = reportController.generateRevenueReport();
                    break;
                case "Fine Report":
                    report = reportController.generateFineReport();
                    break;
                case "Currently Parked Vehicles":
                    report = reportController.generateCurrentlyParkedReport();
                    break;
                case "Reservations Report":
                    report = reportController.generateReservationReport();
                    break;
                default:
                    report = "Unknown report type";
            }

            reportArea.setText(report);
            reportArea.setCaretPosition(0); // Scroll to top

        } catch (Exception e) {
            reportArea.setText("Error generating report:\n" + e.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Error generating report: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void printReport() {
        try {
            boolean complete = reportArea.print();
            if (complete) {
                JOptionPane.showMessageDialog(this,
                        "Report sent to printer successfully!",
                        "Print Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Printing was cancelled.",
                        "Print Cancelled",
                        JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error printing report: " + e.getMessage(),
                    "Print Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void refresh() {
        // Auto-regenerate current report if one is displayed
        if (!reportArea.getText().equals("Select a report type and click 'Generate Report' to view.")) {
            generateReport();
        }
    }

    // Observer Pattern Implementation
    @Override
    public void onParkingEvent(ParkingEventType eventType, Object eventData) {
        // Auto-refresh on relevant events
        SwingUtilities.invokeLater(() -> {
            switch (eventType) {
                case VEHICLE_ENTERED:
                case VEHICLE_EXITED:
                case FINE_GENERATED:
                case FINE_PAID:
                case PAYMENT_PROCESSED:
                case REVENUE_UPDATED:
                    // Auto-refresh if a report is currently displayed
                    String currentText = reportArea.getText();
                    if (!currentText.isEmpty() &&
                            !currentText.equals("Select a report type and click 'Generate Report' to view.")) {
                        refresh();
                    }
                    break;
                default:
                    break;
            }
        });
    }
}
