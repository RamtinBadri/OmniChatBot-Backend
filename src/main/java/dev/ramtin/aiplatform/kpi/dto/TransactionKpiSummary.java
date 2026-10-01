package dev.ramtin.aiplatform.kpi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionKpiSummary {
    private BigDecimal subscriptionRevenue;
    private BigDecimal creditPurchaseRevenue;
}