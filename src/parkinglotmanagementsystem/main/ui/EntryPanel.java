package parkinglotmanagementsystem.main.ui;

import parkinglotmanagementsystem.main.util.PlateValidator;
import parkinglotmanagementsystem.main.util.TimeUtil;
import parkinglotmanagementsystem.parking.model.ParkingSpot;
import parkinglotmanagementsystem.parking.model.SpotType;
import parkinglotmanagementsystem.vehicleandticket.controller.EntryController;
import parkinglotmanagementsystem.vehicleandticket.model.Ticket;
import parkinglotmanagementsystem.vehicleandticket.model.VehicleType;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class EntryPanel extends JPanel {

  private EntryController entryController;

  // UI Components
  private JTextField plateField;
  private JComboBox<VehicleType> vehicleTypeCombo;
  private JButton searchSpotsButton;
  private JButton parkButton;
  private JTable spotsTable;
  private DefaultTableModel spotsTableModel;
  private JTextArea resultArea;

  public EntryPanel(EntryController entryController) {
    this.entryController = entryController;
    initializeUI();
  }

  private void initializeUI() {
    setLayout(new BorderLayout(10, 10));
    setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    // top panel - Input form
    JPanel inputPanel = createInputPanel();
    add(inputPanel, BorderLayout.NORTH);

    // center panel - Available spots table
    JPanel tablePanel = createTablePanel();
    add(tablePanel, BorderLayout.CENTER);

    // bottom panel - Result area
    JPanel resultPanel = createResultPanel();
    add(resultPanel, BorderLayout.SOUTH);
  }

  private JPanel createInputPanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBorder(BorderFactory.createTitledBorder("Vehicle Entry"));

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.fill = GridBagConstraints.HORIZONTAL;

    // Plate number
    gbc.gridx = 0;
    gbc.gridy = 0;
    panel.add(new JLabel("Plate Number:"), gbc);

    gbc.gridx = 1;
    plateField = new JTextField(15);
    panel.add(plateField, gbc);

    gbc.gridx = 2;
    JLabel formatLabel = new JLabel("(Format: ABC1234)");
    panel.add(formatLabel, gbc);

    // Vehicle type
    gbc.gridx = 0;
    gbc.gridy = 1;
    panel.add(new JLabel("Vehicle Type:"), gbc);

    gbc.gridx = 1;
    vehicleTypeCombo = new JComboBox<>(VehicleType.values());
    panel.add(vehicleTypeCombo, gbc);

    // Buttons
    gbc.gridx = 0;
    gbc.gridy = 2;
    gbc.gridwidth = 3;
    JPanel buttonPanel = new JPanel(new FlowLayout());

    searchSpotsButton = new JButton("Search Available Spots");
    searchSpotsButton.addActionListener(e -> searchAvailableSpots());
    buttonPanel.add(searchSpotsButton);

    parkButton = new JButton("Park Vehicle");
    parkButton.addActionListener(e -> parkVehicle());
    parkButton.setEnabled(false);
    buttonPanel.add(parkButton);

    JButton clearButton = new JButton("Clear");
    clearButton.addActionListener(e -> clearForm());
    buttonPanel.add(clearButton);

    panel.add(buttonPanel, gbc);

    return panel;
  }

  private JPanel createTablePanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createTitledBorder("Available Parking Spots"));

    String[] columns = { "Spot ID", "Floor", "Type", "Rate (RM/hr)" };
    spotsTableModel = new DefaultTableModel(columns, 0) {
      @Override
      public boolean isCellEditable(int row, int column) {
        return false;
      }
    };

    spotsTable = new JTable(spotsTableModel);
    spotsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    spotsTable.getSelectionModel().addListSelectionListener(e -> {
      parkButton.setEnabled(spotsTable.getSelectedRow() != -1);
    });

    JScrollPane scrollPane = new JScrollPane(spotsTable);
    panel.add(scrollPane, BorderLayout.CENTER);

    return panel;
  }

  private JPanel createResultPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createTitledBorder("Result"));

    resultArea = new JTextArea(8, 40);
    resultArea.setEditable(false);
    resultArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

    JScrollPane scrollPane = new JScrollPane(resultArea);
    panel.add(scrollPane, BorderLayout.CENTER);

    return panel;
  }

  private void searchAvailableSpots() {
    VehicleType selectedType = (VehicleType) vehicleTypeCombo.getSelectedItem();

    // clear table
    spotsTableModel.setRowCount(0);
    parkButton.setEnabled(false);

    // search for spots
    List<ParkingSpot> spots = entryController.findSuitableSpots(selectedType);

    if (spots.isEmpty()) {
      resultArea.setText("No available spots found for " + selectedType);
      JOptionPane.showMessageDialog(this,
          "No available spots found for " + selectedType,
          "No Spots Available",
          JOptionPane.WARNING_MESSAGE);
      return;
    }

    for (ParkingSpot spot : spots) {
      double hourlyRate = spot.getHourlyRate();

      if (selectedType == VehicleType.HANDICAPPED) {
        hourlyRate = spot.getSpotType() == SpotType.HANDICAPPED ? 0.0 : 2.0;
      }

      spotsTableModel.addRow(new Object[] {
          spot.getSpotId(),
          spot.getFloorNumber(),
          spot.getSpotType(),
          String.format("%.2f", hourlyRate),
      });
    }

    resultArea.setText(String.format("Found %d available spots for %s\nSelect a spot and click 'Park Vehicle'",
        spots.size(), selectedType));

  }

  private void parkVehicle() {
    // validate plate
    String plate = plateField.getText().trim();
    if (!PlateValidator.isValid(plate)) {
      JOptionPane.showMessageDialog(this,
          "Invalid plate number format!\nExpected: 3 letters + 4 digits (e.g., ABC1234)",
          "Invalid Input",
          JOptionPane.ERROR_MESSAGE);
      return;
    }

    int selectedRow = spotsTable.getSelectedRow();
    if (selectedRow == -1) {
      JOptionPane.showMessageDialog(this,
          "Please select a parking spot first!",
          "No Spot Selected",
          JOptionPane.WARNING_MESSAGE);
      return;
    }

    String spotId = (String) spotsTableModel.getValueAt(selectedRow, 0);
    VehicleType vehicleType = (VehicleType) vehicleTypeCombo.getSelectedItem();
    SpotType spotType = (SpotType) spotsTableModel.getValueAt(selectedRow, 2);

    System.out.println(spotsTableModel.getValueAt(selectedRow, 2));

    if (spotType == SpotType.RESERVED) {
      int confirm = JOptionPane.showConfirmDialog(this,
          String.format("Parking at %s spot will be fined. Do you want to continue?", spotType),
          "Confirm Parking At Reserved Spot",
          JOptionPane.YES_NO_OPTION);

      if (confirm == JOptionPane.NO_OPTION) {
        return;
      }
    }

    // park vehicle
    Ticket ticket = entryController.parkVehicle(plate, vehicleType, spotId);

    if (ticket != null) {
      // success
      String message = String.format("""
          === VEHICLE PARKED SUCCESSFULLY ===

          Ticket ID: %s
          Plate Number: %s
          Vehicle Type: %s
          Spot: %s
          Entry Time: %s
          Fine Scheme: %s

          Please keep your ticket for exit.
          """,
          ticket.getTicketId(),
          ticket.getPlateNumber(),
          vehicleType,
          ticket.getSpotId(),
          TimeUtil.formatForDisplay(ticket.getEntryTime()),
          ticket.getFineScheme());

      resultArea.setText(message);

      JOptionPane.showMessageDialog(this,
          "Vehicle parked successfully!\nTicket: " + ticket.getTicketId(),
          "Success",
          JOptionPane.INFORMATION_MESSAGE);

      clearForm();
    } else {
      // failed
      resultArea.setText("Failed to park vehicle. Please check console for details.");
      JOptionPane.showMessageDialog(this,
          "Failed to park vehicle!\nPlease check if the vehicle is already parked or spot is unavailable.",
          "Parking Failed",
          JOptionPane.ERROR_MESSAGE);
    }
  }

  private void clearForm() {
    plateField.setText("");
    vehicleTypeCombo.setSelectedIndex(0);
    spotsTableModel.setRowCount(0);
    resultArea.setText("");
    parkButton.setEnabled(false);
  }

  public void refresh() {
    clearForm();
  }
}
