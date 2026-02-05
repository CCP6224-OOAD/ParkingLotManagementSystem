package parkinglotmanagementsystem.ui;

import parkinglotmanagementsystem.controller.ExitController;
import parkinglotmanagementsystem.model.*;
import parkinglotmanagementsystem.observer.ParkingEventListener;
import parkinglotmanagementsystem.observer.ParkingEventType;
import parkinglotmanagementsystem.util.PlateValidator;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class ExitPanel extends JPanel implements ParkingEventListener {

  private ExitController exitController;

  // UI Components
  private JTextField plateField;
  private JButton calculateBillButton;
  private JButton processPaymentButton;
  private JTextArea billArea;
  private JRadioButton cashRadio;
  private JRadioButton cardRadio;

  private Map<String, Object> currentBill;

  public ExitPanel(ExitController exitController) {
    this.exitController = exitController;
    initializeUI();
  }

  private void initializeUI() {
    setLayout(new BorderLayout(10, 10));
    setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    // Top panel - Input form
    JPanel inputPanel = createInputPanel();
    add(inputPanel, BorderLayout.NORTH);

    // Center panel - Bill display
    JPanel billPanel = createBillPanel();
    add(billPanel, BorderLayout.CENTER);

    // Bottom panel - Payment options
    JPanel paymentPanel = createPaymentPanel();
    add(paymentPanel, BorderLayout.SOUTH);
  }

  private JPanel createInputPanel() {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    panel.setBorder(BorderFactory.createTitledBorder("Vehicle Exit"));

    panel.add(new JLabel("Plate Number:"));

    plateField = new JTextField(15);
    panel.add(plateField);

    JLabel formatLabel = new JLabel("(Format: ABC1234)");
    formatLabel.setFont(formatLabel.getFont().deriveFont(Font.ITALIC));
    panel.add(formatLabel);

    calculateBillButton = new JButton("Calculate Bill");
    calculateBillButton.addActionListener(e -> calculateBill());
    panel.add(calculateBillButton);

    JButton clearButton = new JButton("Clear");
    clearButton.addActionListener(e -> clearForm());
    panel.add(clearButton);

    return panel;
  }

  private JPanel createBillPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createTitledBorder("Parking Bill"));

    billArea = new JTextArea(15, 50);
    billArea.setEditable(false);
    billArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

    JScrollPane scrollPane = new JScrollPane(billArea);
    panel.add(scrollPane, BorderLayout.CENTER);

    return panel;
  }

  private JPanel createPaymentPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createTitledBorder("Payment"));

    // Payment method selection
    JPanel methodPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    methodPanel.add(new JLabel("Payment Method:"));

    cashRadio = new JRadioButton("Cash", true);
    cardRadio = new JRadioButton("Card");

    ButtonGroup paymentGroup = new ButtonGroup();
    paymentGroup.add(cashRadio);
    paymentGroup.add(cardRadio);

    methodPanel.add(cashRadio);
    methodPanel.add(cardRadio);

    processPaymentButton = new JButton("Process Payment & Exit");
    processPaymentButton.addActionListener(e -> processPayment());
    processPaymentButton.setEnabled(false);
    methodPanel.add(processPaymentButton);

    panel.add(methodPanel, BorderLayout.CENTER);

    return panel;
  }

  private void calculateBill() {
    // Validate plate
    String plate = plateField.getText().trim();
    if (!PlateValidator.isValid(plate)) {
      JOptionPane.showMessageDialog(this,
          "Invalid plate number format!\nExpected: 3 letters + 4 digits (e.g., ABC1234)",
          "Invalid Input",
          JOptionPane.ERROR_MESSAGE);
      return;
    }

    // Calculate bill
    currentBill = exitController.calculateBill(plate);

    if (currentBill.containsKey("error")) {
      billArea.setText("ERROR: " + currentBill.get("error"));
      processPaymentButton.setEnabled(false);
      JOptionPane.showMessageDialog(this,
          currentBill.get("error"),
          "Error",
          JOptionPane.ERROR_MESSAGE);
      return;
    }

    // Display bill
    String billSummary = exitController.getBillSummary(currentBill);
    billArea.setText(billSummary);
    processPaymentButton.setEnabled(true);
  }

  private void processPayment() {
    if (currentBill == null || currentBill.containsKey("error")) {
      JOptionPane.showMessageDialog(this,
          "Please calculate the bill first!",
          "No Bill",
          JOptionPane.WARNING_MESSAGE);
      return;
    }

    // Get payment method
    PaymentMethod paymentMethod = cashRadio.isSelected() ? PaymentMethod.CASH : PaymentMethod.CARD;

    // Confirm payment
    double totalDue = (Double) currentBill.get("totalDue");
    int confirm = JOptionPane.showConfirmDialog(this,
        String.format("Confirm payment of RM %.2f via %s?", totalDue, paymentMethod),
        "Confirm Payment",
        JOptionPane.YES_NO_OPTION);

    if (confirm != JOptionPane.YES_OPTION) {
      return;
    }

    // Process payment
    String plate = plateField.getText().trim();
    Payment payment = exitController.processExit(plate, paymentMethod);

    if (payment != null) {
      // Success - generate receipt
      String receipt = exitController.generateReceipt(payment, currentBill);
      billArea.setText(receipt);

      JOptionPane.showMessageDialog(this,
          String.format("Payment successful!\nTotal Paid: RM %.2f\nThank you!",
              payment.getTotalAmount()),
          "Payment Successful",
          JOptionPane.INFORMATION_MESSAGE);

      // Clear form
      clearForm();
    } else {
      billArea.setText("Payment processing failed. Please try again.");
      JOptionPane.showMessageDialog(this,
          "Payment processing failed!\nPlease contact support.",
          "Payment Failed",
          JOptionPane.ERROR_MESSAGE);
    }
  }

  private void clearForm() {
    plateField.setText("");
    billArea.setText("");
    currentBill = null;
    processPaymentButton.setEnabled(false);
    cashRadio.setSelected(true);
  }

  public void refresh() {
    clearForm();
  }

  // Observer Pattern Implementation
  @Override
  public void onParkingEvent(ParkingEventType eventType, Object eventData) {
    // Auto-clear form after successful payment
    SwingUtilities.invokeLater(() -> {
      if (eventType == ParkingEventType.PAYMENT_PROCESSED) {
        // Form is already cleared in processPayment success handler
      }
    });
  }
}
