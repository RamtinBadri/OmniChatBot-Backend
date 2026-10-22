package dev.ramtin.aiplatform.model_pricing.repository;

import dev.ramtin.aiplatform.model_pricing.ModelPricing;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ModelPricingRepository extends MongoRepository<ModelPricing, String> {
    Optional<ModelPricing> findByModelAndActiveTrue(String model);

    Optional<ModelPricing> findByModel(String model);
}