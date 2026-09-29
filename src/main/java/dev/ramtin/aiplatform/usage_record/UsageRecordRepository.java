package dev.ramtin.aiplatform.usage_record;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UsageRecordRepository extends MongoRepository<UsageRecord, String>, UsageRecordRepositoryCustom {
    Page<UsageRecord> findByUserId(String userId, Pageable pageable);
}