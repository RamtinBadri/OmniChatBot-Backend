package dev.ramtin.aiplatform.subscription.service;

import dev.ramtin.aiplatform.exception.ResourceNotFoundException;
import dev.ramtin.aiplatform.plan.Plan;
import dev.ramtin.aiplatform.plan.PlanRepository;
import dev.ramtin.aiplatform.subscription.Subscription;
import dev.ramtin.aiplatform.subscription.SubscriptionRepository;
import dev.ramtin.aiplatform.subscription.SubscriptionStatus;
import dev.ramtin.aiplatform.transaction.Transaction;
import dev.ramtin.aiplatform.transaction.TransactionStatus;
import dev.ramtin.aiplatform.transaction.TransactionType;
import dev.ramtin.aiplatform.transaction.service.TransactionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;
    private final TransactionService transactionService;

    public SubscriptionServiceImpl(SubscriptionRepository subscriptionRepository, PlanRepository planRepository,
                                   TransactionService transactionService) {
        this.subscriptionRepository = subscriptionRepository;
        this.planRepository = planRepository;
        this.transactionService = transactionService;
    }

    @Override
    public Subscription createSubscription(String userId, String planId) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found with id: " + planId));

        if (!plan.isActive()) {
            throw new IllegalStateException("Cannot create subscription for inactive plan");
        }

        LocalDateTime startDate = LocalDateTime.now();

        LocalDateTime endDate = startDate.plusDays(plan.getDuration());

        Subscription subscription = Subscription.builder()
                .userId(userId)
                .planId(planId)
                .pricePaid(plan.getPrice())
                .startDate(startDate)
                .endDate(endDate)
                .status(SubscriptionStatus.ACTIVE)
                .remainingCredit(plan.getCreditLimit())
                .build();

        subscription = subscriptionRepository.save(subscription);

        Transaction transaction = transactionService.createTransaction(userId, TransactionType.SUBSCRIPTION_PURCHASE,
                plan.getPrice(), "USD", "subscription" + subscription.getId());

        transactionService.updateStatus(transaction.getId(), TransactionStatus.SUCCESS);
        return subscription;
    }

    @Override
    public Subscription getSubscriptionById(String subscriptionId) {
        return subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + subscriptionId));
    }

    @Override
    public void cancelSubscription(String subscriptionId) {
        Subscription subscription = getSubscriptionById(subscriptionId);
        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscriptionRepository.save(subscription);
    }

    @Override
    public void consumeCredit(String userId, BigDecimal amount) {
        Subscription subscription = subscriptionRepository
                .findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Active subscription not found"));

        if (subscription.getEndDate().isBefore(LocalDateTime.now())) {
            subscription.setStatus(SubscriptionStatus.EXPIRED);
            subscriptionRepository.save(subscription);

            throw new IllegalStateException("Subscription has expired");
        }

        if (subscription.getRemainingCredit().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient subscription credit");
        }

        subscription.setRemainingCredit(subscription.getRemainingCredit().subtract(amount));

        subscriptionRepository.save(subscription);
    }

    @Override
    public Subscription getCurrentSubscription(String userId) {
        return subscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("No active subscription found"));
    }

    @Override
    public Page<Subscription> getSubscriptions(String userId, String planId, SubscriptionStatus status, Pageable pageable) {
        return subscriptionRepository.findSubscriptions(userId, planId, status, pageable);
    }
}