package dev.ramtin.aiplatform.message.service;

import dev.ramtin.aiplatform.ai.AIService;
import dev.ramtin.aiplatform.conversation.Conversation;
import dev.ramtin.aiplatform.conversation.ConversationRepository;
import dev.ramtin.aiplatform.message.Message;
import dev.ramtin.aiplatform.message.MessageMapper;
import dev.ramtin.aiplatform.message.MessageRepository;
import dev.ramtin.aiplatform.message.MessageRole;
import dev.ramtin.aiplatform.message.dto.MessageDTO;
import dev.ramtin.aiplatform.exception.ResourceNotFoundException;
import dev.ramtin.aiplatform.message.dto.MessageExchangeDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
    private final AIService aiService;
    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;

    @Override
    public MessageDTO getMessageById(String conversationId, String messageId, String userId) {
        getConversationForUser(conversationId, userId);

        Message message = messageRepository.findById(messageId).orElseThrow(() ->
                        new ResourceNotFoundException("Message not found with id: " + messageId));

        if (!message.getConversationId().equals(conversationId)) {
            throw new ResourceNotFoundException("Message not found in conversation: " + conversationId);
        }

        return MessageMapper.toDTO(message);
    }

    @Override
    public MessageExchangeDTO createMessage(String conversationId, String content, String userId) {
        getConversationForUser(conversationId, userId);

        Message message = Message.builder()
                .conversationId(conversationId)
                .role(MessageRole.USER)
                .content(content)
                .createdAt(LocalDateTime.now())
                .build();

        Message savedUserMessage = messageRepository.save(message);


        String assistantContent = aiService.generateResponse(userId, conversationId);

        Message assistantMessage = Message.builder()
                .conversationId(conversationId)
                .role(MessageRole.ASSISTANT)
                .content(assistantContent)
                .createdAt(LocalDateTime.now())
                .build();

        Message savedAssistantMessage = messageRepository.save(assistantMessage);

        return new MessageExchangeDTO(MessageMapper.toDTO(savedUserMessage),
                MessageMapper.toDTO(savedAssistantMessage));
    }

    @Override
    public Page<MessageDTO> getMessages(String conversationId, String userId, Pageable pageable) {
        getConversationForUser(conversationId, userId);

        return messageRepository.findByConversationId(conversationId, pageable)
                .map(MessageMapper::toDTO);
    }

    private void getConversationForUser(String conversationId, String userId) {
        Conversation conversation = conversationRepository.findById(conversationId).orElseThrow(() ->
                        new ResourceNotFoundException("Conversation not found with id: " + conversationId));

        if (!conversation.getUserId().equals(userId)) {
            throw new AccessDeniedException("You do not have access to this conversation");
        }
    }
}