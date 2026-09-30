package dev.ramtin.aiplatform.subscription;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SubscriptionCustomRepository {
    Page<Subscription> findSubscriptions(String userId, String planId, SubscriptionStatus status,
            Pageable pageable);
}