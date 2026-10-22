package dev.ramtin.aiplatform.admin.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ModelPricingRequest {
    @NotBlank
    private String model;

    @NotNull
    @DecimalMin("0.0")
    @Digits(integer = 12, fraction = 6)
    private BigDecimal inputPricePerMillion;

    @NotNull
    @DecimalMin("0.0")
    @Digits(integer = 12, fraction = 6)
    private BigDecimal outputPricePerMillion;

    private boolean active = true;
}
