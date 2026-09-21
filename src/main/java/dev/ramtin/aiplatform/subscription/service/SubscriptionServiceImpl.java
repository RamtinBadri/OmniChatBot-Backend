package dev.ramtin.aiplatform.subscription.service;

import dev.ramtin.aiplatform.exception.ResourceNotFoundException;
import dev.ramtin.aiplatform.plan.Plan;
import dev.ramtin.aiplatform.plan.PlanRepository;
import dev.ramtin.aiplatform.subscription.Subscription;
import dev.ramtin.aiplatform.subscription.SubscriptionRepository;
import dev.ramtin.aiplatform.subscription.SubscriptionStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;

    public SubscriptionServiceImpl(SubscriptionRepository subscriptionRepository, PlanRepository planRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.planRepository = planRepository;
    }

    @Override
    public Subscription createSubscription(String userId, String planId) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Plan not found with id: " + planId
                        )
                );

        if (!plan.isActive()) {
            throw new IllegalStateException("Cannot create subscription for inactive plan");
        }

        LocalDateTime startDate = LocalDateTime.now();

        LocalDateTime endDate = startDate.plusDays(
                plan.getDuration()
        );

        Subscription subscription = Subscription.builder()
                .userId(userId)
                .planId(planId)
                .pricePaid(plan.getPrice())
                .startDate(startDate)
                .endDate(endDate)
                .status(SubscriptionStatus.ACTIVE)
                .remainingCredit(plan.getCreditLimit())
                .build();

        return subscriptionRepository.save(subscription);
    }

    @Override
    public Subscription getSubscriptionById(String subscriptionId) {
        return subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Subscription not found with id: "
                                        + subscriptionId
                        )
                );
    }

    @Override
    public void cancelSubscription(String subscriptionId) {
        Subscription subscription = getSubscriptionById(subscriptionId);

        subscription.setStatus(SubscriptionStatus.CANCELLED);

        subscriptionRepository.save(subscription);
    }
}