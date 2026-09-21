package dev.ramtin.aiplatform.subscription;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface SubscriptionRepository
        extends MongoRepository<Subscription, String> {
}