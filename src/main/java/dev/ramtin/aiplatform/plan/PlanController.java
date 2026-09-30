package dev.ramtin.aiplatform.plan;

import dev.ramtin.aiplatform.plan.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
// This class is used for controlling public endpoints
public class PlanController {
    private final PlanService planService;

    @GetMapping
    public ResponseEntity<Page<Plan>> getActivePlans(Pageable pageable) {
        Page<Plan> plans = planService.getActivePlans(pageable);
        return ResponseEntity.ok(plans);
    }
}