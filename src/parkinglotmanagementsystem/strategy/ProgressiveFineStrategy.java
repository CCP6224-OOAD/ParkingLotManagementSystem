package parkinglotmanagementsystem.strategy;

import parkinglotmanagementsystem.model.FineType;
import parkinglotmanagementsystem.util.Constants;

/**
 * Progressive Fine Strategy
 * Tiered fines based on duration:
 * - 0-24 hours: RM 0
 * - 24-48 hours: RM 50
 * - 48-72 hours: RM 150 (50 + 100)
 * - 72-96 hours: RM 300 (50 + 100 + 150)
 * - 96+ hours: RM 500 (50 + 100 + 150 + 200)
 */
public class ProgressiveFineStrategy implements FineCalculationStrategy {

    @Override
    public double calculateFine(long hoursParked, FineType fineType) {
        switch (fineType) {
            case OVERSTAY:
                return calculateOverstayFine(hoursParked);

            case RESERVED_MISUSE:
                // Reserved spot misuse gets the first tier fine
                return Constants.PROGRESSIVE_TIER_1; // RM 50

            default:
                return 0.0;
        }
    }

    private double calculateOverstayFine(long hoursParked) {
        if (hoursParked <= 24) {
            return 0.0; // No fine for first 24 hours
        } else if (hoursParked <= 48) {
            return Constants.PROGRESSIVE_TIER_1; // RM 50
        } else if (hoursParked <= 72) {
            return Constants.PROGRESSIVE_TIER_2; // RM 150 (50 + 100)
        } else if (hoursParked <= 96) {
            return Constants.PROGRESSIVE_TIER_3; // RM 300 (50 + 100 + 150)
        } else {
            return Constants.PROGRESSIVE_TIER_4; // RM 500 (50 + 100 + 150 + 200)
        }
    }

    @Override
    public String getStrategyName() {
        return "PROGRESSIVE";
    }

    @Override
    public String toString() {
        return "ProgressiveFineStrategy[Tiered: 24-48h=RM50, 48-72h=RM150, 72-96h=RM300, 96+h=RM500]";
    }
}
