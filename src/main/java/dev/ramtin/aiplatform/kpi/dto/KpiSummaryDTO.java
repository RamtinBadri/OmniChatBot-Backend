package dev.ramtin.aiplatform.kpi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KpiSummaryDTO {
    private long totalUsers;
    private long activeUsers;
    private long subscribedUsers;

    private long totalRequests;
    private long successfulRequests;
    private long failedRequests;

    private long totalInputTokens;
    private long totalOutputTokens;
    private long totalTokens;

    private BigDecimal totalProviderCost;
    private BigDecimal totalChargedAmount;

    private BigDecimal subscriptionRevenue;
    private BigDecimal creditPurchaseRevenue;
    private BigDecimal totalRevenue;
}