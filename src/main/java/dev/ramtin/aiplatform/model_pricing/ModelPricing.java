package dev.ramtin.aiplatform.model_pricing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "model_pricing")
public class ModelPricing {
    @Id
    private String id;
    private String model;
    private BigDecimal inputPricePerMillion;
    private BigDecimal outputPricePerMillion;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}