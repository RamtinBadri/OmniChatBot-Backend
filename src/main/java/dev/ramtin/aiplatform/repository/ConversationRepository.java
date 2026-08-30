package dev.ramtin.aiplatform.repository;

import dev.ramtin.aiplatform.model.entity.Conversation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ConversationRepository
        extends MongoRepository<Conversation, String> {
    List<Conversation> findByUserId(String userId);
}