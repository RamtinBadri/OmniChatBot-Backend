package dev.ramtin.aiplatform.subscription.repository;

import dev.ramtin.aiplatform.subscription.Subscription;
import dev.ramtin.aiplatform.subscription.SubscriptionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SubscriptionCustomRepository {
    Page<Subscription> findSubscriptions(String userId, String planId, SubscriptionStatus status,
                                         Pageable pageable);

    long countActiveSubscribers();
}