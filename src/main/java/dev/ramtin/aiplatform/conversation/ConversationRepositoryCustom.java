package dev.ramtin.aiplatform.conversation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ConversationRepositoryCustom {
    Page<Conversation> search(String userId, String search, ConversationStatus status, Pageable pageable);
}