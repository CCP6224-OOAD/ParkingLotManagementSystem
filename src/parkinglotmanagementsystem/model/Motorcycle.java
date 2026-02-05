package parkinglotmanagementsystem.model;

public class Motorcycle extends Vehicle {

  public Motorcycle(String plateNumber) {
    super(plateNumber, VehicleType.MOTORCYCLE);
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
