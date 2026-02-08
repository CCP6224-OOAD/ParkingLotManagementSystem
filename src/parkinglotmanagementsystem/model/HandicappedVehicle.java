package parkinglotmanagementsystem.model;

public class HandicappedVehicle extends Vehicle {

  public HandicappedVehicle(String plateNumber) {
    super(plateNumber, VehicleType.HANDICAPPED);
  }

  public HandicappedVehicle(String plateNumber, double balance) {
    super(plateNumber, VehicleType.HANDICAPPED, balance);
  }

  @Override
  public boolean canParkIn(SpotType spotType) {
    return true;
  }

  @Override
  public double getParkingRate(ParkingSpot spot) {
    if (spot.getSpotType() == SpotType.HANDICAPPED) {
      return 0.0; // FREE in handicapped spots
    } else {
      return 2.0; // RM 2/hour in other spots
    }
  }
}
