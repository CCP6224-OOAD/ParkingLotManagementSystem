package parkinglotmanagementsystem.main.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import parkinglotmanagementsystem.admin.controller.AdminController;
import parkinglotmanagementsystem.main.observer.ParkingEventListener;
import parkinglotmanagementsystem.main.observer.ParkingEventType;
import parkinglotmanagementsystem.parking.model.ParkingSpot;
import parkinglotmanagementsystem.parking.model.SpotStatus;
import parkinglotmanagementsystem.parking.model.SpotType;

public class ParkingLotPanel extends JPanel implements ParkingEventListener {
  AdminController adminController;

  // UI Components
  private JTable spotsTable;
  private DefaultTableModel spotsTableModel;
  private JLabel spotTypeLabel;
  private JComboBox<SpotType> spotTypeCombo;
  private JButton spotTypeButton;

  public ParkingLotPanel(AdminController adminController) {
    this.adminController = adminController;
    initializeUI();
    refresh();
  }

  private void initializeUI() {
    setLayout(new BorderLayout(10, 10));
    setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    JPanel tablePanel = createTablePanel();
    add(tablePanel, BorderLayout.NORTH);

    JPanel buttonsPanel = createButtonsPanel();
    add(buttonsPanel, BorderLayout.CENTER);

  }

  private JPanel createTablePanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createTitledBorder("Parking Spots"));

    String[] columns = { "Spot ID", "Floor", "Type", "Rate (RM/hr)", "Status", "Plate Number" };
    spotsTableModel = new DefaultTableModel(columns, 0) {
      @Override
      public boolean isCellEditable(int row, int column) {
        return false;
      }
    };

    spotsTable = new JTable(spotsTableModel);
    spotsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    spotsTable.getSelectionModel().addListSelectionListener(e -> {
      onSpotRowSelect();
    });

    JScrollPane scrollPane = new JScrollPane(spotsTable);
    panel.add(scrollPane, BorderLayout.CENTER);

    return panel;
  }

  private JPanel createButtonsPanel() {
    JPanel panel = new JPanel(new FlowLayout());

    spotTypeLabel = new JLabel("Spot Type:");
    panel.add(spotTypeLabel);

    spotTypeCombo = new JComboBox<>(SpotType.values());
    panel.add(spotTypeCombo);

    spotTypeButton = new JButton("Update");
    spotTypeButton.addActionListener(e -> updateSpot());
    spotTypeButton.setEnabled(false);
    panel.add(spotTypeButton);

    return panel;
  }

  private void onSpotRowSelect() {
    int selectedRow = spotsTable.getSelectedRow();

    if (selectedRow == -1) {
      spotTypeButton.setEnabled(false);
      return;
    }

    SpotStatus spotStatus = (SpotStatus) spotsTableModel.getValueAt(selectedRow, 4);
    if (spotStatus != SpotStatus.AVAILABLE) {
      spotTypeButton.setEnabled(false);
      return;
    }

    SpotType spotType = (SpotType) spotsTableModel.getValueAt(selectedRow, 2);
    spotTypeCombo.setSelectedItem(spotType);
    spotTypeButton.setEnabled(true);
  }

  private void updateSpot() {
    int selectedRow = spotsTable.getSelectedRow();

    if (selectedRow == -1) {
      return;
    }

    int confirm = JOptionPane.showConfirmDialog(this,
        String.format("Are you sure to change the spot type"),
        "Confirm Change Spot Type",
        JOptionPane.YES_NO_OPTION);

    if (confirm == JOptionPane.NO_OPTION) {
      return;
    }

    SpotType spotType = (SpotType) spotTypeCombo.getSelectedItem();
    String spotId = (String) spotsTableModel.getValueAt(selectedRow, 0);

    boolean isSuccess = adminController.updateSpotType(spotId, spotType);

    if (!isSuccess) {
      JOptionPane
          .showMessageDialog(this,
              "Failed to update the spot type of Parking Spot " + spotId + ".\nPlease try again later.");

    }
  }

  public void refresh() {
    // clear table
    spotsTableModel.setRowCount(0);

    List<ParkingSpot> spots = adminController.getParkingLot().findAllSpots();

    for (ParkingSpot spot : spots) {
      spotsTableModel.addRow(new Object[] {
          spot.getSpotId(),
          spot.getFloorNumber(),
          spot.getSpotType(),
          String.format("%.2f", spot.getHourlyRate()),
          spot.getStatus(),
          spot.getCurrentPlate(),
      });
    }
  }

  // Observer Pattern Implementation
  @Override
  public void onParkingEvent(ParkingEventType eventType, Object eventData) {
    switch (eventType) {
      case VEHICLE_ENTERED:
      case VEHICLE_EXITED:
      case SPOT_TYPE_CHANGED:
        refresh();
        break;
      default:
        break;
    }
  }
}
