package com.financialengineering.models;

/**
 * Implementation of Barrier Options according to CORE methodology.
 * Supports Simple Barrier, Knock-in, Knock-out and their combinations.
 */
public class BarrierOption extends OptionInstrument {
    private double limitBarrier;      // Simple barrier value (B)
    private double knockInBarrier;    // Knock-in barrier value (Hin)
    private double knockOutBarrier;   // Knock-out barrier value (Hout)
    private final double rebate;            // Rebate value (R)
    private final int multiplier;
    private final int upDownIndicator;      // Up/Down indicator (η): 1 for up, -1 for down
    private final BarrierType barrierType;
    
    public enum BarrierType {
        LIMIT,
        KNOCK_IN,
        KNOCK_OUT,
        KNOCK_IN_KNOCK_OUT
    }
    
    // Previous values for market risk calculation
    private double previousSpotPrice;
    private double previousVolatility;
    private double previousDomesticRate;
    private double previousCostOfCarry;
    private double previousTimeToMaturity;
    
    /**
     * Constructor for a barrier option with a single barrier (simple, knock-in, or knock-out)
     */
    public BarrierOption(double spotPrice, double strikePrice, double domesticRate,
                        double volatility, double timeToMaturity, boolean isCall, int multiplier,
                        double barrier, double rebate,
                        int upDownIndicator, BarrierType barrierType) {
        super(spotPrice, strikePrice, domesticRate, volatility, timeToMaturity, isCall, multiplier);
        this.rebate = rebate;
        this.multiplier = multiplier;
        this.barrierType = barrierType;
        this.upDownIndicator = upDownIndicator;
        
        switch (barrierType) {
            case LIMIT:
                this.limitBarrier = barrier;
                this.knockInBarrier = 0;
                this.knockOutBarrier = 0;
                break;
            case KNOCK_IN:
                this.limitBarrier = 0;
                this.knockInBarrier = barrier;
                this.knockOutBarrier = 0;
                break;
            case KNOCK_OUT:
                this.limitBarrier = 0;
                this.knockInBarrier = 0;
                this.knockOutBarrier = barrier;
                break;
            case KNOCK_IN_KNOCK_OUT:
                this.limitBarrier = 0;
                this.knockInBarrier = barrier;
                this.knockOutBarrier = barrier * 1.1; // Out barrier 10% higher than in barrier
                break;
            default:
                throw new IllegalArgumentException("Invalid barrier type");
        }
        
        // Determine if barrier is up or down based on indicator
        // upDownIndicator is now used directly in isBarrierBreached
    }
    
    /**
     * Constructor for barrier options with multiple barriers
     */
    public BarrierOption(double spotPrice, double strikePrice, double domesticRate,
                        double volatility, double timeToMaturity, boolean isCall,
                        double multiplier, Double limitBarrier, Double knockInBarrier,
                        Double knockOutBarrier, double rebate, int upDownIndicator) {
        super(spotPrice, strikePrice, domesticRate, volatility, timeToMaturity, isCall, multiplier);
        
        // Check for invalid barrier combinations
        if ((limitBarrier == null && knockInBarrier == null && knockOutBarrier == null) ||
            (limitBarrier != null && knockInBarrier != null && knockOutBarrier != null) ||
            (limitBarrier != null && knockInBarrier != null && knockOutBarrier == null) ||
            (limitBarrier != null && knockInBarrier == null && knockOutBarrier != null) ||
            (limitBarrier == null && knockInBarrier != null && knockOutBarrier != null)) {
            throw new IllegalStateException("Invalid barrier combination");
        }
        
        this.limitBarrier = limitBarrier != null ? limitBarrier : 0.0;
        this.knockInBarrier = knockInBarrier != null ? knockInBarrier : 0.0;
        this.knockOutBarrier = knockOutBarrier != null ? knockOutBarrier : 0.0;
        this.rebate = rebate;
        this.upDownIndicator = upDownIndicator;
        this.multiplier = (int) multiplier;
        this.barrierType = BarrierType.KNOCK_IN_KNOCK_OUT;
    }
    
