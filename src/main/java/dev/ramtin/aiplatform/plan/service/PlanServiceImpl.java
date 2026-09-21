package dev.ramtin.aiplatform.plan.service;

import dev.ramtin.aiplatform.exception.ResourceNotFoundException;
import dev.ramtin.aiplatform.plan.Plan;
import dev.ramtin.aiplatform.plan.PlanRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class PlanServiceImpl implements PlanService {
    private final PlanRepository planRepository;

    public PlanServiceImpl(PlanRepository planRepository) {
        this.planRepository = planRepository;
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