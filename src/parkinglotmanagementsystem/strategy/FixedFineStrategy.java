package parkinglotmanagementsystem.strategy;

import parkinglotmanagementsystem.model.FineType;
import parkinglotmanagementsystem.util.Constants;

/**
 * Fixed Fine Strategy
 * Flat RM 50 fine for any overstaying violation
 */
public class FixedFineStrategy implements FineCalculationStrategy {
    
    @Override
    public double calculateFine(long hoursParked, FineType fineType) {
        // Fixed fine regardless of duration
        switch (fineType) {
            case OVERSTAY:
                // Overstay fine only applies if parked > 24 hours
                if (hoursParked > Constants.OVERSTAY_THRESHOLD_HOURS) {
                    return Constants.FIXED_FINE_AMOUNT;  // RM 50
                }
                return 0.0;
                
            case RESERVED_MISUSE:
                // Reserved spot misuse always gets fixed fine
                return Constants.FIXED_FINE_AMOUNT;  // RM 50
                
            default:
                return 0.0;
        }
    }
    
    @Override
    public String getStrategyName() {
        return "FIXED";
    }
    
    @Override
    public String toString() {
        return "FixedFineStrategy[Flat RM " + Constants.FIXED_FINE_AMOUNT + " fine]";
    }
}
