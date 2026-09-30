package dev.ramtin.aiplatform.conversation.repository;

import dev.ramtin.aiplatform.conversation.Conversation;
import dev.ramtin.aiplatform.conversation.ConversationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ConversationRepositoryCustom {
    Page<Conversation> search(String userId, String search, ConversationStatus status, Pageable pageable);
}