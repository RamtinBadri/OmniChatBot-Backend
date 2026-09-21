package dev.ramtin.aiplatform.model_pricing;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class CostCalculator {
    private static final BigDecimal ONE_MILLION = BigDecimal.valueOf(1_000_000);

    public BigDecimal calculate(long inputTokens, long outputTokens, ModelPricing pricing) {
        BigDecimal inputCost = BigDecimal.valueOf(inputTokens).multiply(pricing.getInputPricePerMillion())
                .divide(ONE_MILLION, 12, RoundingMode.HALF_UP);

        BigDecimal outputCost = BigDecimal.valueOf(outputTokens).multiply(pricing.getOutputPricePerMillion())
                .divide(ONE_MILLION, 12, RoundingMode.HALF_UP);
        return inputCost.add(outputCost);
    }
}
