package dev.ramtin.aiplatform.kpi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KpiUsageDTO {
    private LocalDate date;

    private long requests;
    private long successfulRequests;
    private long failedRequests;

    private long inputTokens;
    private long outputTokens;
    private long totalTokens;

    private BigDecimal providerCost;
    private BigDecimal chargedAmount;
}