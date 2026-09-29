package dev.ramtin.aiplatform.plan.service;

import dev.ramtin.aiplatform.plan.Plan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PlanService {
    Plan createPlan(Plan plan);

    Plan updatePlan(String planId, Plan plan);

    Plan getPlanById(String planId);

    void deactivatePlan(String planId);

    Page<Plan> getActivePlans(Pageable pageable);
}