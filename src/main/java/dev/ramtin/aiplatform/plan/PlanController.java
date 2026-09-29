package dev.ramtin.aiplatform.plan;

import dev.ramtin.aiplatform.plan.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
public class PlanController {
    private final PlanService planService;

    @GetMapping
    public ResponseEntity<Page<Plan>> getActivePlans(Pageable pageable) {
        Page<Plan> plans = planService.getActivePlans(pageable);
        return ResponseEntity.ok(plans);
    }
}