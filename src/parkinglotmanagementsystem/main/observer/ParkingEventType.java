package parkinglotmanagementsystem.main.observer;

public enum ParkingEventType {
    VEHICLE_ENTERED, // Vehicle parked in a spot
    VEHICLE_EXITED, // Vehicle left the parking lot
    FINE_GENERATED, // Fine was created for a vehicle
    FINE_PAID, // Fine was marked as paid
    PAYMENT_PROCESSED, // Payment transaction completed
    REVENUE_UPDATED, // Total revenue changed
    SPOT_STATUS_CHANGED, // Parking spot became available/occupied
    SPOT_TYPE_CHANGED, // Parking spot type is changed
    OCCUPANCY_CHANGED // Overall occupancy rate changed
}
