package dev.ramtin.aiplatform.conversation.repository;

import dev.ramtin.aiplatform.conversation.Conversation;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ConversationRepository extends MongoRepository<Conversation, String>,
        ConversationRepositoryCustom {}