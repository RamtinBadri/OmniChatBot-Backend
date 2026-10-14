package dev.ramtin.aiplatform.model_pricing.service;

import dev.ramtin.aiplatform.model_pricing.ModelPricing;
import dev.ramtin.aiplatform.model_pricing.dto.ModelCatalogDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ModelPricingService {
    ModelPricing getPricing(String model);

    ModelCatalogDTO getModelCatalog(Pageable pageable);

    boolean isAvailable(String model);
}