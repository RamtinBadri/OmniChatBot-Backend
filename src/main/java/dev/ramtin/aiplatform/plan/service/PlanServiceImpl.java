package dev.ramtin.aiplatform.plan.service;

import dev.ramtin.aiplatform.exception.ResourceNotFoundException;
import dev.ramtin.aiplatform.plan.Plan;
import dev.ramtin.aiplatform.plan.PlanRepository;
import dev.ramtin.aiplatform.subscription.Subscription;
import dev.ramtin.aiplatform.subscription.SubscriptionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class PlanServiceImpl implements PlanService {
    private final PlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;

    public PlanServiceImpl(PlanRepository planRepository, SubscriptionRepository subscriptionRepository) {
        this.planRepository = planRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    @Override
    public Plan createPlan(Plan plan) {
        validatePlan(plan);

        LocalDateTime now = LocalDateTime.now();

        plan.setCreatedAt(now);
        plan.setUpdatedAt(now);

        return planRepository.save(plan);
    }

    @Override
    public Plan updatePlan(String planId, Plan plan) {
        Plan existingPlan = planRepository.findById(planId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Plan not found with id: " + planId
                        )
                );

        validatePlan(plan);

        existingPlan.setName(plan.getName());
        existingPlan.setPrice(plan.getPrice());
        existingPlan.setDuration(plan.getDuration());
        existingPlan.setCreditLimit(plan.getCreditLimit());
        existingPlan.setActive(plan.isActive());
        existingPlan.setUpdatedAt(LocalDateTime.now());

        return planRepository.save(existingPlan);
    }

    @Override
    public Plan getPlanById(String planId) {
        return planRepository.findById(planId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Plan not found with id: " + planId
                        )
                );
    }

    @Override
    public void deactivatePlan(String planId) {
        Plan plan = getPlanById(planId);

        plan.setActive(false);
        plan.setUpdatedAt(LocalDateTime.now());

        planRepository.save(plan);
    }

    @Override
    public Page<Plan> getActivePlans(Pageable pageable) {
        return planRepository.findByActiveTrue(pageable);
    }

    @Override
    public Page<Subscription> getSubscriptionsByPlan(String planId, Pageable pageable) {
        getPlanById(planId);
        return subscriptionRepository.findByPlanId(planId, pageable);
    }

    private void validatePlan(Plan plan) {
        if (plan.getName() == null || plan.getName().isBlank()) {
            throw new IllegalArgumentException(
                    "Plan name cannot be empty"
            );
        }

        if (plan.getPrice() == null || plan.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Plan price cannot be negative"
            );
        }

        if (plan.getDuration() <= 0) {
            throw new IllegalArgumentException(
                    "Plan duration must be greater than zero"
            );
        }

        if (plan.getCreditLimit() == null || plan.getCreditLimit().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Plan credit limit cannot be negative"
            );
        }
    }
}