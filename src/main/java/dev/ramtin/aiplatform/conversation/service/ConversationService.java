package dev.ramtin.aiplatform.conversation.service;

import dev.ramtin.aiplatform.conversation.ConversationStatus;
import dev.ramtin.aiplatform.conversation.dto.ConversationDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ConversationService {
    ConversationDTO createConversation(String userId, String title, String model);

    ConversationDTO getConversationById(String conversationId, String userId);

    ConversationDTO updateConversation(String conversationId, String userId, String title, String model);

    void archiveConversation(String conversationId, String userId);

    Page<ConversationDTO> getConversations(String userId, String search, ConversationStatus status, Pageable pageable);

    void deleteConversation(String conversationId, String userId);
}