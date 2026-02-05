package parkinglotmanagementsystem.dao;

import parkinglotmanagementsystem.model.Reservation;
import parkinglotmanagementsystem.util.TimeUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {

    private Connection connection;

    public ReservationDAO() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    public boolean insertReservation(Reservation reservation) {
        String sql = """
                    INSERT INTO reservations
                    (spot_id, plate_number, reserved_at, is_active)
                    VALUES (?, ?, ?, ?);
                """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, reservation.getSpotId());
            pstmt.setString(2, reservation.getPlateNumber());
            pstmt.setString(3, TimeUtil.formatForDatabase(reservation.getReservedAt()));
            pstmt.setInt(4, reservation.isActive() ? 1 : 0);

            pstmt.executeUpdate();

            // Get generated reservation ID
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                reservation.setReservationId(rs.getInt(1));
            }

            return true;
        } catch (SQLException e) {
            System.err.println("Failed to insert reservation for spot: " + reservation.getSpotId());
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateReservation(Reservation reservation) {
        String sql = """
                    UPDATE reservations
                    SET plate_number = ?, is_active = ?
                    WHERE reservation_id = ?;
                """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, reservation.getPlateNumber());
            pstmt.setInt(2, reservation.isActive() ? 1 : 0);
            pstmt.setInt(3, reservation.getReservationId());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Failed to update reservation: " + reservation.getReservationId());
            e.printStackTrace();
            return false;
        }
    }

    public Reservation findActiveReservation(String spotId) {
        String sql = """
                    SELECT * FROM reservations
                    WHERE spot_id = ? AND is_active = 1
                    ORDER BY reserved_at DESC
                    LIMIT 1;
                """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, spotId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractReservationFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Failed to find active reservation for spot: " + spotId);
            e.printStackTrace();
        }

        return null;
    }

    public Reservation findReservationById(int reservationId) {
        String sql = "SELECT * FROM reservations WHERE reservation_id = ?;";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, reservationId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractReservationFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Failed to find reservation: " + reservationId);
            e.printStackTrace();
        }

        return null;
    }

    public List<Reservation> getAllActiveReservations() {
        String sql = """
                    SELECT * FROM reservations
                    WHERE is_active = 1
                    ORDER BY reserved_at DESC;
                """;

        List<Reservation> reservations = new ArrayList<>();

        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                reservations.add(extractReservationFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Failed to get all active reservations");
            e.printStackTrace();
        }

        return reservations;
    }

    public List<Reservation> getReservationsBySpot(String spotId) {
        String sql = """
                    SELECT * FROM reservations
                    WHERE spot_id = ?
                    ORDER BY reserved_at DESC;
                """;

        List<Reservation> reservations = new ArrayList<>();

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, spotId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                reservations.add(extractReservationFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Failed to get reservations for spot: " + spotId);
            e.printStackTrace();
        }

        return reservations;
    }

    public boolean cancelReservation(int reservationId) {
        String sql = "UPDATE reservations SET is_active = 0 WHERE reservation_id = ?;";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, reservationId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Failed to cancel reservation: " + reservationId);
            e.printStackTrace();
            return false;
        }
    }

    public int getActiveReservationCount() {
        String sql = "SELECT COUNT(*) FROM reservations WHERE is_active = 1;";

        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Failed to get active reservation count");
            e.printStackTrace();
        }

        return 0;
    }

    private Reservation extractReservationFromResultSet(ResultSet rs) throws SQLException {
        int reservationId = rs.getInt("reservation_id");
        String spotId = rs.getString("spot_id");
        String plateNumber = rs.getString("plate_number");
        LocalDateTime reservedAt = TimeUtil.parseFromDatabase(rs.getString("reserved_at"));
        boolean isActive = rs.getInt("is_active") == 1;

        return new Reservation(reservationId, spotId, plateNumber, reservedAt, isActive);
    }
}
