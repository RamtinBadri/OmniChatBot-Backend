package dev.ramtin.aiplatform.model_pricing.service;

import dev.ramtin.aiplatform.model_pricing.ModelPricing;

public interface ModelPricingService {
    ModelPricing getPricing(String model);
}