package parkinglotmanagementsystem.service;

import parkinglotmanagementsystem.dao.VehicleDAO;
import parkinglotmanagementsystem.model.*;
import parkinglotmanagementsystem.util.PlateValidator;

public class VehicleService {

  private VehicleDAO vehicleDAO;

  public VehicleService() {
    this.vehicleDAO = new VehicleDAO();
  }

  public Vehicle registerVehicle(String plateNumber, VehicleType vehicleType) {
    String normalizedPlate = PlateValidator.validateAndNormalize(plateNumber);

    Vehicle existingVehicle = vehicleDAO.findVehicleByPlate(normalizedPlate);
    if (existingVehicle != null) {
      System.out.println("Vehicle already registered: " + normalizedPlate);
      return existingVehicle;
    }

    Vehicle vehicle = createVehicle(normalizedPlate, vehicleType);

    if (vehicleDAO.insertVehicle(vehicle)) {
      System.out.println("Vehicle registered: " + vehicle);
      return vehicle;
    } else {
      System.err.println("Failed to register vehicle: " + normalizedPlate);
      return null;
    }
  }

  public Vehicle getVehicle(String plateNumber) {
    String normalizedPlate = PlateValidator.validateAndNormalize(plateNumber);
    return vehicleDAO.findVehicleByPlate(normalizedPlate);
  }

  public boolean updateVehicle(Vehicle vehicle) {
    return vehicleDAO.updateVehicle(vehicle);
  }

  public boolean isVehicleRegistered(String plateNumber) {
    try {
      String normalizedPlate = PlateValidator.validateAndNormalize(plateNumber);
      return vehicleDAO.vehicleExists(normalizedPlate);
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  public int getTotalVehicleCount() {
    return vehicleDAO.getTotalVehicleCount();
  }

  private Vehicle createVehicle(String plateNumber, VehicleType vehicleType) {
    switch (vehicleType) {
      case MOTORCYCLE:
        return new Motorcycle(plateNumber);
      case CAR:
        return new Car(plateNumber);
      case SUV:
        return new SUV(plateNumber);
      case HANDICAPPED:
        return new HandicappedVehicle(plateNumber);
      default:
        throw new IllegalArgumentException("Unknown vehicle type: " + vehicleType);
    }
  }
}
