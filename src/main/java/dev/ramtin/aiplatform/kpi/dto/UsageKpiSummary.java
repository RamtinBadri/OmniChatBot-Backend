package dev.ramtin.aiplatform.kpi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsageKpiSummary {
    private long totalInputTokens;
    private long totalOutputTokens;
    private long totalTokens;

    private BigDecimal totalProviderCost;
    private BigDecimal totalChargedAmount;
}