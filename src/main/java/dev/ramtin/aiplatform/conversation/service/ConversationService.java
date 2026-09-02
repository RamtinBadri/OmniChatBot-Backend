package dev.ramtin.aiplatform.conversation.service;

import dev.ramtin.aiplatform.conversation.ConversationStatus;
import dev.ramtin.aiplatform.conversation.dto.ConversationDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ConversationService {
    ConversationDTO createConversation(String userId, String title, String model);

    ConversationDTO getConversationById(String conversationId);

    ConversationDTO updateConversation(String conversationId, String title);

    void archiveConversation(String conversationId);

    Page<ConversationDTO> getConversations(String userId, String search, ConversationStatus status, Pageable pageable);
}