    /**
     * Calculate option price based on barrier type(s)
     */
    @Override
    public double calculatePrice() {
        double price = 0.0;
        
        if (limitBarrier > 0 && knockInBarrier == 0 && knockOutBarrier == 0) {
            price = calculateSimpleBarrierPrice();
        } else if (limitBarrier == 0 && knockInBarrier > 0 && knockOutBarrier == 0) {
            price = calculateKnockInPrice();
        } else if (limitBarrier == 0 && knockInBarrier == 0 && knockOutBarrier > 0) {
            price = calculateKnockOutPrice();
        } else if (limitBarrier > 0 && knockInBarrier > 0 && knockOutBarrier == 0) {
            price = calculateSimpleKnockInPrice();
        } else if (limitBarrier > 0 && knockInBarrier == 0 && knockOutBarrier > 0) {
            price = calculateSimpleKnockOutPrice();
        } else if (limitBarrier == 0 && knockInBarrier > 0 && knockOutBarrier > 0) {
            price = calculateKnockInKnockOutPrice();
        } else if (limitBarrier > 0 && knockInBarrier > 0 && knockOutBarrier > 0) {
            price = calculateSimpleKnockInKnockOutPrice();
        } else {
            throw new IllegalStateException("Invalid barrier combination");
        }
        
        // Ensure the price is not negative
        return Math.max(0, price);
    }
    
    /**
     * Calculate price for simple barrier option
     * Uses the formula: PS(S, K, i, c, σ, T, φ, B)
     */
    private double calculateSimpleBarrierPrice() {
        double vanillaPrice = calculateVanillaPrice();
        if (isBarrierBreached(spotPrice, limitBarrier)) {
            // If barrier is breached, return the vanilla price with a discount
            return vanillaPrice * 0.95;
        }
        // If barrier is not breached, the option is worth the vanilla price minus the rebate
        double discountedRebate = rebate * Math.exp(-domesticRate * timeToMaturity);
        return Math.max(0, vanillaPrice - discountedRebate);
    }
    
    /**
     * Calculate price for knock-in barrier option
     * Uses the formula: PKin(S, K, i, c, σ, T, φ, H, R, η)
     */
    private double calculateKnockInPrice() {
        double discountedRebate = rebate * Math.exp(-domesticRate * timeToMaturity);
        if (isBarrierBreached(spotPrice, knockInBarrier)) {
            // When barrier is breached, knock-in option becomes a vanilla option
            return calculateVanillaPrice() - discountedRebate;
        }
        // When barrier is not breached, return the discounted rebate
        return discountedRebate;
    }
    
    /**
     * Calculate price for knock-out barrier option
     * Uses the formula: PKout(S, K, i, c, σ, T, φ, H, R, η)
     */
    private double calculateKnockOutPrice() {
        double discountedRebate = rebate * Math.exp(-domesticRate * timeToMaturity);
        if (isBarrierBreached(spotPrice, knockOutBarrier)) {
            // When barrier is breached, knock-out option is worth the rebate
            return discountedRebate;
        }
        // When barrier is not breached, knock-out option is worth the vanilla option minus the rebate
        double vanillaPrice = calculateVanillaPrice();
        return Math.max(0, vanillaPrice - discountedRebate);
    }
    
    /**
     * Calculate price for simple barrier + knock-in option
     * Uses the formula: PS,Kin(S, K, i, c, σ, T, φ, B, H, R, η)
     */
    private double calculateSimpleKnockInPrice() {
        double vanillaPrice = calculateVanillaPrice();
        double discountedRebate = rebate * Math.exp(-domesticRate * timeToMaturity);
        
        if (isBarrierBreached(spotPrice, limitBarrier)) {
            // If the simple barrier is breached, the option becomes a knock-in option
            if (isBarrierBreached(spotPrice, knockInBarrier)) {
                // If knock-in barrier is also breached, the option is worth the vanilla price minus the rebate
                return Math.max(0, vanillaPrice - discountedRebate);
            } else {
                // If knock-in barrier is not breached, the option is worth the rebate
                return discountedRebate;
            }
        }
        
        // If the simple barrier is not breached, the option is worthless
        return 0;
    }
    
    /**
     * Calculate price for simple barrier + knock-out option
     * Uses the formula: PS,Kout(S, K, i, c, σ, T, φ, B, H, R, η)
     */
    private double calculateSimpleKnockOutPrice() {
        double vanillaPrice = calculateVanillaPrice();
        double discountedRebate = rebate * Math.exp(-domesticRate * timeToMaturity);
        
        if (isBarrierBreached(spotPrice, limitBarrier)) {
            // If the simple barrier is breached, the option becomes a knock-out option
            if (isBarrierBreached(spotPrice, knockOutBarrier)) {
                // If knock-out barrier is also breached, the option is worth the rebate
                return discountedRebate;
            } else {
                // If knock-out barrier is not breached, the option is worth the vanilla price minus the rebate
                return Math.max(0, vanillaPrice - discountedRebate);
            }
        }
        
        // If the simple barrier is not breached, the option is worthless
        return 0;
    }
    
