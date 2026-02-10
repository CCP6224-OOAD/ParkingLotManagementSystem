package parkinglotmanagementsystem.vehicleandticket.model;

import parkinglotmanagementsystem.parking.model.ParkingSpot;
import parkinglotmanagementsystem.parking.model.SpotType;

public class Motorcycle extends Vehicle {

  public Motorcycle(String plateNumber) {
    super(plateNumber, VehicleType.MOTORCYCLE);
  }

  public Motorcycle(String plateNumber, double balance) {
    super(plateNumber, VehicleType.MOTORCYCLE, balance);
  }

  @Override
  public boolean canParkIn(SpotType spotType) {
    return spotType == SpotType.COMPACT;
  }

  @Override
  public double getParkingRate(ParkingSpot spot) {
    return spot.getHourlyRate();
  }
}
