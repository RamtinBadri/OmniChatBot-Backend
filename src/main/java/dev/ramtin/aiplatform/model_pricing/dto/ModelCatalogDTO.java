package dev.ramtin.aiplatform.model_pricing.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModelCatalogDTO {
    private String defaultModel;
    private Page<AvailableModelDTO> models;
}
