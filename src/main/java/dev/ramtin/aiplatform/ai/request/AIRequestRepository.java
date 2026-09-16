package dev.ramtin.aiplatform.ai.request;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface AIRequestRepository extends MongoRepository<AIRequest, String> {
}