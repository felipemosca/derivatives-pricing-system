package com.financialengineering.models;

/**
 * Abstract class for futures according to CORE methodology
 */
public abstract class FutureInstrument implements Instrument {
    protected double spotPrice;
    protected double domesticRate;
    protected double costOfCarry;
    protected double multiplier;
    protected double notional;
    protected double helperExchangeRate;
    protected int daysToMaturity;
    
    public FutureInstrument(double spotPrice, double domesticRate, double costOfCarry, 
                          double multiplier, double notional, int daysToMaturity) {
        this.spotPrice = spotPrice;
        this.domesticRate = domesticRate;
        this.costOfCarry = costOfCarry;
        this.multiplier = multiplier;
        this.notional = notional;
        this.daysToMaturity = daysToMaturity;
        this.helperExchangeRate = 1.0; // Default for domestic futures
    }
    
    public FutureInstrument(double spotPrice, double domesticRate, double costOfCarry,
                          double multiplier, double notional, int daysToMaturity, 
                          double helperExchangeRate) {
        this(spotPrice, domesticRate, costOfCarry, multiplier, notional, daysToMaturity);
        this.helperExchangeRate = helperExchangeRate;
    }
    
    /**
     * Calculate future theoretical price using cost of carry model
     * F = S * e^((r-q)T)
     * where:
     * S = spot price
     * r = domestic interest rate
     * q = cost of carry
     * T = time to maturity in years
     */
    @Override
    public double calculatePrice() {
        double timeToMaturity = daysToMaturity / 360.0; // Converting days to years
        return spotPrice * Math.exp((domesticRate - costOfCarry) * timeToMaturity) * multiplier;
    }
    
    /**
     * Calculate market risk based on price changes
     * RM(t) = [F(St, it, ct) - F(St-1, it-1, ct-1)] * M * N
     */
    @Override
    public abstract double calculateMarketRisk(int t);
    
    // Getters and setters
    public void setSpotPrice(double spotPrice) {
        this.spotPrice = spotPrice;
    }
    
    public void setDomesticRate(double domesticRate) {
        this.domesticRate = domesticRate;
    }
    
    public void setCostOfCarry(double costOfCarry) {
        this.costOfCarry = costOfCarry;
    }
    
    public double getSpotPrice() {
        return spotPrice;
    }
    
    public double getDomesticRate() {
        return domesticRate;
    }
    
    public double getCostOfCarry() {
        return costOfCarry;
    }
    
    public double getMultiplier() {
        return multiplier;
    }
    
    public double getNotional() {
        return notional;
    }
    
    public int getDaysToMaturity() {
        return daysToMaturity;
    }
    
    public double getHelperExchangeRate() {
        return helperExchangeRate;
    }
}
