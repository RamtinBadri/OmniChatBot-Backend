package dev.ramtin.aiplatform.usage_record.repository;

import dev.ramtin.aiplatform.usage_record.UsageRecord;
import dev.ramtin.aiplatform.usage_record.dto.UsageSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Repository
public class UsageRecordRepositoryCustomImpl implements UsageRecordRepositoryCustom {
    private final MongoTemplate mongoTemplate;

    public UsageRecordRepositoryCustomImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public UsageSummary getUsageSummary(String userId) {
        Aggregation aggregation = Aggregation.newAggregation(

                Aggregation.match(Criteria.where("userId").is(userId)),

                Aggregation.group()
                        .count().as("totalRequests")
                        .sum("inputTokens").as("totalInputTokens")
                        .sum("outputTokens").as("totalOutputTokens")
                        .sum("totalTokens").as("totalTokens")
                        .sum("chargedAmount").as("totalCost")
        );

        UsageSummary summary = mongoTemplate
                .aggregate(aggregation, "usage_records", UsageSummary.class)
                .getUniqueMappedResult();

        if (summary == null) {
            return new UsageSummary(
                    0,
                    0,
                    0,
                    0,
                    BigDecimal.ZERO
            );
        }
        return summary;
    }

    @Override
    public Page<UsageRecord> findUsageRecords(String userId, String model, Pageable pageable) {
        List<Criteria> criteriaList = new ArrayList<>();

        if (userId != null && !userId.isBlank()) {
            criteriaList.add(Criteria.where("userId").is(userId));
        }

        if (model != null && !model.isBlank()) {
            criteriaList.add(Criteria.where("model").is(model));
        }

        Query query = new Query();

        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        long total = mongoTemplate.count(query, UsageRecord.class);
        query.with(pageable);
        List<UsageRecord> usageRecords = mongoTemplate.find(query, UsageRecord.class);
        return new PageImpl<>(usageRecords, pageable, total);
    }
}