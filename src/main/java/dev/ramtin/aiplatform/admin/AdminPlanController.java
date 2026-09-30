package dev.ramtin.aiplatform.admin;

import dev.ramtin.aiplatform.plan.Plan;
import dev.ramtin.aiplatform.plan.service.PlanService;
import dev.ramtin.aiplatform.subscription.Subscription;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/plans")
@RequiredArgsConstructor
// This class is used for controlling admin endpoints
public class AdminPlanController {
    private final PlanService planService;

    @PostMapping
    public ResponseEntity<Plan> createPlan(@RequestBody Plan plan) {
        Plan createdPlan = planService.createPlan(plan);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdPlan);
    }

    @PutMapping("/{planId}")
    public ResponseEntity<Plan> updatePlan(@PathVariable String planId, @RequestBody Plan plan) {
        Plan updatedPlan = planService.updatePlan(planId, plan);
        return ResponseEntity.ok(updatedPlan);
    }

    @GetMapping("/{planId}")
    public ResponseEntity<Plan> getPlanById(@PathVariable String planId) {
        Plan plan = planService.getPlanById(planId);
        return ResponseEntity.ok(plan);
    }

    @DeleteMapping("/{planId}")
    public ResponseEntity<Void> deactivatePlan(@PathVariable String planId) {
        planService.deactivatePlan(planId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{planId}/subscriptions")
    public ResponseEntity<Page<Subscription>> getSubscriptionsByPlan(@PathVariable String planId, Pageable pageable) {
        Page<Subscription> subscriptions = planService.getSubscriptionsByPlan(planId, pageable);
        return ResponseEntity.ok(subscriptions);
    }
}