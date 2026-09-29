package dev.ramtin.aiplatform.usage_record;

import dev.ramtin.aiplatform.usage_record.dto.UsageSummary;

public interface UsageRecordRepositoryCustom {
    UsageSummary getUsageSummary(String userId);
}