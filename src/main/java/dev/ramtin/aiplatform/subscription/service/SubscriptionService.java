package dev.ramtin.aiplatform.subscription.service;

import dev.ramtin.aiplatform.subscription.Subscription;

public interface SubscriptionService {
    Subscription createSubscription(String userId, String planId);

    Subscription getSubscriptionById(String subscriptionId);

    void cancelSubscription(String subscriptionId);
}