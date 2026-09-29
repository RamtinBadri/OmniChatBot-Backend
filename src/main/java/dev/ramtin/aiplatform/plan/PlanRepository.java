package dev.ramtin.aiplatform.plan;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PlanRepository extends MongoRepository<Plan, String> {
    Page<Plan> findByActiveTrue(Pageable pageable);
}