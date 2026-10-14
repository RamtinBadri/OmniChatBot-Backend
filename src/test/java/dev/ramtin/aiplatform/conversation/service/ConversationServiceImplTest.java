package dev.ramtin.aiplatform.conversation.service;

import dev.ramtin.aiplatform.conversation.Conversation;
import dev.ramtin.aiplatform.conversation.repository.ConversationRepository;
import dev.ramtin.aiplatform.conversation.ConversationStatus;
import dev.ramtin.aiplatform.conversation.dto.ConversationDTO;
import dev.ramtin.aiplatform.exception.ResourceNotFoundException;
import dev.ramtin.aiplatform.model_pricing.service.ModelPricingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConversationServiceImplTest {
    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private dev.ramtin.aiplatform.message.repository.MessageRepository messageRepository;

    @Mock
    private ModelPricingService modelPricingService;

    @InjectMocks
    private ConversationServiceImpl conversationService;

    @Test
    void getConversationById_whenConversationExistsAndBelongsToUser_returnsConversationDTO() {
        Conversation conversation = Conversation.builder()
                .id("conversation123")
                .userId("user123")
                .title("Learning Spring Boot")
                .model("default")
                .status(ConversationStatus.ACTIVE)
                .build();

        when(conversationRepository.findById("conversation123")).thenReturn(Optional.of(conversation));

        var result = conversationService.getConversationById("conversation123", "user123");

        assertEquals("conversation123", result.getId());
        assertEquals("user123", result.getUserId());
        assertEquals("Learning Spring Boot", result.getTitle());
        assertEquals("default", result.getModel());
        assertEquals(ConversationStatus.ACTIVE, result.getStatus());

        verify(conversationRepository).findById("conversation123");
    }

    @Test
    void getConversationById_whenConversationDoesNotExist_throwsResourceNotFoundException() {
        when(conversationRepository.findById("conversation123")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> conversationService.getConversationById(
                        "conversation123",
                        "user123"));

        verify(conversationRepository).findById("conversation123");
    }

    @Test
    void getConversationById_whenConversationBelongsToAnotherUser_throwsAccessDeniedException() {
        Conversation conversation = Conversation.builder()
                .id("conversation123")
                .userId("user456")
                .title("Private Conversation")
                .model("default")
                .status(ConversationStatus.ACTIVE)
                .build();

        when(conversationRepository.findById("conversation123")).thenReturn(Optional.of(conversation));

        assertThrows(AccessDeniedException.class,
                () -> conversationService.getConversationById("conversation123", "user123"));

        verify(conversationRepository).findById("conversation123");
    }

    @Test
    void createConversation_createsAndReturnsConversationDTO() {
        when(modelPricingService.isAvailable("default")).thenReturn(true);
        when(conversationRepository.save(org.mockito.ArgumentMatchers.any(Conversation.class)))
                .thenAnswer(invocation -> {
                    Conversation conversation = invocation.getArgument(0);
                    conversation.setId("conversation123");
                    return conversation;
                });

        var result = conversationService.createConversation(
                "user123",
                "Learning Spring Boot",
                "default"
        );

        assertEquals("conversation123", result.getId());
        assertEquals("user123", result.getUserId());
        assertEquals("Learning Spring Boot", result.getTitle());
        assertEquals("default", result.getModel());
        assertEquals(ConversationStatus.ACTIVE, result.getStatus());

        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());

        verify(conversationRepository).save(
                org.mockito.ArgumentMatchers.any(Conversation.class)
        );
    }

    @Test
    void updateConversation_whenUserOwnsConversation_updatesTitle() {
        Conversation conversation = Conversation.builder()
                .id("conversation123")
                .userId("user123")
                .title("Old Title")
                .model("default")
                .status(ConversationStatus.ACTIVE)
                .build();

        when(conversationRepository.findById("conversation123"))
                .thenReturn(Optional.of(conversation));
        when(conversationRepository.save(conversation))
                .thenReturn(conversation);

        var result = conversationService.updateConversation(
                "conversation123",
                "user123",
                "New Title",
                null
        );

        assertEquals("New Title", result.getTitle());
        assertNotNull(result.getUpdatedAt());

        verify(conversationRepository).findById("conversation123");
        verify(conversationRepository).save(conversation);
    }

    @Test
    void updateConversation_whenUserDoesNotOwnConversation_throwsAccessDeniedException() {
        Conversation conversation = Conversation.builder()
                .id("conversation123")
                .userId("user456")
                .title("Private Conversation")
                .model("default")
                .status(ConversationStatus.ACTIVE)
                .build();

        when(conversationRepository.findById("conversation123")).thenReturn(Optional.of(conversation));

        assertThrows(AccessDeniedException.class,
                () -> conversationService.updateConversation("conversation123",
                        "user123", "New Title", null));

        verify(conversationRepository).findById("conversation123");
    }

    @Test
    void archiveConversation_whenUserOwnsConversation_archivesConversation() {
        Conversation conversation = Conversation.builder()
                .id("conversation123")
                .userId("user123")
                .title("Learning Spring Boot")
                .model("default")
                .status(ConversationStatus.ACTIVE)
                .build();

        when(conversationRepository.findById("conversation123"))
                .thenReturn(Optional.of(conversation));

        conversationService.archiveConversation(
                "conversation123",
                "user123"
        );

        assertEquals(
                ConversationStatus.ARCHIVED,
                conversation.getStatus()
        );

        assertNotNull(conversation.getUpdatedAt());

        verify(conversationRepository).findById("conversation123");
        verify(conversationRepository).save(conversation);
    }

    @Test
    void archiveConversation_whenUserDoesNotOwnConversation_throwsAccessDeniedException() {
        Conversation conversation = Conversation.builder()
                .id("conversation123")
                .userId("user456")
                .title("Private Conversation")
                .model("default")
                .status(ConversationStatus.ACTIVE)
                .build();

        when(conversationRepository.findById("conversation123"))
                .thenReturn(Optional.of(conversation));

        assertThrows(
                AccessDeniedException.class,
                () -> conversationService.archiveConversation(
                        "conversation123",
                        "user123"
                )
        );

        verify(conversationRepository).findById("conversation123");
    }

    @Test
    void getConversations_returnsMappedConversationDTOPage() {
        Conversation conversation = Conversation.builder()
                .id("conversation123")
                .userId("user123")
                .title("Learning Spring Boot")
                .model("default")
                .status(ConversationStatus.ACTIVE)
                .build();

        Page<Conversation> conversationPage =
                new PageImpl<>(
                        List.of(conversation),
                        PageRequest.of(0, 20),
                        1
                );

        Pageable pageable = PageRequest.of(0, 20);

        when(conversationRepository.search(
                "user123",
                "Spring",
                ConversationStatus.ACTIVE,
                pageable
        )).thenReturn(conversationPage);

        Page<ConversationDTO> result =
                conversationService.getConversations(
                        "user123",
                        "Spring",
                        ConversationStatus.ACTIVE,
                        pageable
                );

        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals(0, result.getNumber());
        assertEquals(20, result.getSize());

        ConversationDTO dto = result.getContent().get(0);

        assertEquals("conversation123", dto.getId());
        assertEquals("user123", dto.getUserId());
        assertEquals("Learning Spring Boot", dto.getTitle());
        assertEquals("default", dto.getModel());
        assertEquals(ConversationStatus.ACTIVE, dto.getStatus());

        verify(conversationRepository).search(
                "user123",
                "Spring",
                ConversationStatus.ACTIVE,
                pageable
        );
    }
}