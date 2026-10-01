package dev.ramtin.aiplatform.ai.request.repository;

import dev.ramtin.aiplatform.kpi.dto.KpiUsageDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface AIRequestRepositoryCustom {
    List<KpiUsageDTO> getDailyKpiUsage(LocalDateTime from, LocalDateTime to);
}