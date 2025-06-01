package com.financialengineering;

import com.financialengineering.utils.MathUtils;
import java.util.List;

/**
 * CORE0Calculator - Calcula o risco CORE0 conforme metodologia do capítulo 7 do manual B3.
 * Considera posições alocadas e garantias do comitente.
 */
public class CORE0Calculator {
    /**
     * Representa um cenário de estresse de mercado para avaliação de risco.
     */
    public static class StressScenario {
        public final String id;
        public final double[] priceShocks; // Vetor de choques de preço por ativo

        public StressScenario(String id, double[] priceShocks) {
            this.id = id;
            this.priceShocks = priceShocks;
        }
    }

    /**
     * Representa uma posição em um ativo.
     */
    public static class Position {
        public final String assetId;
        public final double quantity;
        public final double price;

        public Position(String assetId, double quantity, double price) {
            this.assetId = assetId;
            this.quantity = quantity;
            this.price = price;
        }
    }

    /**
     * Representa uma garantia (collateral) depositada.
     */
    public static class Collateral {
        public final String assetId;
        public final double value;

        public Collateral(String assetId, double value) {
            this.assetId = assetId;
            this.value = value;
        }
    }

    /**
     * Calcula o resultado da liquidação das posições + garantias sob um cenário.
     * @param positions Lista de posições do comitente
     * @param collaterals Lista de garantias depositadas
     * @param scenario Cenário de estresse
     * @return Resultado financeiro sob o cenário
     */
    public static double calculateResult(List<Position> positions, List<Collateral> collaterals, StressScenario scenario) {
        double result = 0.0;
        // Ajusta posições
        for (Position pos : positions) {
            // Supondo que priceShocks está alinhado com a ordem dos ativos
            double shockedPrice = pos.price + getShockForAsset(pos.assetId, scenario);
            result += pos.quantity * shockedPrice;
        }
        // Soma garantias
        for (Collateral col : collaterals) {
            result += col.value;
        }
        return result;
    }

    /**
     * Busca o choque de preço para o ativo no cenário. (Implementação simplificada)
     */
    private static double getShockForAsset(String assetId, StressScenario scenario) {
        // Em um sistema real, mapeie assetId para o índice correto do vetor de choques
        // Aqui, simplificado: todos ativos recebem o mesmo choque
        return scenario.priceShocks.length > 0 ? scenario.priceShocks[0] : 0.0;
    }

    /**
     * Calcula o risco residual CORE0 (RiscoRes) e a chamada de margem.
     * @param positions Posições do comitente
     * @param collaterals Garantias depositadas
     * @param scenarios Lista de cenários de estresse
     * @return Valor da chamada de margem (>=0 se houver insuficiência de garantias)
     */
    public static double calculateCORE0Margin(List<Position> positions, List<Collateral> collaterals, List<StressScenario> scenarios) {
        double minResult = Double.POSITIVE_INFINITY;
        for (StressScenario scenario : scenarios) {
            double result = calculateResult(positions, collaterals, scenario);
            if (result < minResult) {
                minResult = result;
            }
        }
        double riscoRes = -minResult;
        return Math.max(riscoRes, 0.0); // Chamada de margem não pode ser negativa
    }
}
