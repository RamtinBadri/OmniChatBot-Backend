package dev.ramtin.aiplatform.user.user_balance.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PurchaseCreditRequest {
    @DecimalMin(value = "0.01")
    private BigDecimal amount;
    @NotBlank
    private String currency;
    private String reference;
}