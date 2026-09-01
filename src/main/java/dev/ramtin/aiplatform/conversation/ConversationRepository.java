package dev.ramtin.aiplatform.conversation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ConversationRepository extends MongoRepository<Conversation, String>,
        ConversationRepositoryCustom {
    Page<Conversation> findByUserId(String userId, Pageable pageable);
}