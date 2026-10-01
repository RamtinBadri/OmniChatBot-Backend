package dev.ramtin.aiplatform.usage_record.repository;

import dev.ramtin.aiplatform.kpi.dto.KpiUsageDTO;
import dev.ramtin.aiplatform.kpi.dto.UsageKpiSummary;
import dev.ramtin.aiplatform.usage_record.UsageRecord;
import dev.ramtin.aiplatform.usage_record.dto.UsageSummary;
import org.bson.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.DateOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

    @Override
    public UsageKpiSummary getUsageKpiSummary() {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.group()
                        .sum("inputTokens").as("totalInputTokens")
                        .sum("outputTokens").as("totalOutputTokens")
                        .sum("totalTokens").as("totalTokens")
                        .sum("providerCost").as("totalProviderCost")
                        .sum("chargedAmount").as("totalChargedAmount")
        );

        UsageKpiSummary result = mongoTemplate.aggregate(aggregation, UsageRecord.class, UsageKpiSummary.class).getUniqueMappedResult();

        if (result == null) {
            return new UsageKpiSummary(
                    0,
                    0,
                    0,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO
            );
        }
        return result;
    }

    @Override
    public List<KpiUsageDTO> getDailyKpiUsage(LocalDateTime from, LocalDateTime to) {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("timestamp").gte(from).lt(to)),
                Aggregation.project()
                        .and(
                                DateOperators.DateToString
                                        .dateOf("timestamp")
                                        .toString("%Y-%m-%d")
                        )
                        .as("date")
                        .andInclude(
                                "inputTokens",
                                "outputTokens",
                                "totalTokens",
                                "providerCost",
                                "chargedAmount"
                        ),

                Aggregation.group("date")
                        .sum("inputTokens")
                        .as("inputTokens")
                        .sum("outputTokens")
                        .as("outputTokens")
                        .sum("totalTokens")
                        .as("totalTokens")
                        .sum("providerCost")
                        .as("providerCost")
                        .sum("chargedAmount")
                        .as("chargedAmount"),

                Aggregation.project()
                        .and("_id")
                        .as("date")
                        .andInclude(
                                "inputTokens",
                                "outputTokens",
                                "totalTokens",
                                "providerCost",
                                "chargedAmount"
                        ),

                Aggregation.sort(Sort.Direction.ASC, "date")
        );

        List<Document> results = mongoTemplate.aggregate(aggregation, UsageRecord.class, Document.class).getMappedResults();
        List<KpiUsageDTO> dailyUsage = new ArrayList<>();

        for (Document result : results) {
            Number providerCostValue = result.get("providerCost", Number.class);
            Number chargedAmountValue = result.get("chargedAmount", Number.class);

            BigDecimal providerCost = providerCostValue != null ? BigDecimal.valueOf(providerCostValue.doubleValue())
                            : BigDecimal.ZERO;

            BigDecimal chargedAmount = chargedAmountValue != null ? BigDecimal.valueOf(chargedAmountValue.doubleValue())
                            : BigDecimal.ZERO;

            KpiUsageDTO dto = KpiUsageDTO.builder()
                    .date(LocalDate.parse(result.getString("date")))
                    .requests(0)
                    .successfulRequests(0)
                    .failedRequests(0)
                    .inputTokens(result.getLong("inputTokens"))
                    .outputTokens(result.getLong("outputTokens"))
                    .totalTokens(result.getLong("totalTokens"))
                    .providerCost(providerCost)
                    .chargedAmount(chargedAmount)
                    .build();
            dailyUsage.add(dto);
        }
        return dailyUsage;
    }
}