package com.financialengineering.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MathUtilsTest {
    private static final double EPSILON = 1e-6;
    
    @Test
    void testNormalCDF() {
        assertEquals(0.5, MathUtils.normalCDF(0.0), EPSILON);
        assertEquals(0.8766, MathUtils.normalCDF(1.0), 1e-4);
        assertEquals(0.1234, MathUtils.normalCDF(-1.0), 1e-4);
        assertEquals(0.9838, MathUtils.normalCDF(2.0), 1e-4);
    }
    
    @Test
    void testNormalPDF() {
        assertEquals(0.3989, MathUtils.normalPDF(0.0), 1e-4);
        assertEquals(0.2420, MathUtils.normalPDF(1.0), 1e-4);
        assertEquals(0.2420, MathUtils.normalPDF(-1.0), 1e-4);
        assertEquals(0.0540, MathUtils.normalPDF(2.0), 1e-4);
    }
    
    @Test
    void testLinearInterpolation() {
        assertEquals(1.5, MathUtils.linearInterpolate(1.5, 1.0, 2.0, 1.0, 2.0), EPSILON);
        assertEquals(0.0, MathUtils.linearInterpolate(0.0, -1.0, 1.0, -1.0, 1.0), EPSILON);
        assertEquals(0.5, MathUtils.linearInterpolate(0.5, 0.0, 1.0, 0.0, 1.0), EPSILON);
    }
    
    @Test
    void testRateConversion() {
        double annualRate = 0.05; // 5%
        int frequency = 2; // Semi-annual
        
        double continuous = MathUtils.annualToContinuous(annualRate, frequency);
        double backToAnnual = MathUtils.continuousToAnnual(continuous, frequency);
        
        assertEquals(annualRate, backToAnnual, EPSILON);
    }
    
    @Test
    void testDayCountFractions() {
        int startDate = 0;
        int endDate = 180;
        
        assertEquals(0.5, MathUtils.dayCountFraction360(startDate, endDate), EPSILON);
        assertEquals(0.4932, MathUtils.dayCountFraction365(startDate, endDate), 1e-4);
    }
    
    @Test
    void testForwardRate() {
        double rate1 = 0.05;
        double time1 = 1.0;
        double rate2 = 0.06;
        double time2 = 2.0;
        
        double forwardRate = MathUtils.calculateForwardRate(rate1, time1, rate2, time2);
        assertEquals(0.07, forwardRate, EPSILON);
    }
    
    @Test
    void testImpliedVolatility() {
        // Test parameters for an at-the-money call option
        double spotPrice = 100.0;
        double strikePrice = 100.0;
        double riskFreeRate = 0.05;
        double timeToMaturity = 1.0;
        double targetVol = 0.2;
        
        // Calculate option price using known volatility
        double price = MathUtils.blackScholesPrice(spotPrice, strikePrice, riskFreeRate,
                                                 targetVol, timeToMaturity, true);
        
        // Calculate implied volatility from the price
        double impliedVol = MathUtils.calculateImpliedVolatility(price, spotPrice, strikePrice,
                                                               timeToMaturity, riskFreeRate, true);
        
        assertEquals(targetVol, impliedVol, EPSILON);
    }
    
    @Test
    void testBlackScholesPrice() {
        double spotPrice = 100.0;
        double strikePrice = 100.0;
        double riskFreeRate = 0.05;
        double volatility = 0.2;
        double timeToMaturity = 1.0;
        
        double callPrice = MathUtils.blackScholesPrice(spotPrice, strikePrice, riskFreeRate,
                                                     volatility, timeToMaturity, true);
        double putPrice = MathUtils.blackScholesPrice(spotPrice, strikePrice, riskFreeRate,
                                                    volatility, timeToMaturity, false);
        
        // Test put-call parity
        double lhs = callPrice - putPrice;
        double rhs = spotPrice - strikePrice * Math.exp(-riskFreeRate * timeToMaturity);
        
        assertEquals(lhs, rhs, EPSILON);
    }
    
    @Test
    void testBlackScholesVega() {
        double spotPrice = 100.0;
        double strikePrice = 100.0;
        double riskFreeRate = 0.05;
        double volatility = 0.2;
        double timeToMaturity = 1.0;
        
        double vega = MathUtils.blackScholesVega(spotPrice, strikePrice, riskFreeRate,
                                               volatility, timeToMaturity);
        
        assertTrue(vega > 0, "Vega should be positive");
        assertEquals(37.52, vega, 0.1, "Vega should be approximately 37.52 for ATM option");
    }
}
