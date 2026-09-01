package dev.ramtin.aiplatform.conversation;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ConversationRepository extends MongoRepository<Conversation, String>,
        ConversationRepositoryCustom {}