package dev.ramtin.aiplatform.repository;

import dev.ramtin.aiplatform.model.entity.Message;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MessageRepository
        extends MongoRepository<Message, String> {
    List<Message> findByConversation(String conversationId);
}