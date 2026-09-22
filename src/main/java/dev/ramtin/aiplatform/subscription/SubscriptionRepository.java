package dev.ramtin.aiplatform.subscription;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface SubscriptionRepository extends MongoRepository<Subscription, String> {
    Optional<Subscription> findByUserIdAndStatus(String userId, SubscriptionStatus status);
}