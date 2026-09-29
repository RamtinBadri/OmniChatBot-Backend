package dev.ramtin.aiplatform.usage_record.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsageSummary {
    private long totalRequests;
    private long totalInputTokens;
    private long totalOutputTokens;
    private long totalTokens;
    private BigDecimal totalCost;
}