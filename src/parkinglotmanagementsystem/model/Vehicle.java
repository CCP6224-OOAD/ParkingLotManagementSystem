package parkinglotmanagementsystem.model;

public abstract class Vehicle {

  protected String plateNumber;
  protected VehicleType vehicleType;

  public Vehicle(String plateNumber, VehicleType vehicleType) {
    this.plateNumber = plateNumber;
    this.vehicleType = vehicleType;
  }

  public abstract boolean canParkIn(SpotType spotType);

  public abstract double getParkingRate(ParkingSpot spot);

  // Getters and Setters

  public String getPlateNumber() {
    return plateNumber;
  }

  public VehicleType getVehicleType() {
    return vehicleType;
  }

  @Override
  public String toString() {
    return String.format("Vehicle[Plate=%s, Type=%s]", plateNumber, vehicleType);
  }
}
