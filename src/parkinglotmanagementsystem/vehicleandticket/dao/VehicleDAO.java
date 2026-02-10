package parkinglotmanagementsystem.vehicleandticket.dao;

import parkinglotmanagementsystem.main.dao.DatabaseManager;
import parkinglotmanagementsystem.vehicleandticket.model.Car;
import parkinglotmanagementsystem.vehicleandticket.model.HandicappedVehicle;
import parkinglotmanagementsystem.vehicleandticket.model.Motorcycle;
import parkinglotmanagementsystem.vehicleandticket.model.SUV;
import parkinglotmanagementsystem.vehicleandticket.model.Vehicle;
import parkinglotmanagementsystem.vehicleandticket.model.VehicleType;

import java.sql.*;

public class VehicleDAO {

  private Connection connection;

  public VehicleDAO() {
    this.connection = DatabaseManager.getInstance().getConnection();
  }

  public boolean insertVehicle(Vehicle vehicle) {
    String sql = "INSERT INTO vehicles (plate_number, vehicle_type) VALUES (?, ?);";

    try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
      pstmt.setString(1, vehicle.getPlateNumber());
      pstmt.setString(2, vehicle.getVehicleType().name());

      pstmt.executeUpdate();
      return true;
    } catch (SQLException e) {
      // Check if vehicle already exists (duplicate key)
      if (e.getMessage().contains("UNIQUE constraint")) {
        System.out.println("Vehicle already registered: " + vehicle.getPlateNumber());
        return true; // Not an error, just already exists
      }
      System.err.println("Failed to insert vehicle: " + vehicle.getPlateNumber());
      e.printStackTrace();
      return false;
    }
  }

  public boolean updateVehicle(Vehicle vehicle) {
    String sql = """
        UPDATE vehicles
        SET vehicle_type = ?, balance = ?
        WHERE plate_number = ?;
        """;

    try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
      pstmt.setString(1, vehicle.getVehicleType().name());
      pstmt.setDouble(2, vehicle.getBalance());
      pstmt.setString(3, vehicle.getPlateNumber());

      int rowsAffected = pstmt.executeUpdate();
      return rowsAffected > 0;
    } catch (SQLException e) {
      System.err.println("Failed to update vehicle for plate: " + vehicle.getPlateNumber());
      e.printStackTrace();
      return false;
    }
  }

  public Vehicle findVehicleByPlate(String plateNumber) {
    String sql = "SELECT * FROM vehicles WHERE plate_number = ?;";

    try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
      pstmt.setString(1, plateNumber);
      ResultSet rs = pstmt.executeQuery();

      if (rs.next()) {
        return extractVehicleFromResultSet(rs);
      }
    } catch (SQLException e) {
      System.err.println("Failed to find vehicle: " + plateNumber);
      e.printStackTrace();
    }

    return null;
  }

  public boolean vehicleExists(String plateNumber) {
    return findVehicleByPlate(plateNumber) != null;
  }

  public boolean deleteVehicle(String plateNumber) {
    String sql = "DELETE FROM vehicles WHERE plate_number = ?;";

    try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
      pstmt.setString(1, plateNumber);
      int rowsAffected = pstmt.executeUpdate();
      return rowsAffected > 0;
    } catch (SQLException e) {
      System.err.println("Failed to delete vehicle: " + plateNumber);
      e.printStackTrace();
      return false;
    }
  }

  public int getTotalVehicleCount() {
    String sql = "SELECT COUNT(*) FROM vehicles;";

    try (Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql)) {

      if (rs.next()) {
        return rs.getInt(1);
      }
    } catch (SQLException e) {
      System.err.println("Failed to get vehicle count");
      e.printStackTrace();
    }

    return 0;
  }

  private Vehicle extractVehicleFromResultSet(ResultSet rs) throws SQLException {
    String plateNumber = rs.getString("plate_number");
    VehicleType vehicleType = VehicleType.valueOf(rs.getString("vehicle_type"));
    double balance = rs.getDouble("balance");

    // Create appropriate subclass based on vehicle type
    switch (vehicleType) {
      case MOTORCYCLE:
        return new Motorcycle(plateNumber, balance);
      case CAR:
        return new Car(plateNumber, balance);
      case SUV:
        return new SUV(plateNumber, balance);
      case HANDICAPPED:
        return new HandicappedVehicle(plateNumber, balance);
      default:
        throw new IllegalArgumentException("Unknown vehicle type: " + vehicleType);
    }
  }
}
