package com.financialengineering.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PlainVanillaOptionTest {
    private PlainVanillaOption callOption;
    private PlainVanillaOption putOption;
    private static final double SPOT_PRICE = 100.0;
    private static final double STRIKE_PRICE = 100.0; // At-the-money options
    private static final double DOMESTIC_RATE = 0.05;
    private static final double VOLATILITY = 0.2;
    private static final double TIME_TO_MATURITY = 1.0;
    private static final double MULTIPLIER = 1.0;
    private static final double EPSILON = 1e-6;
    
    @BeforeEach
    void setUp() {
        // Create at-the-money call and put options
        callOption = new PlainVanillaOption(SPOT_PRICE, STRIKE_PRICE, DOMESTIC_RATE,
                                          VOLATILITY, TIME_TO_MATURITY, true, MULTIPLIER);
        putOption = new PlainVanillaOption(SPOT_PRICE, STRIKE_PRICE, DOMESTIC_RATE,
                                         VOLATILITY, TIME_TO_MATURITY, false, MULTIPLIER);
    }
    
    @Test
    void testCallOptionPrice() {
        double price = callOption.calculatePrice();
        assertTrue(price > 0, "Call option price should be positive");
        assertEquals(12.24, price, 0.1, "Call option price should be approximately 12.24");
    }
    
    @Test
    void testPutOptionPrice() {
        double price = putOption.calculatePrice();
        assertTrue(price > 0, "Put option price should be positive");
        assertEquals(7.36, price, 0.1, "Put option price should be approximately 7.36");
    }
    
    @Test
    void testPutCallParity() {
        double callPrice = callOption.calculatePrice();
        double putPrice = putOption.calculatePrice();
        double spotPrice = callOption.getSpotPrice();
        double strikePrice = callOption.getStrikePrice();
        double rate = callOption.getDomesticRate();
        double time = callOption.getTimeToMaturity();
        
        // Put-Call Parity: C - P = S - K*e^(-rT)
        double lhs = callPrice - putPrice;
        double rhs = spotPrice - strikePrice * Math.exp(-rate * time);
        
        assertEquals(lhs, rhs, 0.01, "Put-Call parity should hold");
    }
    
    @Test
    void testDelta() {
        double callDelta = callOption.calculateDelta();
        double putDelta = putOption.calculateDelta();
        
        assertTrue(callDelta > 0 && callDelta < 1,
            "Call delta should be between 0 and 1");
        assertTrue(putDelta > -1 && putDelta < 0,
            "Put delta should be between -1 and 0");
    }
    
    @Test
    void testGamma() {
        double callGamma = callOption.calculateGamma();
        double putGamma = putOption.calculateGamma();
        
        assertTrue(callGamma > 0, "Call gamma should be positive");
        assertEquals(callGamma, putGamma, EPSILON,
            "Call and put gamma should be equal");
    }
    
    @Test
    void testVega() {
        double callVega = callOption.calculateVega();
        double putVega = putOption.calculateVega();
        
        assertTrue(callVega > 0, "Call vega should be positive");
        assertEquals(callVega, putVega, EPSILON,
            "Call and put vega should be equal");
    }
    
    @Test
    void testTheta() {
        double callTheta = callOption.calculateTheta();
        double putTheta = putOption.calculateTheta();
        
        // For at-the-money options with positive interest rates,
        // both call and put theta should typically be negative
        assertTrue(callTheta < 0, "Call theta should be negative");
        assertTrue(putTheta < 0, "Put theta should be negative");
    }
    
    @Test
    void testRho() {
        double callRho = callOption.calculateRho();
        double putRho = putOption.calculateRho();
        
        assertTrue(callRho > 0, "Call rho should be positive");
        assertTrue(putRho < 0, "Put rho should be negative");
    }
    
    @Test
    void testPutDelta() {
        double delta = putOption.calculateDelta();
        assertTrue(delta > -1 && delta < 0, "Put delta should be between -1 and 0");
        assertEquals(-0.32, delta, 0.1, "ATM put delta should be approximately -0.32");
    }
    
    @Test
    void testMarketRisk() {
        // Initial calculation with current values
        double initialRisk = callOption.calculateMarketRisk(0);
        assertEquals(0.0, initialRisk, 0.01, "Initial market risk should be zero");
        
        // Change spot price and recalculate
        callOption.setSpotPrice(110.0);
        double riskAfterSpotChange = callOption.calculateMarketRisk(1);
        assertTrue(riskAfterSpotChange > 0, "Risk should be positive when spot price increases");
        
        // Change volatility and recalculate
        callOption.setVolatility(0.25);
        double riskAfterVolChange = callOption.calculateMarketRisk(2);
        assertNotEquals(0.0, riskAfterVolChange, "Risk should change when volatility changes");
    }
}
