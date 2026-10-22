package dev.ramtin.aiplatform.admin.dto;

import dev.ramtin.aiplatform.model_pricing.ModelPricing;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class ModelPricingCatalogDTO {
    private BigDecimal defaultInputPricePerMillion;
    private BigDecimal defaultOutputPricePerMillion;
    private List<ModelPricing> pricing;
}
