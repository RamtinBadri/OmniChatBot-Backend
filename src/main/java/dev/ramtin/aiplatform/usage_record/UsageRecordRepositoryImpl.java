package dev.ramtin.aiplatform.usage_record;

import dev.ramtin.aiplatform.usage_record.dto.UsageSummary;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public class UsageRecordRepositoryImpl implements UsageRecordRepositoryCustom {
    private final MongoTemplate mongoTemplate;

    public UsageRecordRepositoryImpl(MongoTemplate mongoTemplate) {
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
}