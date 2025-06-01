package com.financialengineering.models;

/**
 * Abstract class for options according to CORE methodology
 */
public abstract class OptionInstrument implements Instrument {
    protected double spotPrice;
    protected double strikePrice;
    protected double domesticRate;
    protected double volatility;
    protected double timeToMaturity;
    protected boolean isCall;
    protected double multiplier;
    protected double helperExchangeRate;
    protected double costOfCarry;
    
    public OptionInstrument(double spotPrice, double strikePrice, double domesticRate,
                          double volatility, double timeToMaturity, boolean isCall,
                          double multiplier) {
        this.spotPrice = spotPrice;
        this.strikePrice = strikePrice;
        this.domesticRate = domesticRate;
        this.volatility = volatility;
        this.timeToMaturity = timeToMaturity;
        this.isCall = isCall;
        this.multiplier = multiplier;
        this.helperExchangeRate = 1.0; // Default for domestic options
        this.costOfCarry = domesticRate; // Default cost of carry equals domestic rate
    }
    
    public OptionInstrument(double spotPrice, double strikePrice, double domesticRate,
                          double volatility, double timeToMaturity, boolean isCall,
                          double multiplier, double helperExchangeRate, double costOfCarry) {
        this(spotPrice, strikePrice, domesticRate, volatility, timeToMaturity, isCall, multiplier);
        this.helperExchangeRate = helperExchangeRate;
        this.costOfCarry = costOfCarry;
    }
    
    /**
     * Calculate d1 parameter for Black-Scholes formula
     */
    protected double calculateD1() {
        return (Math.log(spotPrice/strikePrice) + 
                (costOfCarry + 0.5 * volatility * volatility) * timeToMaturity) / 
                (volatility * Math.sqrt(timeToMaturity));
    }
    
    /**
     * Calculate d2 parameter for Black-Scholes formula
     */
    protected double calculateD2() {
        return calculateD1() - volatility * Math.sqrt(timeToMaturity);
    }
    
    /**
     * Standard normal cumulative distribution function
     */
    protected double normalCDF(double x) {
        return 0.5 * (1.0 + erf(x / Math.sqrt(2.0)));
    }
    
    /**
     * Error function implementation
     */
    private double erf(double z) {
        double t = 1.0 / (1.0 + 0.5 * Math.abs(z));
        
        // Constants for Horner's method
        double[] a = {
            0.254829592, -0.284496736, 1.421413741,
            -1.453152027, 1.061405429
        };
        
        // Polynomial approximation
        double sum = t * (a[0] + t * (a[1] + t * (a[2] + t * (a[3] + t * a[4]))));
        sum = 1.0 - sum * Math.exp(-z * z);
        
        return z < 0 ? -sum : sum;
    }
    
    // Getters and setters
    public double getSpotPrice() {
        return spotPrice;
    }
    
    public double getStrikePrice() {
        return strikePrice;
    }
    
    public double getDomesticRate() {
        return domesticRate;
    }
    
    public double getVolatility() {
        return volatility;
    }
    
    public double getTimeToMaturity() {
        return timeToMaturity;
    }
    
    public boolean isCall() {
        return isCall;
    }
    
    public double getMultiplier() {
        return multiplier;
    }
    
    public double getHelperExchangeRate() {
        return helperExchangeRate;
    }
    
    public double getCostOfCarry() {
        return costOfCarry;
    }
    
    public void setSpotPrice(double spotPrice) {
        this.spotPrice = spotPrice;
    }
    
    public void setVolatility(double volatility) {
        this.volatility = volatility;
    }
    
    public void setDomesticRate(double domesticRate) {
        this.domesticRate = domesticRate;
    }
    
    public void setCostOfCarry(double costOfCarry) {
        this.costOfCarry = costOfCarry;
    }
}
