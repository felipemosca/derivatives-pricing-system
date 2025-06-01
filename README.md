# Derivatives Pricing System

## Overview
This is a comprehensive Java-based system for pricing and analyzing financial derivatives, supporting:
- Options (Call and Put)
- Futures Contracts
- Advanced Volatility Calculations

## Features
- Black-Scholes Option Pricing Model
- Cost of Carry Future Pricing
- Historical Volatility Calculation
- GARCH Volatility Estimation
- Implied Volatility Calculation

## Prerequisites
- Java 17+
- Maven

## Running the Application
```bash
mvn clean install
mvn exec:java -Dexec.mainClass="com.financialengineering.DerivativesPricingApp"
```

## Key Components
- `Derivative`: Abstract base class for all derivatives
- `Option`: Call and Put option pricing
- `Future`: Future contract pricing
- `VolatilityCalculator`: Advanced volatility estimation techniques

## Supported Calculations
1. Option Pricing
2. Future Contract Pricing
3. Historical Volatility
4. GARCH Volatility
5. Implied Volatility

## Limitations
- Simplified models for educational and demonstration purposes
- Real-world implementations would require more sophisticated statistical techniques

## License
MIT License
