package dev.ramtin.aiplatform.model_pricing.service;

import dev.ramtin.aiplatform.model_pricing.ModelPricing;
import dev.ramtin.aiplatform.model_pricing.dto.ModelCatalogDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface ModelPricingService {
    ModelPricing getPricing(String model);

    ModelCatalogDTO getModelCatalog(Pageable pageable);

    boolean isAvailable(String model);

    List<ModelPricing> getAllPricing();

    BigDecimal getDefaultInputPricePerMillion();

    BigDecimal getDefaultOutputPricePerMillion();

    ModelPricing savePricing(String model, BigDecimal inputPricePerMillion,
                             BigDecimal outputPricePerMillion, boolean active);

    void deactivatePricing(String model);
}