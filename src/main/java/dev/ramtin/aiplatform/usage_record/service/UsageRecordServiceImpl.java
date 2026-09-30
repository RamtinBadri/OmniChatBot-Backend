package dev.ramtin.aiplatform.usage_record.service;

import dev.ramtin.aiplatform.usage_record.UsageRecord;
import dev.ramtin.aiplatform.usage_record.repository.UsageRecordRepository;
import dev.ramtin.aiplatform.usage_record.dto.UsageSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class UsageRecordServiceImpl implements UsageRecordService {
    private final UsageRecordRepository usageRecordRepository;

    public UsageRecordServiceImpl(UsageRecordRepository usageRecordRepository) {
        this.usageRecordRepository = usageRecordRepository;
    }

    @Override
    public Page<UsageRecord> getUsageHistory(String userId, Pageable pageable) {
        return usageRecordRepository.findByUserId(userId, pageable);
    }

    @Override
    public UsageSummary getUsageSummary(String userId) {
        return usageRecordRepository.getUsageSummary(userId);
    }

    @Override
    public Page<UsageRecord> getUsageRecords(String userId, String model, Pageable pageable) {
        return usageRecordRepository.findUsageRecords(userId, model, pageable);
    }
}