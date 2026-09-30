package dev.ramtin.aiplatform.plan.repository;

import dev.ramtin.aiplatform.plan.Plan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PlanRepository extends MongoRepository<Plan, String> {
    Page<Plan> findByActiveTrue(Pageable pageable);
}