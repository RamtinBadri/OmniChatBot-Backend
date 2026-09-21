package dev.ramtin.aiplatform.model_pricing;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CostCalculatorTest {
    private final CostCalculator costCalculator = new CostCalculator();

    @Test
    void calculate_shouldCalculateTotalCostCorrectly() {
        ModelPricing pricing = ModelPricing.builder()
                .model("minimax-m2.5")
                .inputPricePerMillion(new BigDecimal("0.50"))
                .outputPricePerMillion(new BigDecimal("2.00"))
                .active(true)
                .build();

        BigDecimal result = costCalculator.calculate(4405, 31, pricing);

        BigDecimal expected = new BigDecimal("0.002264500000");

        assertEquals(0, expected.compareTo(result));
    }
}