package dev.ramtin.aiplatform.message.service;

import dev.ramtin.aiplatform.message.dto.MessageDTO;

public interface MessageService {
    MessageDTO getMessageById(String messageId);
    MessageDTO createMessage(String conversationId, String content);
}