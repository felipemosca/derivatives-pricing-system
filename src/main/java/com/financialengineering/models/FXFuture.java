package com.financialengineering.models;

/**
 * Implementation of FX Futures according to CORE methodology
 */
public class FXFuture extends FutureInstrument {
    private double previousSpotPrice;
    private double previousDomesticRate;
    private double previousCostOfCarry;
    
    public FXFuture(double spotPrice, double domesticRate, double costOfCarry,
                   double multiplier, double notional, int daysToMaturity) {
        super(spotPrice, domesticRate, costOfCarry, multiplier, notional, daysToMaturity);
        this.previousSpotPrice = spotPrice;
        this.previousDomesticRate = domesticRate;
        this.previousCostOfCarry = costOfCarry;
    }
    
    public FXFuture(double spotPrice, double domesticRate, double costOfCarry,
                   double multiplier, double notional, int daysToMaturity,
                   double helperExchangeRate) {
        super(spotPrice, domesticRate, costOfCarry, multiplier, notional, daysToMaturity, helperExchangeRate);
        this.previousSpotPrice = spotPrice;
        this.previousDomesticRate = domesticRate;
        this.previousCostOfCarry = costOfCarry;
    }
    
    /**
     * Calculate market risk for FX futures according to CORE formula:
     * RM(t) = [F(St, it, ct) - F(St-1, it-1, ct-1)] * M * N
     * where:
     * F = Future theoretical price
     * St = Current spot price
     * it = Current domestic interest rate
     * ct = Current cost of carry
     * St-1 = Previous spot price
     * it-1 = Previous domestic interest rate
     * ct-1 = Previous cost of carry
     * M = Contract multiplier
     * N = Number of contracts (notional)
     */
    @Override
    public double calculateMarketRisk(int t) {
        double currentPrice = calculatePrice();
        
        // Store current values
        double tempSpot = spotPrice;
        double tempRate = domesticRate;
        double tempCarry = costOfCarry;
        
        // Calculate previous price using previous values
        spotPrice = previousSpotPrice;
        domesticRate = previousDomesticRate;
        costOfCarry = previousCostOfCarry;
        double previousPrice = calculatePrice();
        
        // Restore current values
        spotPrice = tempSpot;
        domesticRate = tempRate;
        costOfCarry = tempCarry;
        
        // Update previous values for next calculation
        previousSpotPrice = spotPrice;
        previousDomesticRate = domesticRate;
        previousCostOfCarry = costOfCarry;
        
        return (currentPrice - previousPrice) * multiplier * notional;
    }
    
    // Additional getters and setters for previous values
    public void setPreviousValues(double spotPrice, double domesticRate, double costOfCarry) {
        this.previousSpotPrice = spotPrice;
        this.previousDomesticRate = domesticRate;
        this.previousCostOfCarry = costOfCarry;
    }
    
    public double getPreviousSpotPrice() {
        return previousSpotPrice;
    }
    
    public double getPreviousDomesticRate() {
        return previousDomesticRate;
    }
    
    public double getPreviousCostOfCarry() {
        return previousCostOfCarry;
    }
}
