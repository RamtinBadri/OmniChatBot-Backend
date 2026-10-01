package dev.ramtin.aiplatform.ai.request.repository;

import dev.ramtin.aiplatform.ai.request.AIRequest;
import dev.ramtin.aiplatform.ai.request.AIRequestStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;

public interface AIRequestRepository extends MongoRepository<AIRequest, String>, AIRequestRepositoryCustom {
    long countByStatus(AIRequestStatus status);

    long countByStatusIn(Collection<AIRequestStatus> statuses);
}