package dev.ramtin.aiplatform.ai.request.repository;

import dev.ramtin.aiplatform.ai.request.AIRequest;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AIRequestRepository extends MongoRepository<AIRequest, String> {
}