package com.financialengineering.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class InterestRateSwapTest {
    private InterestRateSwap swap;
    private static final double NOTIONAL = 1_000_000.0;
    private static final double FIXED_RATE = 0.05; // 5%
    private static final double[] PAYMENT_DATES = {0.5, 1.0, 1.5, 2.0}; // Semi-annual payments for 2 years
    private static final double DOMESTIC_RATE = 0.05; // 5%
    
    @BeforeEach
    void setUp() {
        // Create floating rates that start at 4% and increase by 0.5% each period
        double[] floatingRates = {0.04, 0.045, 0.05, 0.055};
        swap = new InterestRateSwap(NOTIONAL, FIXED_RATE, floatingRates, PAYMENT_DATES, DOMESTIC_RATE);
    }
    
    @Test
    void testInitialPrice() {
        double price = swap.calculatePrice();
        assertTrue(Math.abs(price) < NOTIONAL * 0.1, 
            "Initial swap price should be relatively small compared to notional");
    }
    
    @Test
    void testParRate() {
        double parRate = swap.calculateParRate();
        assertTrue(parRate > 0, "Par rate should be positive");
        
        // Create a new swap with the par rate
        InterestRateSwap parSwap = new InterestRateSwap(
            NOTIONAL, parRate, swap.getFloatingRates(), PAYMENT_DATES, DOMESTIC_RATE
        );
        
        // The price should be very close to zero
        double price = parSwap.calculatePrice();
        assertEquals(0.0, price, 0.01, "Swap with par rate should have price close to zero");
    }
    
    @Test
    void testDuration() {
        double duration = swap.calculateDuration();
        assertTrue(duration > 0, "Duration should be positive");
        assertTrue(duration < PAYMENT_DATES[PAYMENT_DATES.length - 1],
            "Duration should be less than the final payment date");
    }
    
    @Test
    void testMarketRisk() {
        // Initial calculation with current values
        double initialRisk = swap.calculateMarketRisk(0);
        assertEquals(0.0, initialRisk, 0.01, "Initial market risk should be zero");
        
        // Change fixed rate and recalculate
        double[] newFloatingRates = {0.045, 0.05, 0.055, 0.06};
        swap.setPreviousRates(FIXED_RATE, swap.getFloatingRates());
        for (int i = 0; i < newFloatingRates.length; i++) {
            swap.getFloatingRates()[i] = newFloatingRates[i];
        }
        
        double riskAfterRateChange = swap.calculateMarketRisk(1);
        assertNotEquals(0.0, riskAfterRateChange,
            "Risk should change when floating rates change");
    }
    
    @Test
    void testFixedLegPV() {
        // For a 5% fixed rate and 2-year swap
        double fixedLegPV = swap.calculateFixedLegPV();
        assertTrue(fixedLegPV > 0, "Fixed leg PV should be positive");
        assertTrue(fixedLegPV < NOTIONAL * FIXED_RATE * 2,
            "Fixed leg PV should be less than undiscounted cash flows");
    }
    
    @Test
    void testFloatingLegPV() {
        double floatingLegPV = swap.calculateFloatingLegPV();
        assertTrue(floatingLegPV > 0, "Floating leg PV should be positive");
        
        // Calculate average floating rate
        double avgFloatingRate = 0.0;
        for (double rate : swap.getFloatingRates()) {
            avgFloatingRate += rate;
        }
        avgFloatingRate /= swap.getFloatingRates().length;
        
        assertTrue(floatingLegPV < NOTIONAL * avgFloatingRate * 2,
            "Floating leg PV should be less than undiscounted cash flows");
    }
}
