package dev.ramtin.aiplatform.model_pricing.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvailableModelDTO {
    private String id;
    private String name;
    private boolean defaultModel;
}
