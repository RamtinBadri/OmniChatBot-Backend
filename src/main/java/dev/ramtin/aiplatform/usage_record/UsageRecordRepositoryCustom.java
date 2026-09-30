package dev.ramtin.aiplatform.usage_record;

import dev.ramtin.aiplatform.usage_record.dto.UsageSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UsageRecordRepositoryCustom {
    UsageSummary getUsageSummary(String userId);

    Page<UsageRecord> findUsageRecords(String userId, String model, Pageable pageable);
}