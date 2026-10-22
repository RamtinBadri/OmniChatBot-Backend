package dev.ramtin.aiplatform.admin;

import dev.ramtin.aiplatform.admin.dto.ModelPricingRequest;
import dev.ramtin.aiplatform.admin.dto.ModelPricingCatalogDTO;
import dev.ramtin.aiplatform.model_pricing.ModelPricing;
import dev.ramtin.aiplatform.model_pricing.service.ModelPricingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/model-pricing")
@RequiredArgsConstructor
public class AdminModelPricingController {
    private final ModelPricingService modelPricingService;

    @GetMapping
    public ResponseEntity<ModelPricingCatalogDTO> getPricing() {
        return ResponseEntity.ok(new ModelPricingCatalogDTO(
                modelPricingService.getDefaultInputPricePerMillion(),
                modelPricingService.getDefaultOutputPricePerMillion(),
                modelPricingService.getAllPricing()));
    }

    @PutMapping
    public ResponseEntity<ModelPricing> savePricing(@Valid @RequestBody ModelPricingRequest request) {
        return ResponseEntity.ok(modelPricingService.savePricing(request.getModel(),
                request.getInputPricePerMillion(), request.getOutputPricePerMillion(), request.isActive()));
    }

    @DeleteMapping
    public ResponseEntity<Void> deactivatePricing(@RequestParam String model) {
        modelPricingService.deactivatePricing(model);
        return ResponseEntity.noContent().build();
    }
}
