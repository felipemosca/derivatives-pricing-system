# Derivatives Pricing System / Sistema de Precificação de Derivativos

## Overview / Visão Geral
This is a comprehensive Java-based system for pricing and analyzing financial derivatives, supporting:
Sistema abrangente em Java para precificação e análise de derivativos financeiros, com suporte a:

- Options (Call and Put) / Opções (Compra e Venda)
- Futures Contracts / Contratos Futuros
- Advanced Volatility Calculations / Cálculos Avançados de Volatilidade

## Features / Recursos
- Black-Scholes Option Pricing Model / Modelo Black-Scholes para Opções
- Cost of Carry Future Pricing / Precificação de Futuros por Custo de Carregamento
- Historical Volatility Calculation / Cálculo de Volatilidade Histórica
- GARCH Volatility Estimation / Estimativa de Volatilidade GARCH
- Implied Volatility Calculation / Cálculo de Volatilidade Implícita

## Prerequisites / Pré-requisitos
- Java 17+
- Maven

## Running the Application / Executando a Aplicação
```bash
mvn clean install
mvn exec:java -Dexec.mainClass="com.financialengineering.DerivativesPricingApp"
```

## Key Components / Componentes Principais
- `Derivative`: Classe base abstrata para todos os derivativos
- `Option`: Precificação de opções de compra e venda
- `Future`: Precificação de contratos futuros
- `VolatilityCalculator`: Técnicas avançadas de estimativa de volatilidade

## Supported Calculations / Cálculos Suportados
1. Option Pricing / Precificação de Opções
2. Future Contract Pricing / Precificação de Contratos Futuros
3. Historical Volatility / Volatilidade Histórica
4. GARCH Volatility / Volatilidade GARCH
5. Implied Volatility / Volatilidade Implícita

## Limitations / Limitações
- Modelos simplificados para fins educacionais e de demonstração
- Implementações do mundo real exigiriam técnicas estatísticas mais sofisticadas

## License / Licença
MIT License
