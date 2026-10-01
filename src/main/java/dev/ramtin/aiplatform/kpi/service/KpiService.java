package dev.ramtin.aiplatform.kpi.service;

import dev.ramtin.aiplatform.kpi.dto.KpiSummaryDTO;
import dev.ramtin.aiplatform.kpi.dto.KpiUsageDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface KpiService {
    KpiSummaryDTO getSummary();

    List<KpiUsageDTO> getUsage(LocalDateTime from, LocalDateTime to);
}