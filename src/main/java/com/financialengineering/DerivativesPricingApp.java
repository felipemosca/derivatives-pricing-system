package com.financialengineering;

import com.financialengineering.models.PlainVanillaOption;
import com.financialengineering.models.FXFuture;
import com.financialengineering.utils.VolatilityCalculator;

import java.util.Arrays;
import java.util.List;

public class DerivativesPricingApp {
    public static void main(String[] args) {
        // Example Option Pricing
        PlainVanillaOption callOption = new PlainVanillaOption(
            150.0,   // Spot price
            155.0,   // Strike price
            0.02,    // Domestic rate
            0.3,     // Volatility
            0.25,    // Time to maturity (3 months)
            true,    // Is call
            1.0      // Multiplier
        );

        System.out.println("Call Option Price: $" + callOption.calculatePrice());
        System.out.println("Call Option Delta: " + callOption.calculateDelta());
        System.out.println("Call Option Gamma: " + callOption.calculateGamma());
        System.out.println("Call Option Vega: " + callOption.calculateVega());

        // Example Future Pricing
        FXFuture futureContract = new FXFuture(
            4200.0,  // Spot price
            0.03,    // Domestic rate
            0.01,    // Cost of carry
            1.0,     // Multiplier
            1.0,     // Notional
            180      // Days to maturity (6 months)
        );

        System.out.println("Future Contract Price: $" + futureContract.calculatePrice());
        System.out.println("Future Contract Market Risk: $" + futureContract.calculateMarketRisk(180));

        // Volatility Calculation Example
        List<Double> historicalPrices = Arrays.asList(
            100.0, 102.0, 99.5, 101.5, 103.0, 
            102.5, 104.0, 105.5, 103.5, 106.0
        );

        double historicalVolatility = VolatilityCalculator.calculateHistoricalVolatility(historicalPrices, 252);
        double garchVolatility = VolatilityCalculator.calculateGarchVolatility(historicalPrices, 1, 1);

        System.out.println("Historical Volatility: " + historicalVolatility);
        System.out.println("GARCH Volatility: " + garchVolatility);
    }
}
