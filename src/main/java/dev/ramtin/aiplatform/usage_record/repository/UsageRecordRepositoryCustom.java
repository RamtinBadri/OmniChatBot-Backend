package dev.ramtin.aiplatform.usage_record.repository;

import dev.ramtin.aiplatform.kpi.dto.KpiUsageDTO;
import dev.ramtin.aiplatform.kpi.dto.UsageKpiSummary;
import dev.ramtin.aiplatform.usage_record.UsageRecord;
import dev.ramtin.aiplatform.usage_record.dto.UsageSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface UsageRecordRepositoryCustom {
    UsageSummary getUsageSummary(String userId);

    Page<UsageRecord> findUsageRecords(String userId, String model, Pageable pageable);

    UsageKpiSummary getUsageKpiSummary();

    List<KpiUsageDTO> getDailyKpiUsage(LocalDateTime from, LocalDateTime to);
}