package com.financialengineering.models;

/**
 * Implementation of Plain Vanilla Options using Black-Scholes model according to CORE methodology
 */
public class PlainVanillaOption extends OptionInstrument {
    private double previousSpotPrice;
    private double previousVolatility;
    private double previousDomesticRate;
    private double previousCostOfCarry;
    
    public PlainVanillaOption(double spotPrice, double strikePrice, double domesticRate,
                             double volatility, double timeToMaturity, boolean isCall,
                             double multiplier) {
        super(spotPrice, strikePrice, domesticRate, volatility, timeToMaturity, isCall, multiplier);
        initializePreviousValues();
    }
    
    public PlainVanillaOption(double spotPrice, double strikePrice, double domesticRate,
                             double volatility, double timeToMaturity, boolean isCall,
                             double multiplier, double helperExchangeRate, double costOfCarry) {
        super(spotPrice, strikePrice, domesticRate, volatility, timeToMaturity, isCall,
              multiplier, helperExchangeRate, costOfCarry);
        initializePreviousValues();
    }
    
    private void initializePreviousValues() {
        this.previousSpotPrice = spotPrice;
        this.previousVolatility = volatility;
        this.previousDomesticRate = domesticRate;
        this.previousCostOfCarry = costOfCarry;
    }
    
    /**
     * Calculate option price using Black-Scholes formula
     * For call option: C = S * e^((b-r)T) * N(d1) - K * e^(-rT) * N(d2)
     * For put option: P = K * e^(-rT) * N(-d2) - S * e^((b-r)T) * N(-d1)
     * where:
     * S = spot price
     * K = strike price
     * r = domestic interest rate
     * b = cost of carry
     * T = time to maturity
     * N(x) = cumulative normal distribution function
     */
    @Override
    public double calculatePrice() {
        double d1 = calculateD1();
        double d2 = calculateD2();
        
        if (isCall) {
            return multiplier * (
                spotPrice * Math.exp((costOfCarry - domesticRate) * timeToMaturity) * normalCDF(d1) -
                strikePrice * Math.exp(-domesticRate * timeToMaturity) * normalCDF(d2)
            );
        } else {
            return multiplier * (
                strikePrice * Math.exp(-domesticRate * timeToMaturity) * normalCDF(-d2) -
                spotPrice * Math.exp((costOfCarry - domesticRate) * timeToMaturity) * normalCDF(-d1)
            );
        }
    }
    
    /**
     * Calculate market risk according to CORE methodology:
     * RM(t) = [BS(St, K, it, σt) - BS(St-1, K, it-1, σt-1)] * M
     * where:
     * BS = Black-Scholes formula
     * St = Current spot price
     * K = Strike price
     * it = Current domestic interest rate
     * σt = Current volatility
     * St-1 = Previous spot price
     * it-1 = Previous domestic interest rate
     * σt-1 = Previous volatility
     * M = Contract multiplier
     */
    @Override
    public double calculateMarketRisk(int t) {
        double currentPrice = calculatePrice();
        
        // Store current values
        double tempSpot = spotPrice;
        double tempVol = volatility;
        double tempRate = domesticRate;
        double tempCarry = costOfCarry;
        
        // Calculate previous price using previous values
        spotPrice = previousSpotPrice;
        volatility = previousVolatility;
        domesticRate = previousDomesticRate;
        costOfCarry = previousCostOfCarry;
        double previousPrice = calculatePrice();
        
        // Restore current values
        spotPrice = tempSpot;
        volatility = tempVol;
        domesticRate = tempRate;
        costOfCarry = tempCarry;
        
        // Update previous values for next calculation
        previousSpotPrice = spotPrice;
        previousVolatility = volatility;
        previousDomesticRate = domesticRate;
        previousCostOfCarry = costOfCarry;
        
        return (currentPrice - previousPrice) * helperExchangeRate;
    }
    
    /**
     * Calculate option delta (first derivative with respect to spot price)
     * For call option: Δ = e^((b-r)T) * N(d1)
     * For put option: Δ = e^((b-r)T) * (N(d1) - 1)
     */
    public double calculateDelta() {
        double d1 = calculateD1();
        double adjustment = Math.exp((costOfCarry - domesticRate) * timeToMaturity);
        
        if (isCall) {
            return adjustment * normalCDF(d1);
        } else {
            return adjustment * (normalCDF(d1) - 1);
        }
    }
    
    /**
     * Calculate option gamma (second derivative with respect to spot price)
     * Γ = (N'(d1) * e^((b-r)T)) / (S * σ * √T)
     */
    public double calculateGamma() {
        double d1 = calculateD1();
        double nd1 = normalPDF(d1);
        return (nd1 * Math.exp((costOfCarry - domesticRate) * timeToMaturity)) / 
               (spotPrice * volatility * Math.sqrt(timeToMaturity));
    }
    
    /**
     * Calculate option vega (sensitivity to volatility)
     * ν = S * e^((b-r)T) * N'(d1) * √T
     */
    public double calculateVega() {
        double d1 = calculateD1();
        double nd1 = normalPDF(d1);
        return spotPrice * Math.exp((costOfCarry - domesticRate) * timeToMaturity) * 
               nd1 * Math.sqrt(timeToMaturity);
    }
    
    /**
     * Calculate option theta (sensitivity to time)
     * Θ = -(S * σ * e^((b-r)T) * N'(d1)) / (2√T) - (b-r)S * e^((b-r)T) * N(d1) ± r * K * e^(-rT) * N(±d2)
     */
    public double calculateTheta() {
        double d1 = calculateD1();
        double d2 = calculateD2();
        double nd1 = normalPDF(d1);
        double adjustment = Math.exp((costOfCarry - domesticRate) * timeToMaturity);
        
        double term1 = -(spotPrice * volatility * adjustment * nd1) / (2 * Math.sqrt(timeToMaturity));
        double term2 = -(costOfCarry - domesticRate) * spotPrice * adjustment * normalCDF(isCall ? d1 : -d1);
        double term3 = domesticRate * strikePrice * Math.exp(-domesticRate * timeToMaturity) * 
                      normalCDF(isCall ? d2 : -d2);
        
        return isCall ? term1 + term2 - term3 : term1 + term2 + term3;
    }
    
    /**
     * Calculate option rho (sensitivity to interest rate)
     * ρ = ±K * T * e^(-rT) * N(±d2)
     */
    public double calculateRho() {
        double d2 = calculateD2();
        return (isCall ? 1 : -1) * strikePrice * timeToMaturity * 
               Math.exp(-domesticRate * timeToMaturity) * normalCDF(isCall ? d2 : -d2);
    }
    
    /**
     * Standard normal probability density function
     */
    private double normalPDF(double x) {
        return Math.exp(-0.5 * x * x) / Math.sqrt(2 * Math.PI);
    }
    
    // Additional getters and setters for previous values
    public void setPreviousValues(double spotPrice, double volatility, 
                                double domesticRate, double costOfCarry) {
        this.previousSpotPrice = spotPrice;
        this.previousVolatility = volatility;
        this.previousDomesticRate = domesticRate;
        this.previousCostOfCarry = costOfCarry;
    }
    
    public double getPreviousSpotPrice() {
        return previousSpotPrice;
    }
    
    public double getPreviousVolatility() {
        return previousVolatility;
    }
    
    public double getPreviousDomesticRate() {
        return previousDomesticRate;
    }
    
    public double getPreviousCostOfCarry() {
        return previousCostOfCarry;
    }
}
