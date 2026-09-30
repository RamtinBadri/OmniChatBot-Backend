package dev.ramtin.aiplatform.subscription.repository;

import dev.ramtin.aiplatform.subscription.Subscription;
import dev.ramtin.aiplatform.subscription.SubscriptionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.List;

public class SubscriptionCustomRepositoryImpl implements SubscriptionCustomRepository {
    private final MongoTemplate mongoTemplate;

    public SubscriptionCustomRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Page<Subscription> findSubscriptions(String userId, String planId, SubscriptionStatus status,
                                                Pageable pageable) {
        List<Criteria> criteriaList = new ArrayList<>();

        if (userId != null && !userId.isBlank()) {
            criteriaList.add(Criteria.where("userId").is(userId));
        }

        if (planId != null && !planId.isBlank()) {
            criteriaList.add(Criteria.where("planId").is(planId));
        }

        if (status != null) {
            criteriaList.add(Criteria.where("status").is(status));
        }

        Query query = new Query();

        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        long total = mongoTemplate.count(query, Subscription.class);

        query.with(pageable);

        List<Subscription> subscriptions = mongoTemplate.find(query, Subscription.class);

        return new PageImpl<>(subscriptions, pageable, total);
    }
}