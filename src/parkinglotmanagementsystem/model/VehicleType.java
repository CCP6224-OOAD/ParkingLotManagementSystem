package parkinglotmanagementsystem.model;

public enum VehicleType {
  MOTORCYCLE, // Can park in COMPACT only
  CAR, // Can park in COMPACT or REGULAR
  SUV, // Can park in REGULAR only
  HANDICAPPED // Can park in any spot, gets discounted rate
}
