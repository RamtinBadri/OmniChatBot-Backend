package dev.ramtin.aiplatform.ai.request.repository;

import dev.ramtin.aiplatform.ai.request.AIRequest;
import dev.ramtin.aiplatform.ai.request.AIRequestStatus;
import dev.ramtin.aiplatform.kpi.dto.KpiUsageDTO;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators;
import org.springframework.data.mongodb.core.aggregation.DateOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class AIRequestRepositoryCustomImpl implements AIRequestRepositoryCustom {
    private final MongoTemplate mongoTemplate;

    @Override
    public List<KpiUsageDTO> getDailyKpiUsage(LocalDateTime from, LocalDateTime to) {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("requestTime").gte(from).lt(to)),

                Aggregation.project()
                        .and(
                                DateOperators.DateToString
                                        .dateOf("requestTime")
                                        .toString("%Y-%m-%d")
                        )
                        .as("date")
                        .andInclude("status"),

                Aggregation.group("date")
                        .count().as("requests")
                        .sum(
                                ConditionalOperators.when(
                                        Criteria.where("status")
                                                .is(AIRequestStatus.SUCCESS)
                                ).then(1).otherwise(0)
                        ).as("successfulRequests")
                        .sum(
                                ConditionalOperators.when(
                                        Criteria.where("status")
                                                .in(
                                                        AIRequestStatus.FAILED,
                                                        AIRequestStatus.TIMEOUT,
                                                        AIRequestStatus.RATE_LIMITED,
                                                        AIRequestStatus.PROVIDER_ERROR
                                                )
                                ).then(1).otherwise(0)
                        ).as("failedRequests"),

                Aggregation.project()
                        .and("_id").as("date")
                        .andInclude(
                                "requests",
                                "successfulRequests",
                                "failedRequests"
                        ),

                Aggregation.sort(
                        Sort.Direction.ASC,
                        "date"
                )
        );

        List<Document> results = mongoTemplate.aggregate(aggregation, AIRequest.class, Document.class).getMappedResults();
        List<KpiUsageDTO> dailyUsage = new ArrayList<>();

        for (Document result : results) {
            KpiUsageDTO dto = KpiUsageDTO.builder()
                    .date(LocalDate.parse(result.getString("date")))
                    .requests(result.getInteger("requests", 0))
                    .successfulRequests(result.getInteger("successfulRequests", 0))
                    .failedRequests(result.getInteger("failedRequests", 0))
                    .inputTokens(0)
                    .outputTokens(0)
                    .totalTokens(0)
                    .providerCost(java.math.BigDecimal.ZERO)
                    .chargedAmount(java.math.BigDecimal.ZERO)
                    .build();

            dailyUsage.add(dto);
        }
        return dailyUsage;
    }
}