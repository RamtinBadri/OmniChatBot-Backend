package dev.ramtin.aiplatform.conversation.service;

import dev.ramtin.aiplatform.conversation.Conversation;
import dev.ramtin.aiplatform.conversation.ConversationRepository;
import dev.ramtin.aiplatform.conversation.ConversationMapper;
import dev.ramtin.aiplatform.conversation.ConversationStatus;
import dev.ramtin.aiplatform.conversation.dto.ConversationDTO;
import dev.ramtin.aiplatform.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {
    private final ConversationRepository conversationRepository;
    private final ConversationMapper conversationMapper;

    @Override
    public ConversationDTO createConversation(String userId, String title, String model) {
        LocalDateTime now = LocalDateTime.now();

        Conversation conversation = Conversation.builder()
                .userId(userId)
                .title(title)
                .model(model)
                .status(ConversationStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .build();

        Conversation savedConversation = conversationRepository.save(conversation);
        return conversationMapper.toDTO(savedConversation);
    }

    @Override
    public ConversationDTO getConversationById(String conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId).orElseThrow(() ->
                new ResourceNotFoundException("Conversation not found with id: " + conversationId));
        return conversationMapper.toDTO(conversation);
    }

    @Override
    public ConversationDTO updateConversation(String conversationId, String title) {
        Conversation conversation = conversationRepository.findById(conversationId).orElseThrow(() ->
                new ResourceNotFoundException("Conversation not found with id: " + conversationId));

        conversation.setTitle(title);
        conversation.setUpdatedAt(LocalDateTime.now());

        Conversation updatedConversation = conversationRepository.save(conversation);

        return conversationMapper.toDTO(updatedConversation);
    }

    @Override
    public void archiveConversation(String conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId).orElseThrow(() ->
                new ResourceNotFoundException("Conversation not found with id: " + conversationId));

        conversation.setStatus(ConversationStatus.ARCHIVED);
        conversation.setUpdatedAt(LocalDateTime.now());

        conversationRepository.save(conversation);
    }
}