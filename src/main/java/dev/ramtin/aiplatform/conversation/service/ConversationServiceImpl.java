package dev.ramtin.aiplatform.conversation.service;

import dev.ramtin.aiplatform.conversation.Conversation;
import dev.ramtin.aiplatform.conversation.repository.ConversationRepository;
import dev.ramtin.aiplatform.conversation.ConversationMapper;
import dev.ramtin.aiplatform.conversation.ConversationStatus;
import dev.ramtin.aiplatform.conversation.dto.ConversationDTO;
import dev.ramtin.aiplatform.exception.ResourceNotFoundException;
import dev.ramtin.aiplatform.message.repository.MessageRepository;
import dev.ramtin.aiplatform.model_pricing.service.ModelPricingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final ModelPricingService modelPricingService;

    @Override
    public ConversationDTO createConversation(String userId, String title, String model) {
        String selectedModel = model == null || model.isBlank()
                ? modelPricingService.getModelCatalog(PageRequest.of(0, 1)).getDefaultModel()
                : model;
        if (!modelPricingService.isAvailable(selectedModel)) {
            throw new IllegalArgumentException("The selected AI model is unavailable");
        }
        LocalDateTime now = LocalDateTime.now();

        Conversation conversation = Conversation.builder()
                .userId(userId)
                .title(title)
                .model(selectedModel)
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
    public ConversationDTO updateConversation(String conversationId, String userId, String title, String model) {
        Conversation conversation = getConversationForUser(conversationId, userId);
        if (title != null) {
            if (title.isBlank()) throw new IllegalArgumentException("Title must not be blank");
            conversation.setTitle(title);
        }
        if (model != null) {
            if (!modelPricingService.isAvailable(model)) {
                throw new IllegalArgumentException("The selected AI model is unavailable");
            }
            conversation.setModel(model);
        }
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

    @Override
    public void deleteConversation(String conversationId, String userId) {
        Conversation conversation = getConversationForUser(conversationId, userId);
        messageRepository.deleteAll(messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId));
        conversationRepository.delete(conversation);
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