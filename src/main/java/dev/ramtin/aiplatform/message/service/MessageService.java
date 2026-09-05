package dev.ramtin.aiplatform.message.service;

import dev.ramtin.aiplatform.message.dto.MessageDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MessageService {
    MessageDTO getMessageById(String conversationId, String messageId, String userId);
    MessageDTO createMessage(String conversationId, String content, String userId);
    Page<MessageDTO> getMessages(String conversationId, String userId, Pageable pageable);
}