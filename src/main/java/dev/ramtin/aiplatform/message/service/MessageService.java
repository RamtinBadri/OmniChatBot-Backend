package dev.ramtin.aiplatform.message.service;

import dev.ramtin.aiplatform.message.dto.MessageDTO;
import dev.ramtin.aiplatform.message.dto.MessageExchangeDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MessageService {
    MessageDTO getMessageById(String conversationId, String messageId, String userId);
    MessageExchangeDTO createMessage(String conversationId, String content, String userId);
    Page<MessageDTO> getMessages(String conversationId, String userId, Pageable pageable);
}