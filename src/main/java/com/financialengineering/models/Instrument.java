package com.financialengineering.models;

/**
 * Base interface for all financial instruments
 */
public interface Instrument {
    /**
     * Calculate market risk according to CORE methodology
     * @param t Current date
     * @return Market risk value
     */
    double calculateMarketRisk(int t);
    
    /**
     * Calculate theoretical price of the instrument
     * @return Theoretical price
     */
    double calculatePrice();
}
