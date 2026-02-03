package parkinglotmanagementsystem.observer;

/**
 * Observer interface for listening to parking system events
 * Implementing classes will be notified when events occur
 */
public interface ParkingEventListener {
    
    /**
     * Called when a parking event occurs
     * @param eventType the type of event that occurred
     * @param eventData associated data (can be Ticket, Fine, Payment, etc.)
     */
    void onParkingEvent(ParkingEventType eventType, Object eventData);
}
