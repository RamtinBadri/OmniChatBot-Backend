package dev.ramtin.aiplatform.model_pricing.service;

import dev.ramtin.aiplatform.config.DefaultPricingProperties;
import dev.ramtin.aiplatform.exception.ResourceNotFoundException;
import dev.ramtin.aiplatform.config.AIProperties;
import dev.ramtin.aiplatform.model_pricing.ModelPricing;
import dev.ramtin.aiplatform.model_pricing.dto.AvailableModelDTO;
import dev.ramtin.aiplatform.model_pricing.dto.ModelCatalogDTO;
import dev.ramtin.aiplatform.model_pricing.repository.ModelPricingRepository;
import dev.ramtin.aiplatform.omniroute.response.OmniRouteModel;
import dev.ramtin.aiplatform.omniroute.service.OmniRouteClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Service
public class ModelPricingServiceImpl implements ModelPricingService {
    private final ModelPricingRepository modelPricingRepository;
    private final DefaultPricingProperties defaultPricingProperties;
    private final AIProperties aiProperties;
    private final OmniRouteClient omniRouteClient;

    public ModelPricingServiceImpl(ModelPricingRepository modelPricingRepository,
                                   DefaultPricingProperties defaultPricingProperties,
                                   AIProperties aiProperties,
                                   OmniRouteClient omniRouteClient) {
        this.modelPricingRepository = modelPricingRepository;
        this.defaultPricingProperties = defaultPricingProperties;
        this.aiProperties = aiProperties;
        this.omniRouteClient = omniRouteClient;
    }

    @Override
    public ModelCatalogDTO getModelCatalog(Pageable pageable) {
        List<AvailableModelDTO> allModels = omniRouteClient.getModels().getData().stream()
                .filter(model -> model.getId() != null && !model.getId().isBlank())
                .map(model -> new AvailableModelDTO(
                        model.getId(),
                        model.getName() == null || model.getName().isBlank() ? model.getId() : model.getName(),
                        model.getId().equals(aiProperties.getDefaultModel())))
                .toList();
        int start = (int) Math.min(pageable.getOffset(), allModels.size());
        int end = Math.min(start + pageable.getPageSize(), allModels.size());
        Page<AvailableModelDTO> modelsPage = new PageImpl<>(allModels.subList(start, end), pageable, allModels.size());
        return new ModelCatalogDTO(aiProperties.getDefaultModel(), modelsPage);
    }

    @Override
    public boolean isAvailable(String model) {
        if (model == null || model.isBlank()) return false;
        if (model.equals(aiProperties.getDefaultModel())) return true;
        return omniRouteClient.getModels().getData().stream()
                .anyMatch(available -> model.equals(available.getId()));
    }

    @Override
    public List<ModelPricing> getAllPricing() {
        return modelPricingRepository.findAll();
    }

    @Override
    public BigDecimal getDefaultInputPricePerMillion() {
        return defaultPricingProperties.getDefaultInputPricePerMillion();
    }

    @Override
    public BigDecimal getDefaultOutputPricePerMillion() {
        return defaultPricingProperties.getDefaultOutputPricePerMillion();
    }

    @Override
    public ModelPricing savePricing(String model, BigDecimal inputPricePerMillion,
                                    BigDecimal outputPricePerMillion, boolean active) {
        if (model == null || model.isBlank()) throw new IllegalArgumentException("Model cannot be empty");
        if (model.trim().length() > 200) throw new IllegalArgumentException("Model identifier is too long");
        validatePrice(inputPricePerMillion, "Input price");
        validatePrice(outputPricePerMillion, "Output price");

        LocalDateTime now = LocalDateTime.now();
        ModelPricing pricing = modelPricingRepository.findByModel(model.trim()).orElse(null);
        if (pricing == null) {
            if (!isAvailable(model.trim())) throw new IllegalArgumentException("Model is not available: " + model.trim());
            pricing = ModelPricing.builder().model(model.trim()).createdAt(now).build();
        }
        pricing.setInputPricePerMillion(inputPricePerMillion);
        pricing.setOutputPricePerMillion(outputPricePerMillion);
        pricing.setActive(active);
        pricing.setUpdatedAt(now);
        return modelPricingRepository.save(pricing);
    }

    @Override
    public void deactivatePricing(String model) {
        ModelPricing pricing = modelPricingRepository.findByModel(model)
                .orElseThrow(() -> new ResourceNotFoundException("Model pricing not found: " + model));
        pricing.setActive(false);
        pricing.setUpdatedAt(LocalDateTime.now());
        modelPricingRepository.save(pricing);
    }

    private void validatePrice(BigDecimal price, String label) {
        if (price == null || price.signum() < 0 || price.precision() > 18 || price.scale() > 6) {
            throw new IllegalArgumentException(label + " must be non-negative with at most 18 digits and 6 decimal places");
        }
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