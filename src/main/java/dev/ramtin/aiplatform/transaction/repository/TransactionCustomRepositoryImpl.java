package dev.ramtin.aiplatform.transaction.repository;

import dev.ramtin.aiplatform.kpi.dto.TransactionKpiSummary;
import dev.ramtin.aiplatform.transaction.Transaction;
import dev.ramtin.aiplatform.transaction.TransactionStatus;
import dev.ramtin.aiplatform.transaction.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TransactionCustomRepositoryImpl implements TransactionCustomRepository {
    private final MongoTemplate mongoTemplate;

    public TransactionCustomRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Page<Transaction> findTransactions(String userId, TransactionType type, TransactionStatus status, Pageable pageable) {
        List<Criteria> criteriaList = new ArrayList<>();

        if (userId != null && !userId.isBlank()) {
            criteriaList.add(Criteria.where("userId").is(userId));
        }

        if (type != null) {
            criteriaList.add(Criteria.where("type").is(type));
        }

        if (status != null) {
            criteriaList.add(Criteria.where("status").is(status));
        }

        Query query = new Query();

        if (!criteriaList.isEmpty()) {
            query.addCriteria(
                    new Criteria().andOperator(
                            criteriaList.toArray(new Criteria[0])
                    )
            );
        }

        long total = mongoTemplate.count(query, Transaction.class);

        query.with(pageable);

        List<Transaction> transactions = mongoTemplate.find(query, Transaction.class);
        return new PageImpl<>(transactions, pageable, total);
    }

    @Override
    public TransactionKpiSummary getTransactionKpiSummary() {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(
                        Criteria.where("status").is(TransactionStatus.SUCCESS)
                ),
                Aggregation.group()
                        .sum(
                                ConditionalOperators.when(
                                        Criteria.where("type")
                                                .is(TransactionType.SUBSCRIPTION_PURCHASE)
                                ).thenValueOf("amount").otherwise(0)
                        ).as("subscriptionRevenue")
                        .sum(
                                ConditionalOperators.when(
                                        Criteria.where("type")
                                                .is(TransactionType.CREDIT_PURCHASE)
                                ).thenValueOf("amount").otherwise(0)
                        ).as("creditPurchaseRevenue")
        );

        TransactionKpiSummary result = mongoTemplate.aggregate(
                aggregation,
                Transaction.class,
                TransactionKpiSummary.class
        ).getUniqueMappedResult();

        if (result == null) {
            return new TransactionKpiSummary(BigDecimal.ZERO, BigDecimal.ZERO);
        }

        return result;
    }
}