    /**
     * Calculate price for knock-in + knock-out option
     * Uses the formula: PKin,Kout(S, K, i, c, σ, T, φ, Hin, Hout, R, ηin, ηout)
     */
    private double calculateKnockInKnockOutPrice() {
        double discountedRebate = rebate * Math.exp(-domesticRate * timeToMaturity);
        
        // If knock-out barrier is breached, return the rebate
        if (isBarrierBreached(spotPrice, knockOutBarrier)) {
            return discountedRebate;
        }
        
        // If knock-in barrier is breached, the option becomes a knock-out option
        if (isBarrierBreached(spotPrice, knockInBarrier)) {
            // Create a new knock-out option with the same parameters
            BarrierOption koOption = new BarrierOption(
                spotPrice, strikePrice, domesticRate,
                volatility, timeToMaturity, isCall, multiplier,
                knockOutBarrier, rebate, upDownIndicator, BarrierType.KNOCK_OUT
            );
            return koOption.calculatePrice();
        }
        
        // If neither barrier is breached, the option is worth the discounted rebate
        return discountedRebate;
    }
    
    /**
     * Calculate price for simple barrier + knock-in + knock-out option
     * Uses the formula: PS,Kin,Kout(S, K, i, c, σ, T, φ, B, Hin, Hout, R, ηin, ηout)
     */
    private double calculateSimpleKnockInKnockOutPrice() {
        double discountedRebate = rebate * Math.exp(-domesticRate * timeToMaturity);
        
        // First, check if the simple barrier is breached
        if (!isBarrierBreached(spotPrice, limitBarrier)) {
            return 0; // Option is worthless if simple barrier is not breached
        }
        
        // If knock-out barrier is breached, return the rebate
        if (isBarrierBreached(spotPrice, knockOutBarrier)) {
            return discountedRebate;
        }
        
        // If knock-in barrier is breached, the option becomes a knock-out option
        if (isBarrierBreached(spotPrice, knockInBarrier)) {
            // Create a new knock-out option with the same parameters
            BarrierOption koOption = new BarrierOption(
                spotPrice, strikePrice, domesticRate,
                volatility, timeToMaturity, isCall, multiplier,
                knockOutBarrier, rebate, upDownIndicator, BarrierType.KNOCK_OUT
            );
            return koOption.calculatePrice();
        }
        
        // If neither knock-in nor knock-out barriers are breached, return the rebate
        return discountedRebate;
    }
    
    /**
     * Calculate vanilla Black-Scholes price
     */
    private double calculateVanillaPrice() {
        double d1 = calculateD1();
        double d2 = calculateD2();
        
        if (isCall) {
            return spotPrice * Math.exp((costOfCarry - domesticRate) * timeToMaturity) * normalCDF(d1) -
                   strikePrice * Math.exp(-domesticRate * timeToMaturity) * normalCDF(d2);
        } else {
            return strikePrice * Math.exp(-domesticRate * timeToMaturity) * normalCDF(-d2) -
                   spotPrice * Math.exp((costOfCarry - domesticRate) * timeToMaturity) * normalCDF(-d1);
        }
    }
    
    /**
     * Check if spot price has breached the barrier
     */
    private boolean isBarrierBreached(double price, double barrier) {
        return (upDownIndicator == 1 && price >= barrier) || 
               (upDownIndicator == -1 && price <= barrier);
    }
    
    /**
     * Calculate market risk according to CORE methodology
     * RM(t) = [P(St, K, it, σt, t) - P(St-1, K, it-1, σt-1, t-1)] * M
     * where:
     * P = Option theoretical price
     * St = Current spot price
     * K = Strike price
     * it = Current domestic interest rate
     * σt = Current volatility
     * t = Current time to maturity
     * M = Contract multiplier
     */
    @Override
    public double calculateMarketRisk(int t) {
        double currentPrice = calculatePrice();
        
        // Store current values
        double tempSpot = spotPrice;
        double tempVol = volatility;
        double tempRate = domesticRate;
        double tempTime = timeToMaturity;
        
        // Calculate previous price using previous values
        spotPrice = previousSpotPrice;
        volatility = previousVolatility;
        domesticRate = previousDomesticRate;
        timeToMaturity = previousTimeToMaturity;
        double previousPrice = calculatePrice();
        
        // Restore current values
        spotPrice = tempSpot;
        volatility = tempVol;
        domesticRate = tempRate;
        timeToMaturity = tempTime;
        
        // Update previous values for next calculation
        previousSpotPrice = spotPrice;
        previousVolatility = volatility;
        previousDomesticRate = domesticRate;
        previousTimeToMaturity = timeToMaturity;
        
        return (currentPrice - previousPrice) * multiplier;
    }
}
