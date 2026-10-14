package dev.ramtin.aiplatform.model_pricing;

import dev.ramtin.aiplatform.model_pricing.dto.ModelCatalogDTO;
import dev.ramtin.aiplatform.model_pricing.service.ModelPricingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/models")
@RequiredArgsConstructor
public class ModelCatalogController {
    private final ModelPricingService modelPricingService;

    @GetMapping
    public ResponseEntity<ModelCatalogDTO> getAvailableModels(Pageable pageable) {
        return ResponseEntity.ok(modelPricingService.getModelCatalog(pageable));
    }
}
