package com.financialengineering.utils;

/**
 * Utility class for common mathematical calculations
 */
public class MathUtils {
    /**
     * Standard normal cumulative distribution function
     */
    public static double normalCDF(double x) {
        return 0.5 * (1.0 + erf(x / Math.sqrt(2.0)));
    }
    
    /**
     * Standard normal probability density function
     */
    public static double normalPDF(double x) {
        return Math.exp(-0.5 * x * x) / Math.sqrt(2.0 * Math.PI);
    }
    
    /**
     * Error function implementation using Horner's method
     */
    public static double erf(double z) {
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
    
    /**
     * Linear interpolation
     */
    public static double linearInterpolate(double x, double x0, double x1, double y0, double y1) {
        if (Math.abs(x1 - x0) < 1e-10) {
            return (y0 + y1) / 2;
        }
        return y0 + (x - x0) * (y1 - y0) / (x1 - x0);
    }
    
    /**
     * Convert annual rate to continuous compound rate
     */
    public static double annualToContinuous(double annualRate, int compoundingFrequency) {
        return compoundingFrequency * Math.log(1 + annualRate / compoundingFrequency);
    }
    
    /**
     * Convert continuous compound rate to annual rate
     */
    public static double continuousToAnnual(double continuousRate, int compoundingFrequency) {
        return compoundingFrequency * (Math.exp(continuousRate / compoundingFrequency) - 1);
    }
    
    /**
     * Calculate day count fraction using ACT/360 convention
     */
    public static double dayCountFraction360(int startDate, int endDate) {
        return (endDate - startDate) / 360.0;
    }
    
    /**
     * Calculate day count fraction using ACT/365 convention
     */
    public static double dayCountFraction365(int startDate, int endDate) {
        return (endDate - startDate) / 365.0;
    }
    
    /**
     * Calculate forward rate from spot rates
     */
    public static double calculateForwardRate(double rate1, double time1, double rate2, double time2) {
        if (Math.abs(time2 - time1) < 1e-10) {
            return rate1;
        }
        return (rate2 * time2 - rate1 * time1) / (time2 - time1);
    }
    
    /**
     * Calculate implied volatility using Newton-Raphson method
     */
    public static double calculateImpliedVolatility(double targetPrice, double spotPrice,
                                                  double strikePrice, double timeToMaturity,
                                                  double riskFreeRate, boolean isCall) {
        double sigma = 0.5; // Initial guess
        double tolerance = 1e-5;
        int maxIterations = 100;
        
        for (int i = 0; i < maxIterations; i++) {
            double price = blackScholesPrice(spotPrice, strikePrice, riskFreeRate,
                                          sigma, timeToMaturity, isCall);
            double vega = blackScholesVega(spotPrice, strikePrice, riskFreeRate,
                                         sigma, timeToMaturity);
            
            double diff = price - targetPrice;
            if (Math.abs(diff) < tolerance) {
                return sigma;
            }
            
            sigma = sigma - diff / vega;
            if (sigma <= 0) {
                sigma = 0.0001;
            }
        }
        
        throw new RuntimeException("Implied volatility did not converge");
    }
    
    /**
     * Calculate Black-Scholes option price
     */
    public static double blackScholesPrice(double S, double K, double r,
                                          double sigma, double T, boolean isCall) {
        double d1 = (Math.log(S/K) + (r + 0.5 * sigma * sigma) * T) / (sigma * Math.sqrt(T));
        double d2 = d1 - sigma * Math.sqrt(T);
        
        if (isCall) {
            return S * normalCDF(d1) - K * Math.exp(-r * T) * normalCDF(d2);
        } else {
            return K * Math.exp(-r * T) * normalCDF(-d2) - S * normalCDF(-d1);
        }
    }
    
    /**
     * Calculate Black-Scholes vega
     */
    public static double blackScholesVega(double S, double K, double r,
                                         double sigma, double T) {
        double d1 = (Math.log(S/K) + (r + 0.5 * sigma * sigma) * T) / (sigma * Math.sqrt(T));
        return S * Math.sqrt(T) * normalPDF(d1);
    }
}
