package dev.ramtin.aiplatform.model_pricing.service;

import dev.ramtin.aiplatform.config.DefaultPricingProperties;
import dev.ramtin.aiplatform.model_pricing.ModelPricing;
import dev.ramtin.aiplatform.model_pricing.repository.ModelPricingRepository;
import org.springframework.stereotype.Service;

@Service
public class ModelPricingServiceImpl implements ModelPricingService {
    private final ModelPricingRepository modelPricingRepository;
    private final DefaultPricingProperties defaultPricingProperties;

    public ModelPricingServiceImpl(ModelPricingRepository modelPricingRepository, DefaultPricingProperties defaultPricingProperties) {
        this.modelPricingRepository = modelPricingRepository;
        this.defaultPricingProperties = defaultPricingProperties;
    }

    @Override
    public ModelPricing getPricing(String model) {
        return modelPricingRepository
                .findByModelAndActiveTrue(model)
                .orElseGet(() ->
                        ModelPricing.builder()
                                .model(model)
                                .inputPricePerMillion(
                                        defaultPricingProperties
                                                .getDefaultInputPricePerMillion()
                                )
                                .outputPricePerMillion(
                                        defaultPricingProperties
                                                .getDefaultOutputPricePerMillion()
                                )
                                .active(true)
                                .build()
                );
    }
}