package dev.ramtin.aiplatform.conversation;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ConversationRepository
        extends MongoRepository<Conversation, String> {
    List<Conversation> findByUserId(String userId);
}