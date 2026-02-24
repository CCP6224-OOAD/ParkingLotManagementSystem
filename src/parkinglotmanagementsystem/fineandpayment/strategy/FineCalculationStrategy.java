package parkinglotmanagementsystem.fineandpayment.strategy;

import parkinglotmanagementsystem.fineandpayment.model.FineType;

public interface FineCalculationStrategy {

    double calculateFine(long hoursParked, FineType fineType);

    String getStrategyName();
}
