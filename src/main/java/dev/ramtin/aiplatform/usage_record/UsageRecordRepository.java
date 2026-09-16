package dev.ramtin.aiplatform.usage_record;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface UsageRecordRepository extends MongoRepository<UsageRecord, String> {
}