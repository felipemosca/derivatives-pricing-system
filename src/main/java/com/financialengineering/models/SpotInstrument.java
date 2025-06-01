package com.financialengineering.models;

/**
 * Abstract class for spot instruments according to CORE methodology
 */
public abstract class SpotInstrument implements Instrument {
    protected double spotPrice;
    protected double helperExchangeRate;
    
    public SpotInstrument(double spotPrice) {
        this.spotPrice = spotPrice;
        this.helperExchangeRate = 1.0; // Default for domestic assets
    }
    
    public SpotInstrument(double spotPrice, double helperExchangeRate) {
        this.spotPrice = spotPrice;
        this.helperExchangeRate = helperExchangeRate;
    }
    
    @Override
    public double calculateMarketRisk(int t) {
        return spotPrice * helperExchangeRate;
    }
    
    @Override
    public double calculatePrice() {
        return spotPrice * helperExchangeRate;
    }
    
    public void setSpotPrice(double spotPrice) {
        this.spotPrice = spotPrice;
    }
    
    public void setHelperExchangeRate(double helperExchangeRate) {
        this.helperExchangeRate = helperExchangeRate;
    }
    
    public double getSpotPrice() {
        return spotPrice;
    }
    
    public double getHelperExchangeRate() {
        return helperExchangeRate;
    }
}
