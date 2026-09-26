package dev.ramtin.aiplatform.user.dto;

import dev.ramtin.aiplatform.user.CreditSource;
import lombok.Data;

@Data
public class UpdateCreditSourceRequest {
    private CreditSource creditSource;
}