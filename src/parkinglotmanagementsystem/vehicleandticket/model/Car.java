package parkinglotmanagementsystem.vehicleandticket.model;

import parkinglotmanagementsystem.parking.model.ParkingSpot;
import parkinglotmanagementsystem.parking.model.SpotType;

public class Car extends Vehicle {

  public Car(String plateNumber) {
    super(plateNumber, VehicleType.CAR);
  }

  public Car(String plateNumber, double balance) {
    super(plateNumber, VehicleType.CAR, balance);
  }

  @Override
  public boolean canParkIn(SpotType spotType) {
    return spotType == SpotType.COMPACT || spotType == SpotType.REGULAR;
  }

  @Override
  public double getParkingRate(ParkingSpot spot) {
    return spot.getHourlyRate();
  }
}
