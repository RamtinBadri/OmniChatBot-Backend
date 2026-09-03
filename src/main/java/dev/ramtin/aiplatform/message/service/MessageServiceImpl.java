package dev.ramtin.aiplatform.message.service;

import dev.ramtin.aiplatform.conversation.ConversationRepository;
import dev.ramtin.aiplatform.message.Message;
import dev.ramtin.aiplatform.message.MessageMapper;
import dev.ramtin.aiplatform.message.MessageRepository;
import dev.ramtin.aiplatform.message.MessageRole;
import dev.ramtin.aiplatform.message.dto.MessageDTO;
import dev.ramtin.aiplatform.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;

    @Override
    public MessageDTO getMessageById(String messageId) {
        Message message = messageRepository.findById(messageId).orElseThrow(() ->
                        new ResourceNotFoundException("Message not found with id: " + messageId));
        return MessageMapper.toDTO(message);
    }

    @Override
    public MessageDTO createMessage(String conversationId, String content) {
        conversationRepository.findById(conversationId).orElseThrow(() ->
                        new ResourceNotFoundException("Conversation not found with id: " + conversationId));

        Message message = Message.builder()
                .conversationId(conversationId)
                .role(MessageRole.USER)
                .content(content)
                .createdAt(LocalDateTime.now())
                .build();
        Message savedMessage = messageRepository.save(message);
        return MessageMapper.toDTO(savedMessage);
    }

    @Override
    public Page<MessageDTO> getMessages(String conversationId, Pageable pageable) {
        conversationRepository.findById(conversationId).orElseThrow(() ->
                        new ResourceNotFoundException("Conversation not found with id: " + conversationId));

        return messageRepository.findByConversationId(conversationId, pageable)
                .map(MessageMapper::toDTO);
    }
}