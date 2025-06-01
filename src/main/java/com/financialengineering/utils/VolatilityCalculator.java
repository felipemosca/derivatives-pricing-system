package com.financialengineering.utils;

import java.util.List;

public class VolatilityCalculator {
    
    public static double calculateHistoricalVolatility(List<Double> prices, int periods) {
        if (prices == null || prices.size() < 2) {
            throw new IllegalArgumentException("Insufficient price data");
        }

        // Calculate log returns
        double[] logReturns = new double[prices.size() - 1];
        for (int i = 1; i < prices.size(); i++) {
            logReturns[i-1] = Math.log(prices.get(i) / prices.get(i-1));
        }

        // Calculate mean of log returns
        double meanReturn = calculateMean(logReturns);

        // Calculate variance
        double variance = calculateVariance(logReturns, meanReturn);

        // Annualize volatility
        return Math.sqrt(variance * periods);
    }

    public static double calculateGarchVolatility(List<Double> prices, int p, int q) {
        // Simplified GARCH(p,q) model implementation
        // This is a complex model and would require more sophisticated implementation
        // Here's a basic skeleton
        double[] returns = calculateReturns(prices);
        double[] volatilities = new double[returns.length];
        
        // Initial volatility estimation
        volatilities[0] = calculateStandardDeviation(returns);

        // GARCH model parameters
        double omega = 0.1;  // Constant term
        double[] alpha = new double[p];  // ARCH terms
        double[] beta = new double[q];   // GARCH terms

        // Placeholder parameter initialization
        for (int i = 0; i < p; i++) alpha[i] = 0.1 / p;
        for (int i = 0; i < q; i++) beta[i] = 0.8 / q;

        // Iterative volatility estimation
        for (int t = 1; t < returns.length; t++) {
            double conditionalVariance = omega;
            
            // ARCH terms
            for (int i = 0; i < p; i++) {
                if (t - i - 1 >= 0) {
                    conditionalVariance += alpha[i] * Math.pow(returns[t-i-1], 2);
                }
            }

            // GARCH terms
            for (int j = 0; j < q; j++) {
                if (t - j - 1 >= 0) {
                    conditionalVariance += beta[j] * volatilities[t-j-1];
                }
            }

            volatilities[t] = Math.sqrt(conditionalVariance);
        }

        return volatilities[volatilities.length - 1];
    }

    private static double[] calculateReturns(List<Double> prices) {
        double[] returns = new double[prices.size() - 1];
        for (int i = 1; i < prices.size(); i++) {
            returns[i-1] = Math.log(prices.get(i) / prices.get(i-1));
        }
        return returns;
    }

    private static double calculateMean(double[] values) {
        double sum = 0;
        for (double value : values) {
            sum += value;
        }
        return sum / values.length;
    }

    private static double calculateVariance(double[] values, double mean) {
        double sumSquaredDiff = 0;
        for (double value : values) {
            sumSquaredDiff += Math.pow(value - mean, 2);
        }
        return sumSquaredDiff / (values.length - 1);
    }

    private static double calculateStandardDeviation(double[] values) {
        double mean = calculateMean(values);
        double variance = calculateVariance(values, mean);
        return Math.sqrt(variance);
    }
}
