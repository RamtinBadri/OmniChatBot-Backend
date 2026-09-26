package dev.ramtin.aiplatform.subscription.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateSubscriptionRequest {
    @NotBlank
    private String planId;
}