package dev.ramtin.aiplatform.message.service;

import dev.ramtin.aiplatform.message.dto.MessageDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MessageService {
    MessageDTO getMessageById(String messageId);
    MessageDTO createMessage(String conversationId, String content);
    Page<MessageDTO> getMessages(String conversationId, Pageable pageable);
}