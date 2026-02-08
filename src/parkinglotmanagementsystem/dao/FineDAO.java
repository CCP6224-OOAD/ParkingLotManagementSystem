package parkinglotmanagementsystem.dao;

import parkinglotmanagementsystem.model.Fine;
import parkinglotmanagementsystem.model.FineScheme;
import parkinglotmanagementsystem.model.FineType;
import parkinglotmanagementsystem.util.TimeUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FineDAO {

    private Connection connection;

    public FineDAO() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    public boolean insertFine(Fine fine) {
        String sql = """
                    INSERT INTO fines
                    (plate_number, ticket_id, fine_type, fine_amount, fine_scheme, is_paid, created_at)
                    VALUES (?, ?, ?, ?, ?, ?, ?);
                """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, fine.getPlateNumber());
            pstmt.setString(2, fine.getTicketId());
            pstmt.setString(3, fine.getFineType().name());
            pstmt.setDouble(4, fine.getFineAmount());
            pstmt.setString(5, fine.getFineScheme().name());
            pstmt.setInt(6, fine.isPaid() ? 1 : 0);
            pstmt.setString(7, TimeUtil.formatForDatabase(fine.getCreatedAt()));

            pstmt.executeUpdate();

            // Get generated fine ID
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                fine.setFineId(rs.getInt(1));
            }

            return true;
        } catch (SQLException e) {
            System.err.println("Failed to insert fine for plate: " + fine.getPlateNumber());
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateFine(Fine fine) {
        String sql = """
                    UPDATE fines
                    SET plate_number = ? , ticket_id = ?, fine_type = ?, fine_amount = ?, fine_scheme = ?, is_paid = ?, created_at = ?
                    WHERE fine_id = ?;
                """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, fine.getPlateNumber());
            pstmt.setString(2, fine.getTicketId());
            pstmt.setString(3, fine.getFineType().name());
            pstmt.setDouble(4, fine.getFineAmount());
            pstmt.setString(5, fine.getFineScheme().name());
            pstmt.setInt(6, fine.isPaid() ? 1 : 0);
            pstmt.setString(7, TimeUtil.formatForDatabase(fine.getCreatedAt()));
            pstmt.setInt(8, fine.getFineId());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Failed to update fine for plate: " + fine.getPlateNumber());
            e.printStackTrace();
            return false;
        }
    }

    public Fine getFineByTicketIdAndFineType(String ticket, FineType fineType) {
        String sql = """
                    SELECT * FROM fines
                    WHERE ticket_id = ? AND fine_type = ?;
                """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, ticket);
            pstmt.setString(2, fineType.toString());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractFineFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Failed to get unpaid fines for: " + ticket);
            e.printStackTrace();
        }

        return null;
    }

    public List<Fine> getUnpaidFines(String plateNumber) {
        String sql = """
                    SELECT * FROM fines
                    WHERE plate_number = ? AND is_paid = 0
                    ORDER BY created_at ASC;
                """;

        List<Fine> fines = new ArrayList<>();

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, plateNumber);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                fines.add(extractFineFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Failed to get unpaid fines for: " + plateNumber);
            e.printStackTrace();
        }

        return fines;
    }

    public List<Fine> getAllFines(String plateNumber) {
        String sql = """
                    SELECT * FROM fines
                    WHERE plate_number = ?
                    ORDER BY created_at DESC;
                """;

        List<Fine> fines = new ArrayList<>();

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, plateNumber);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                fines.add(extractFineFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Failed to get fines for: " + plateNumber);
            e.printStackTrace();
        }

        return fines;
    }

    public List<Fine> getAllUnpaidFines() {
        String sql = """
                    SELECT * FROM fines
                    WHERE is_paid = 0
                    ORDER BY created_at DESC;
                """;

        List<Fine> fines = new ArrayList<>();

        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                fines.add(extractFineFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Failed to get all unpaid fines");
            e.printStackTrace();
        }

        return fines;
    }

    public boolean markFinesPaid(List<Integer> fineIds) {
        if (fineIds == null || fineIds.isEmpty()) {
            return true; // Nothing to mark
        }

        // Build SQL with placeholders
        String placeholders = String.join(",", "?".repeat(fineIds.size()).split(""));
        String sql = "UPDATE fines SET is_paid = 1 WHERE fine_id IN (" + placeholders + ");";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < fineIds.size(); i++) {
                pstmt.setInt(i + 1, fineIds.get(i));
            }

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Failed to mark fines as paid");
            e.printStackTrace();
            return false;
        }
    }

    public boolean markAllFinesPaidForPlate(String plateNumber) {
        String sql = "UPDATE fines SET is_paid = 1 WHERE plate_number = ? AND is_paid = 0;";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, plateNumber);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Failed to mark fines as paid for: " + plateNumber);
            e.printStackTrace();
            return false;
        }
    }

    public double getTotalUnpaidFineAmount(String plateNumber) {
        List<Fine> unpaidFines = getUnpaidFines(plateNumber);
        return unpaidFines.stream()
                .mapToDouble(Fine::getFineAmount)
                .sum();
    }

    public double getTotalFineRevenue() {
        String sql = "SELECT SUM(fine_amount) FROM fines WHERE is_paid = 1;";

        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            System.err.println("Failed to get total fine revenue");
            e.printStackTrace();
        }

        return 0.0;
    }

    private Fine extractFineFromResultSet(ResultSet rs) throws SQLException {
        int fineId = rs.getInt("fine_id");
        String plateNumber = rs.getString("plate_number");
        String ticketId = rs.getString("ticket_id");
        FineType fineType = FineType.valueOf(rs.getString("fine_type"));
        double fineAmount = rs.getDouble("fine_amount");
        FineScheme fineScheme = FineScheme.valueOf(rs.getString("fine_scheme"));
        boolean isPaid = rs.getInt("is_paid") == 1;
        LocalDateTime createdAt = TimeUtil.parseFromDatabase(rs.getString("created_at"));

        return new Fine(fineId, plateNumber, ticketId, fineType, fineAmount,
                fineScheme, isPaid, createdAt);
    }
}
