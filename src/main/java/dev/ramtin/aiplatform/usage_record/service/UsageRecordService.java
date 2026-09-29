package dev.ramtin.aiplatform.usage_record.service;

import dev.ramtin.aiplatform.usage_record.UsageRecord;
import dev.ramtin.aiplatform.usage_record.dto.UsageSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UsageRecordService {
    Page<UsageRecord> getUsageHistory(String userId, Pageable pageable);

    UsageSummary getUsageSummary(String userId);
}
