package parkinglotmanagementsystem.main.observer;

public interface ParkingEventListener {

    void onParkingEvent(ParkingEventType eventType, Object eventData);
}
