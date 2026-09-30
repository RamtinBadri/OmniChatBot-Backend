package dev.ramtin.aiplatform.subscription.service;

import dev.ramtin.aiplatform.subscription.Subscription;
import dev.ramtin.aiplatform.subscription.SubscriptionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface SubscriptionService {
    Subscription createSubscription(String userId, String planId);

    Subscription getSubscriptionById(String subscriptionId);

    void cancelSubscription(String subscriptionId);

    void consumeCredit(String userId, BigDecimal amount);

    Subscription getCurrentSubscription(String userId);

    Page<Subscription> getSubscriptions(String userId, String planId, SubscriptionStatus status, Pageable pageable);
}