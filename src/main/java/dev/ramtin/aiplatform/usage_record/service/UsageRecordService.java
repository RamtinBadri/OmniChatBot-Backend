package dev.ramtin.aiplatform.usage_record.service;

import dev.ramtin.aiplatform.usage_record.UsageRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UsageRecordService {
    Page<UsageRecord> getUsageHistory(String userId, Pageable pageable);
}
