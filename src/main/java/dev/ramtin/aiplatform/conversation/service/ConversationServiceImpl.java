package dev.ramtin.aiplatform.conversation.service;

import dev.ramtin.aiplatform.conversation.Conversation;
import dev.ramtin.aiplatform.conversation.ConversationRepository;
import dev.ramtin.aiplatform.conversation.ConversationMapper;
import dev.ramtin.aiplatform.conversation.ConversationStatus;
import dev.ramtin.aiplatform.conversation.dto.ConversationDTO;
import dev.ramtin.aiplatform.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {
    private final ConversationRepository conversationRepository;

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
        return ConversationMapper.toDTO(savedConversation);
    }

    @Override
    public ConversationDTO getConversationById(String conversationId, String userId) {
        Conversation conversation = getConversationForUser(conversationId, userId);
        return ConversationMapper.toDTO(conversation);
    }

    @Override
    public ConversationDTO updateConversation(String conversationId, String userId, String title) {
        Conversation conversation = getConversationForUser(conversationId, userId);
        conversation.setTitle(title);
        conversation.setUpdatedAt(LocalDateTime.now());

        Conversation updatedConversation = conversationRepository.save(conversation);
        return ConversationMapper.toDTO(updatedConversation);
    }

    @Override
    public void archiveConversation(String conversationId, String userId) {
        Conversation conversation = getConversationForUser(conversationId, userId);

        conversation.setStatus(ConversationStatus.ARCHIVED);
        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);
    }

    @Override
    public Page<ConversationDTO> getConversations(String userId, String search,
                                                  ConversationStatus status, Pageable pageable) {
        return conversationRepository.search(userId, search, status, pageable)
                .map(ConversationMapper::toDTO);
    }

    private Conversation getConversationForUser(String conversationId, String userId) {
        Conversation conversation = conversationRepository.findById(conversationId).orElseThrow(() ->
                        new ResourceNotFoundException("Conversation not found with id: " + conversationId));
        if (!conversation.getUserId().equals(userId)) {
            throw new AccessDeniedException("You do not have access to this conversation");
        }
        return conversation;
    }
}