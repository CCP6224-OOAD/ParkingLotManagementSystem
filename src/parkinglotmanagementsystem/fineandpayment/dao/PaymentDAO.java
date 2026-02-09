package parkinglotmanagementsystem.fineandpayment.dao;

import parkinglotmanagementsystem.fineandpayment.model.Payment;
import parkinglotmanagementsystem.fineandpayment.model.PaymentMethod;
import parkinglotmanagementsystem.main.dao.DatabaseManager;
import parkinglotmanagementsystem.main.util.TimeUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PaymentDAO {

    private Connection connection;

    public PaymentDAO() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    public boolean insertPayment(Payment payment) {
        String sql = """
                    INSERT INTO payments
                    (ticket_id, parking_fee, fine_amount, total_amount, payment_method, payment_time)
                    VALUES (?, ?, ?, ?, ?, ?);
                """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, payment.getTicketId());
            pstmt.setDouble(2, payment.getParkingFee());
            pstmt.setDouble(3, payment.getFineAmount());
            pstmt.setDouble(4, payment.getTotalAmount());
            pstmt.setString(5, payment.getPaymentMethod().name());
            pstmt.setString(6, TimeUtil.formatForDatabase(payment.getPaymentTime()));

            pstmt.executeUpdate();

            // Get generated payment ID
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                payment.setPaymentId(rs.getInt(1));
            }

            return true;
        } catch (SQLException e) {
            System.err.println("Failed to insert payment for ticket: " + payment.getTicketId());
            e.printStackTrace();
            return false;
        }
    }

    public Payment getPaymentByTicket(String ticketId) {
        String sql = "SELECT * FROM payments WHERE ticket_id = ?;";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, ticketId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractPaymentFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Failed to get payment for ticket: " + ticketId);
            e.printStackTrace();
        }

        return null;
    }

    public List<Payment> getAllPayments() {
        String sql = "SELECT * FROM payments ORDER BY payment_time DESC;";

        List<Payment> payments = new ArrayList<>();

        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                payments.add(extractPaymentFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Failed to get all payments");
            e.printStackTrace();
        }

        return payments;
    }

    public double getTotalParkingRevenue() {
        String sql = "SELECT SUM(parking_fee) FROM payments;";

        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            System.err.println("Failed to get total parking revenue");
            e.printStackTrace();
        }

        return 0.0;
    }

    public double getTotalFineRevenueFromPayments() {
        String sql = "SELECT SUM(fine_amount) FROM payments;";

        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            System.err.println("Failed to get total fine revenue from payments");
            e.printStackTrace();
        }

        return 0.0;
    }

    public double getTotalRevenue() {
        String sql = "SELECT SUM(total_amount) FROM payments;";

        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            System.err.println("Failed to get total revenue");
            e.printStackTrace();
        }

        return 0.0;
    }

    public int getTotalPaymentCount() {
        String sql = "SELECT COUNT(*) FROM payments;";

        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Failed to get payment count");
            e.printStackTrace();
        }

        return 0;
    }

    private Payment extractPaymentFromResultSet(ResultSet rs) throws SQLException {
        int paymentId = rs.getInt("payment_id");
        String ticketId = rs.getString("ticket_id");
        double parkingFee = rs.getDouble("parking_fee");
        double fineAmount = rs.getDouble("fine_amount");
        double totalAmount = rs.getDouble("total_amount");
        PaymentMethod paymentMethod = PaymentMethod.valueOf(rs.getString("payment_method"));
        LocalDateTime paymentTime = TimeUtil.parseFromDatabase(rs.getString("payment_time"));

        return new Payment(paymentId, ticketId, parkingFee, fineAmount,
                totalAmount, paymentMethod, paymentTime);
    }
}
