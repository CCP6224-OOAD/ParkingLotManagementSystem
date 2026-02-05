package parkinglotmanagementsystem.model;

public class Car extends Vehicle {

  public Car(String plateNumber) {
    super(plateNumber, VehicleType.CAR);
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
