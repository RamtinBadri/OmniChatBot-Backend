package dev.ramtin.aiplatform.subscription.service;

import dev.ramtin.aiplatform.subscription.Subscription;

import java.math.BigDecimal;

public interface SubscriptionService {
    Subscription createSubscription(String userId, String planId);

    Subscription getSubscriptionById(String subscriptionId);

    void cancelSubscription(String subscriptionId);

    void consumeCredit(String userId, BigDecimal amount);

    Subscription getCurrentSubscription(String userId);
}