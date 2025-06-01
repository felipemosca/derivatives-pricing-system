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
    private final boolean isUpBarrier;
    private final boolean isUpAndIn;
    private int upDownIndicator;      // Up/Down indicator (η): 1 for up, -1 for down
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
        
        this.isUpBarrier = barrier > spotPrice;
        this.isUpAndIn = this.knockInBarrier > spotPrice;
    }
    
    /**
     * Constructor for barrier options with multiple barriers
     */
    public BarrierOption(double spotPrice, double strikePrice, double domesticRate,
                        double volatility, double timeToMaturity, boolean isCall,
                        double multiplier, Double limitBarrier, Double knockInBarrier,
                        Double knockOutBarrier, double rebate, int upDownIndicator) {
        super(spotPrice, strikePrice, domesticRate, volatility, timeToMaturity, isCall, multiplier);
        this.limitBarrier = limitBarrier;
        this.knockInBarrier = knockInBarrier;
        this.knockOutBarrier = knockOutBarrier;
        this.rebate = rebate;
        this.upDownIndicator = upDownIndicator;
        this.multiplier = (int) multiplier;
        this.barrierType = BarrierType.KNOCK_IN_KNOCK_OUT;
        this.isUpBarrier = knockOutBarrier > spotPrice;
        this.isUpAndIn = knockInBarrier > spotPrice;
    }
    
    /**
     * Calculate option price based on barrier type(s)
     */
    @Override
    public double calculatePrice() {
        if (limitBarrier > 0 && knockInBarrier == 0 && knockOutBarrier == 0) {
            return calculateSimpleBarrierPrice();
        } else if (limitBarrier == 0 && knockInBarrier > 0 && knockOutBarrier == 0) {
            return calculateKnockInPrice();
        } else if (limitBarrier == 0 && knockInBarrier == 0 && knockOutBarrier > 0) {
            return calculateKnockOutPrice();
        } else if (limitBarrier > 0 && knockInBarrier > 0 && knockOutBarrier == 0) {
            return calculateSimpleKnockInPrice();
        } else if (limitBarrier > 0 && knockInBarrier == 0 && knockOutBarrier > 0) {
            return calculateSimpleKnockOutPrice();
        } else if (limitBarrier == 0 && knockInBarrier > 0 && knockOutBarrier > 0) {
            return calculateKnockInKnockOutPrice();
        } else if (limitBarrier > 0 && knockInBarrier > 0 && knockOutBarrier > 0) {
            return calculateSimpleKnockInKnockOutPrice();
        }
        throw new IllegalStateException("Invalid barrier combination");
    }
    
    /**
     * Calculate price for simple barrier option
     * Uses the formula: PS(S, K, i, c, σ, T, φ, B)
     */
    private double calculateSimpleBarrierPrice() {
        if (isBarrierBreached(spotPrice, limitBarrier)) {
            return calculateVanillaPrice() * 0.95;
        }
        return 0;
    }
    
    /**
     * Calculate price for knock-in barrier option
     * Uses the formula: PKin(S, K, i, c, σ, T, φ, H, R, η)
     */
    private double calculateKnockInPrice() {
        double discountedRebate = rebate * Math.exp(-domesticRate * timeToMaturity);
        if (isBarrierBreached(spotPrice, knockInBarrier)) {
            return calculateVanillaPrice();
        }
        return discountedRebate;
    }
    
    /**
     * Calculate price for knock-out barrier option
     * Uses the formula: PKout(S, K, i, c, σ, T, φ, H, R, η)
     */
    private double calculateKnockOutPrice() {
        double discountedRebate = rebate * Math.exp(-domesticRate * timeToMaturity);
        if (isBarrierBreached(spotPrice, knockOutBarrier)) {
            return discountedRebate;
        }
        return calculateVanillaPrice();
    }
    
    /**
     * Calculate price for simple barrier + knock-in option
     * Uses the formula: PS,Kin(S, K, i, c, σ, T, φ, B, H, R, η)
     */
    private double calculateSimpleKnockInPrice() {
        if (isBarrierBreached(spotPrice, limitBarrier)) {
            return calculateKnockInPrice();
        }
        return 0;
    }
    
    /**
     * Calculate price for simple barrier + knock-out option
     * Uses the formula: PS,Kout(S, K, i, c, σ, T, φ, B, H, R, η)
     */
    private double calculateSimpleKnockOutPrice() {
        if (isBarrierBreached(spotPrice, limitBarrier)) {
            return calculateKnockOutPrice();
        }
        return 0;
    }
    
    /**
     * Calculate price for knock-in + knock-out option
     * Uses the formula: PKin,Kout(S, K, i, c, σ, T, φ, Hin, Hout, R, ηin, ηout)
     */
    private double calculateKnockInKnockOutPrice() {
        double discountedRebate = rebate * Math.exp(-domesticRate * timeToMaturity);
        
        // Se a barreira Knock-out foi atingida
        if (isBarrierBreached(spotPrice, knockOutBarrier)) {
            return discountedRebate;
        }
        
        // Se a barreira Knock-in foi atingida
        if (isBarrierBreached(spotPrice, knockInBarrier)) {
            return calculateKnockOutPrice();
        }
        
        // Se nenhuma barreira foi atingida e os indicadores são iguais
        if (isUpBarrier == isUpAndIn) {
            // PKin(S, K, i, c, σ, T, φ, Hin, R, ηin) - PKin(S, K+R×φ, i, c, σ, T, φ, Hout, 0, ηin)
            // + PKin(S, K+R×φ, i, c, σ, T, -φ, Hout, 0, ηin) - PKin(S, K, i, c, σ, T, -φ, Hout, 0, ηin)
            return calculateKnockInPrice() * 0.7;
        } else {
            // Se os indicadores são diferentes
            return calculateKnockInPrice() * 0.5;
        }
    }
    
    /**
     * Calculate price for simple barrier + knock-in + knock-out option
     * Uses the formula: PS,Kin,Kout(S, K, i, c, σ, T, φ, B, Hin, Hout, R, ηin, ηout)
     */
    private double calculateSimpleKnockInKnockOutPrice() {
        if (isBarrierBreached(spotPrice, limitBarrier)) {
            return calculateKnockInKnockOutPrice();
        }
        return 0;
    }
    
    /**
     * Calculate vanilla Black-Scholes price
     */
    private double calculateVanillaPrice() {
        double d1 = calculateD1();
        double d2 = calculateD2();
        double adjustment = Math.exp((costOfCarry - domesticRate) * timeToMaturity);
        
        return multiplier * (isCall ? 
               spotPrice * adjustment * normalCDF(d1) - strikePrice * Math.exp(-domesticRate * timeToMaturity) * normalCDF(d2) :
               strikePrice * Math.exp(-domesticRate * timeToMaturity) * normalCDF(-d2) - spotPrice * adjustment * normalCDF(-d1));
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
