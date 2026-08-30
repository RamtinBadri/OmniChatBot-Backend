package dev.ramtin.aiplatform.message;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MessageRepository
        extends MongoRepository<Message, String> {
    List<Message> findByConversation(String conversationId);
}