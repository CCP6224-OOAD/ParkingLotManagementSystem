package parkinglotmanagementsystem.model;

public abstract class Vehicle {

  protected String plateNumber;
  protected VehicleType vehicleType;
  protected double balance;

  public Vehicle(String plateNumber, VehicleType vehicleType) {
    this.plateNumber = plateNumber;
    this.vehicleType = vehicleType;
    this.balance = 0.0;
  }

  public Vehicle(String plateNumber, VehicleType vehicleType, double balance) {
    this.plateNumber = plateNumber;
    this.vehicleType = vehicleType;
    this.balance = balance;
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

  public double getBalance() {
    return balance;
  }

  public void setBalance(double balance) {
    this.balance = balance;
  }

  @Override
  public String toString() {
    return String.format("Vehicle[Plate=%s, Type=%s]", plateNumber, vehicleType);
  }
}
