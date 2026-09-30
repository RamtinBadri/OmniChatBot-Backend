package dev.ramtin.aiplatform.subscription.repository;

import dev.ramtin.aiplatform.subscription.Subscription;
import dev.ramtin.aiplatform.subscription.SubscriptionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface SubscriptionRepository extends MongoRepository<Subscription, String>, SubscriptionCustomRepository {
    Optional<Subscription> findByUserIdAndStatus(String userId, SubscriptionStatus status);

    Page<Subscription> findByPlanId(String planId, Pageable pageable);
}