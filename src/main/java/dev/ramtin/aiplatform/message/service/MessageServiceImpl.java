package dev.ramtin.aiplatform.message.service;

import dev.ramtin.aiplatform.message.Message;
import dev.ramtin.aiplatform.message.MessageMapper;
import dev.ramtin.aiplatform.message.MessageRepository;
import dev.ramtin.aiplatform.message.MessageRole;
import dev.ramtin.aiplatform.message.dto.MessageDTO;
import dev.ramtin.aiplatform.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
    private final MessageRepository messageRepository;
    private final MessageMapper messageMapper;

    @Override
    public MessageDTO getMessageById(String messageId) {
        Message message = messageRepository.findById(messageId).orElseThrow(() ->
                        new ResourceNotFoundException("Message not found with id: " + messageId));
        return messageMapper.toDTO(message);
    }

    @Override
    public MessageDTO createMessage(String conversationId, String content) {
        Message message = Message.builder()
                .conversationId(conversationId)
                .role(MessageRole.USER)
                .content(content)
                .createdAt(LocalDateTime.now())
                .build();
        Message savedMessage = messageRepository.save(message);
        return messageMapper.toDTO(savedMessage);
    }
}