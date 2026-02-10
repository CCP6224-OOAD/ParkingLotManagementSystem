package parkinglotmanagementsystem.vehicleandticket.model;

import parkinglotmanagementsystem.parking.model.ParkingSpot;
import parkinglotmanagementsystem.parking.model.SpotType;

public class SUV extends Vehicle {

  public SUV(String plateNumber) {
    super(plateNumber, VehicleType.SUV);
  }

  public SUV(String plateNumber, double balance) {
    super(plateNumber, VehicleType.SUV, balance);
  }

  @Override
  public boolean canParkIn(SpotType spotType) {
    return spotType == SpotType.REGULAR;
  }

  @Override
  public double getParkingRate(ParkingSpot spot) {
    return spot.getHourlyRate();
  }
}
