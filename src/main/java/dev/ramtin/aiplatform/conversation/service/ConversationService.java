package dev.ramtin.aiplatform.conversation.service;

import dev.ramtin.aiplatform.conversation.dto.ConversationDTO;

public interface ConversationService {
    ConversationDTO createConversation(String userId, String title, String model);

    ConversationDTO getConversationById(String conversationId);

    ConversationDTO updateConversation(String conversationId, String title);

    void archiveConversation(String conversationId);
}