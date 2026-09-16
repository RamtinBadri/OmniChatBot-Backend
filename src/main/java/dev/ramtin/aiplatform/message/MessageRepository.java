package dev.ramtin.aiplatform.message;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MessageRepository extends MongoRepository<Message, String> {
    Page<Message> findByConversationId(String conversationId, Pageable pageable);

    List<Message> findByConversationIdOrderByCreatedAtAsc(String conversationId);
}