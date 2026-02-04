package parkinglotmanagementsystem.strategy;

import parkinglotmanagementsystem.model.FineType;

/**
 * Strategy interface for fine calculation
 * Different implementations provide different fine calculation methods
 */
public interface FineCalculationStrategy {
    
    /**
     * Calculates fine amount based on hours parked
     * @param hoursParked total hours the vehicle was parked
     * @param fineType type of fine (OVERSTAY or RESERVED_MISUSE)
     * @return fine amount in RM
     */
    double calculateFine(long hoursParked, FineType fineType);
    
    /**
     * Gets the name of this strategy
     * @return strategy name (e.g., "FIXED", "PROGRESSIVE", "HOURLY")
     */
    String getStrategyName();
}
