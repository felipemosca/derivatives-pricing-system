package com.financialengineering.models;

/**
 * Abstract class for swaps according to CORE methodology
 */
public abstract class SwapInstrument implements Instrument {
    protected double notional;
    protected double fixedRate;
    protected double[] floatingRates;
    protected double[] paymentDates;
    protected double helperExchangeRate;
    protected double baseValue;
    protected double currentIndex;
    protected double baseIndex;
    protected double proRataIndex;
    protected double domesticRate;
    
    public SwapInstrument(double notional, double fixedRate, double[] floatingRates,
                         double[] paymentDates, double domesticRate) {
        this.notional = notional;
        this.fixedRate = fixedRate;
        this.floatingRates = floatingRates;
        this.paymentDates = paymentDates;
        this.helperExchangeRate = 1.0;
        this.baseValue = notional;
        this.currentIndex = 1.0;
        this.baseIndex = 1.0;
        this.proRataIndex = 1.0;
        this.domesticRate = domesticRate;
    }
    
    /**
     * Calculate the present value of fixed leg payments
     */
    protected double calculateFixedLegPV() {
        double pv = 0.0;
        double previousDate = 0.0;
        for (double paymentDate : paymentDates) {
            double discountFactor = calculateDiscountFactor(paymentDate);
            double period = paymentDate - previousDate;
            pv += notional * fixedRate * (period / 1.0) * discountFactor;
            previousDate = paymentDate;
        }
        return pv;
    }
    
    /**
     * Calculate the present value of floating leg payments
     */
    protected double calculateFloatingLegPV() {
        double pv = 0.0;
        double previousDate = 0.0;
        for (int i = 0; i < paymentDates.length; i++) {
            double discountFactor = calculateDiscountFactor(paymentDates[i]);
            double period = paymentDates[i] - previousDate;
            pv += notional * floatingRates[i] * (period / 1.0) * discountFactor;
            previousDate = paymentDates[i];
        }
        return pv;
    }
    
    /**
     * Calculate discount factor for a given payment date
     */
    protected double calculateDiscountFactor(double paymentDate) {
        return Math.exp(-domesticRate * paymentDate);
    }
    
    /**
     * Update base value according to CORE methodology:
     * VBA = V0 * (S/S0) * I
     * where:
     * VBA = Updated base value
     * V0 = Initial base value
     * S = Current index
     * S0 = Base index
     * I = Pro-rata index
     */
    protected double updateBaseValue() {
        return baseValue * (currentIndex / baseIndex) * proRataIndex;
    }
    
    @Override
    public double calculatePrice() {
        return (calculateFixedLegPV() - calculateFloatingLegPV()) * helperExchangeRate;
    }
    
    // Getters and setters
    public double getNotional() {
        return notional;
    }
    
    public double getFixedRate() {
        return fixedRate;
    }
    
    public double[] getFloatingRates() {
        return floatingRates;
    }
    
    public double[] getPaymentDates() {
        return paymentDates;
    }
    
    public double getHelperExchangeRate() {
        return helperExchangeRate;
    }
    
    public void setHelperExchangeRate(double helperExchangeRate) {
        this.helperExchangeRate = helperExchangeRate;
    }
    
    public void setCurrentIndex(double currentIndex) {
        this.currentIndex = currentIndex;
    }
    
    public void setBaseIndex(double baseIndex) {
        this.baseIndex = baseIndex;
    }
    
    public void setProRataIndex(double proRataIndex) {
        this.proRataIndex = proRataIndex;
    }
    
    public double getCurrentIndex() {
        return currentIndex;
    }
    
    public double getBaseIndex() {
        return baseIndex;
    }
    
    public double getProRataIndex() {
        return proRataIndex;
    }
}
