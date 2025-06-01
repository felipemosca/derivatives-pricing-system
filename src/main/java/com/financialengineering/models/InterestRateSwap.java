package com.financialengineering.models;

/**
 * Implementation of Interest Rate Swaps according to CORE methodology
 */
public class InterestRateSwap extends SwapInstrument {
    private double[] previousFloatingRates;
    private double previousFixedRate;
    
    public InterestRateSwap(double notional, double fixedRate, double[] floatingRates,
                           double[] paymentDates, double domesticRate) {
        super(notional, fixedRate, floatingRates, paymentDates, domesticRate);
        this.previousFloatingRates = floatingRates.clone();
        this.previousFixedRate = fixedRate;
    }
    
    /**
     * Calculate market risk according to CORE methodology:
     * RM(t) = [MtM(t) - MtM(t-1)] * H
     * where:
     * MtM = Mark to Market value (swap price)
     * H = Helper exchange rate
     */
    @Override
    public double calculateMarketRisk(int t) {
        double currentMtM = calculatePrice();
        
        // Store current values
        double tempFixed = fixedRate;
        double[] tempFloating = floatingRates.clone();
        
        // Calculate previous MtM using previous values
        fixedRate = previousFixedRate;
        floatingRates = previousFloatingRates;
        double previousMtM = calculatePrice();
        
        // Restore current values
        fixedRate = tempFixed;
        floatingRates = tempFloating;
        
        // Update previous values for next calculation
        previousFixedRate = fixedRate;
        previousFloatingRates = floatingRates.clone();
        
        return (currentMtM - previousMtM) * helperExchangeRate;
    }
    
    /**
     * Calculate the fixed rate that makes the swap value zero (par rate)
     */
    public double calculateParRate() {
        double floatingLegPV = calculateFloatingLegPV();
        double annuityFactor = 0.0;
        double previousDate = 0.0;
        
        for (double paymentDate : paymentDates) {
            double period = paymentDate - previousDate;
            annuityFactor += calculateDiscountFactor(paymentDate) * period;
            previousDate = paymentDate;
        }
        
        return floatingLegPV / (notional * annuityFactor);
    }
    
    /**
     * Calculate the duration of the swap
     */
    public double calculateDuration() {
        double price = calculatePrice();
        if (Math.abs(price) < 1e-10) {
            return 0.0;
        }
        
        double weightedSum = 0.0;
        for (int i = 0; i < paymentDates.length; i++) {
            double discountFactor = calculateDiscountFactor(paymentDates[i]);
            double cashFlow = notional * (fixedRate - floatingRates[i]);
            weightedSum += paymentDates[i] * cashFlow * discountFactor;
        }
        
        return weightedSum / price;
    }
    
    // Additional getters and setters for previous values
    public void setPreviousRates(double fixedRate, double[] floatingRates) {
        this.previousFixedRate = fixedRate;
        this.previousFloatingRates = floatingRates.clone();
    }
    
    public double getPreviousFixedRate() {
        return previousFixedRate;
    }
    
    public double[] getPreviousFloatingRates() {
        return previousFloatingRates;
    }
}
