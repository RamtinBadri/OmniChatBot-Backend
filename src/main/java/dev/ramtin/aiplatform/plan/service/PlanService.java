package dev.ramtin.aiplatform.plan.service;

import dev.ramtin.aiplatform.plan.Plan;

public interface PlanService {
    Plan createPlan(Plan plan);

    Plan updatePlan(String planId, Plan plan);

    Plan getPlanById(String planId);

    void deactivatePlan(String planId);
}