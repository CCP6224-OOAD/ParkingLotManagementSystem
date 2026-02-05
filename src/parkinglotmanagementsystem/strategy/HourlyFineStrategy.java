package parkinglotmanagementsystem.strategy;

import parkinglotmanagementsystem.model.FineType;
import parkinglotmanagementsystem.util.Constants;

/**
 * Hourly Fine Strategy
 * RM 20 per hour for overstaying beyond 24 hours
 */
public class HourlyFineStrategy implements FineCalculationStrategy {
    
    @Override
    public double calculateFine(long hoursParked, FineType fineType) {
        switch (fineType) {
            case OVERSTAY:
                return calculateOverstayFine(hoursParked);
                
            case RESERVED_MISUSE:
                // Reserved spot misuse gets a fixed penalty
                return Constants.FIXED_FINE_AMOUNT;  // RM 50
                
            default:
                return 0.0;
        }
    }
    
    /**
     * Calculates hourly overstay fine
     * RM 20 per hour for each hour beyond 24
     */
    private double calculateOverstayFine(long hoursParked) {
        if (hoursParked <= Constants.OVERSTAY_THRESHOLD_HOURS) {
            return 0.0;  // No fine for first 24 hours
        }
        
        long overstayHours = hoursParked - Constants.OVERSTAY_THRESHOLD_HOURS;
        return overstayHours * Constants.HOURLY_FINE_RATE;  // RM 20 per hour
    }
    
    @Override
    public String getStrategyName() {
        return "HOURLY";
    }
    
    @Override
    public String toString() {
        return "HourlyFineStrategy[RM " + Constants.HOURLY_FINE_RATE + " per hour over 24 hours]";
    }
}
