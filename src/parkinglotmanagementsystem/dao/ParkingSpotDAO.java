package parkinglotmanagementsystem.dao;

import parkinglotmanagementsystem.model.ParkingSpot;
import parkinglotmanagementsystem.model.SpotType;
import parkinglotmanagementsystem.model.SpotStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ParkingSpotDAO {

    private Connection connection;

    public ParkingSpotDAO() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    public boolean insertSpot(ParkingSpot spot) {
        String sql = """
                    INSERT INTO parking_spots
                    (spot_id, floor_number, row_number, spot_number, spot_type, hourly_rate, is_occupied, current_plate)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?);
                """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, spot.getSpotId());
            pstmt.setInt(2, spot.getFloorNumber());
            pstmt.setInt(3, spot.getRowNumber());
            pstmt.setInt(4, spot.getSpotNumber());
            pstmt.setString(5, spot.getSpotType().name());
            pstmt.setDouble(6, spot.getHourlyRate());
            pstmt.setInt(7, spot.getStatus() == SpotStatus.OCCUPIED ? 1 : 0);
            pstmt.setString(8, spot.getCurrentPlate());

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Failed to insert spot: " + spot.getSpotId());
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateSpotStatus(String spotId, SpotStatus status, String plateNumber) {
        String sql = """
                    UPDATE parking_spots
                    SET is_occupied = ?, current_plate = ?
                    WHERE spot_id = ?;
                """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, status == SpotStatus.OCCUPIED ? 1 : 0);
            pstmt.setString(2, plateNumber);
            pstmt.setString(3, spotId);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Failed to update spot status: " + spotId);
            e.printStackTrace();
            return false;
        }
    }

    public ParkingSpot findSpotById(String spotId) {
        String sql = "SELECT * FROM parking_spots WHERE spot_id = ?;";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, spotId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractSpotFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Failed to find spot: " + spotId);
            e.printStackTrace();
        }
        return null;
    }

    public List<ParkingSpot> findAvailableSpots(SpotType spotType) {
        String sql = """
                    SELECT * FROM parking_spots
                    WHERE spot_type = ? AND is_occupied = 0
                    ORDER BY floor_number, row_number, spot_number;
                """;

        List<ParkingSpot> spots = new ArrayList<>();

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, spotType.name());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                spots.add(extractSpotFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Failed to find available spots of type: " + spotType);
            e.printStackTrace();
        }

        return spots;
    }

    public List<ParkingSpot> getSpotsByFloor(int floorNumber) {
        String sql = """
                    SELECT * FROM parking_spots
                    WHERE floor_number = ?
                    ORDER BY row_number, spot_number;
                """;

        List<ParkingSpot> spots = new ArrayList<>();

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, floorNumber);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                spots.add(extractSpotFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Failed to get spots for floor: " + floorNumber);
            e.printStackTrace();
        }

        return spots;
    }

    public List<ParkingSpot> getAllSpots() {
        String sql = "SELECT * FROM parking_spots ORDER BY floor_number, row_number, spot_number;";

        List<ParkingSpot> spots = new ArrayList<>();

        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                spots.add(extractSpotFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Failed to get all spots");
            e.printStackTrace();
        }

        return spots;
    }

    public int getOccupiedCount() {
        String sql = "SELECT COUNT(*) FROM parking_spots WHERE is_occupied = 1;";

        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Failed to get occupied count");
            e.printStackTrace();
        }

        return 0;
    }

    public int getTotalCount() {
        String sql = "SELECT COUNT(*) FROM parking_spots;";

        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Failed to get total count");
            e.printStackTrace();
        }

        return 0;
    }

    private ParkingSpot extractSpotFromResultSet(ResultSet rs) throws SQLException {
        String spotId = rs.getString("spot_id");
        int floorNumber = rs.getInt("floor_number");
        int rowNumber = rs.getInt("row_number");
        int spotNumber = rs.getInt("spot_number");
        SpotType spotType = SpotType.valueOf(rs.getString("spot_type"));
        double hourlyRate = rs.getDouble("hourly_rate");
        SpotStatus status = rs.getInt("is_occupied") == 1 ? SpotStatus.OCCUPIED : SpotStatus.AVAILABLE;
        String currentPlate = rs.getString("current_plate");

        return new ParkingSpot(spotId, floorNumber, rowNumber, spotNumber,
                spotType, hourlyRate, status, currentPlate);
    }
}
