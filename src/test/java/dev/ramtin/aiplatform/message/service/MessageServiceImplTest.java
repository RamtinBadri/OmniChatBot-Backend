package dev.ramtin.aiplatform.message.service;

import dev.ramtin.aiplatform.conversation.Conversation;
import dev.ramtin.aiplatform.conversation.repository.ConversationRepository;
import dev.ramtin.aiplatform.exception.ResourceNotFoundException;
import dev.ramtin.aiplatform.message.Message;
import dev.ramtin.aiplatform.message.repository.MessageRepository;
import dev.ramtin.aiplatform.message.MessageRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import dev.ramtin.aiplatform.ai.AIService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageServiceImplTest {
    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private AIService aiService;

    @InjectMocks
    private MessageServiceImpl messageService;

    @Test
    void getMessageById_whenMessageExistsAndBelongsToConversation_returnsMessageDTO() {
        Conversation conversation = Conversation.builder().id("conversation123").userId("user123").build();

        Message message = Message.builder()
                .id("message123")
                .conversationId("conversation123")
                .role(MessageRole.USER)
                .content("Hello")
                .build();

        when(conversationRepository.findById("conversation123")).thenReturn(Optional.of(conversation));

        when(messageRepository.findById("message123")).thenReturn(Optional.of(message));

        var result = messageService.getMessageById(
                "conversation123",
                "message123",
                "user123"
        );

        assertEquals("message123", result.getId());
        assertEquals("conversation123", result.getConversationId());
        assertEquals(MessageRole.USER, result.getRole());
        assertEquals("Hello", result.getContent());

        verify(conversationRepository).findById("conversation123");
        verify(messageRepository).findById("message123");
    }

    @Test
    void getMessageById_whenMessageDoesNotExist_throwsResourceNotFoundException() {
        Conversation conversation = Conversation.builder().id("conversation123").userId("user123").build();

        when(conversationRepository.findById("conversation123")).thenReturn(Optional.of(conversation));

        when(messageRepository.findById("message123")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> messageService.getMessageById(
                        "conversation123",
                        "message123",
                        "user123"
                )
        );

        verify(conversationRepository).findById("conversation123");
        verify(messageRepository).findById("message123");
    }

    @Test
    void getMessageById_whenMessageBelongsToDifferentConversation_throwsResourceNotFoundException() {
        Conversation conversation = Conversation.builder()
                .id("conversation123")
                .userId("user123")
                .build();

        Message message = Message.builder()
                .id("message123")
                .conversationId("conversation456")
                .role(MessageRole.USER)
                .content("Hello")
                .build();

        when(conversationRepository.findById("conversation123")).thenReturn(Optional.of(conversation));

        when(messageRepository.findById("message123")).thenReturn(Optional.of(message));

        assertThrows(
                ResourceNotFoundException.class,
                () -> messageService.getMessageById("conversation123", "message123", "user123")
        );

        verify(conversationRepository).findById("conversation123");
        verify(messageRepository).findById("message123");
    }

    @Test
    void createMessage_whenUserOwnsConversation_createsAndReturnsMessageExchangeDTO() {
        Conversation conversation = Conversation.builder()
                .id("conversation123")
                .userId("user123")
                .model("selected-model")
                .build();

        when(conversationRepository.findById("conversation123"))
                .thenReturn(Optional.of(conversation));

        when(messageRepository.save(org.mockito.ArgumentMatchers.any(Message.class)))
                .thenAnswer(invocation -> {
                    Message message = invocation.getArgument(0);

                    if (message.getRole() == MessageRole.USER) {
                        message.setId("userMessage123");
                    } else {
                        message.setId("assistantMessage123");
                    }

                    return message;
                });

        when(aiService.generateResponse("user123", "conversation123", "selected-model"))
                .thenReturn("Hi! How can I help you?");

        var result = messageService.createMessage(
                "conversation123",
                "Hello",
                "user123"
        );

        assertNotNull(result);

        assertNotNull(result.getUserMessage());
        assertEquals("userMessage123", result.getUserMessage().getId());
        assertEquals("conversation123", result.getUserMessage().getConversationId());
        assertEquals(MessageRole.USER, result.getUserMessage().getRole());
        assertEquals("Hello", result.getUserMessage().getContent());
        assertNotNull(result.getUserMessage().getCreatedAt());

        assertNotNull(result.getAssistantMessage());
        assertEquals("assistantMessage123", result.getAssistantMessage().getId());
        assertEquals("conversation123", result.getAssistantMessage().getConversationId());
        assertEquals(MessageRole.ASSISTANT, result.getAssistantMessage().getRole());
        assertEquals("Hi! How can I help you?", result.getAssistantMessage().getContent());
        assertNotNull(result.getAssistantMessage().getCreatedAt());

        verify(conversationRepository).findById("conversation123");

        verify(aiService).generateResponse(
                "user123",
                "conversation123",
                "selected-model"
        );

        verify(messageRepository, times(2)).save(
                org.mockito.ArgumentMatchers.any(Message.class)
        );
    }

    @Test
    void createMessage_whenUserDoesNotOwnConversation_throwsAccessDeniedException() {
        Conversation conversation = Conversation.builder().id("conversation123").userId("user456").build();

        when(conversationRepository.findById("conversation123")).thenReturn(Optional.of(conversation));

        assertThrows(AccessDeniedException.class,
                () -> messageService.createMessage(
                        "conversation123",
                        "Hello",
                        "user123"
                )
        );

        verify(conversationRepository).findById("conversation123");
    }

    @Test
    void getMessages_whenUserOwnsConversation_returnsPaginatedMessages() {
        Conversation conversation = Conversation.builder().id("conversation123").userId("user123").build();

        Message message1 = Message.builder()
                .id("message123")
                .conversationId("conversation123")
                .role(MessageRole.USER)
                .content("Hello")
                .build();

        Message message2 = Message.builder()
                .id("message124")
                .conversationId("conversation123")
                .role(MessageRole.ASSISTANT)
                .content("Hi!")
                .build();

        PageRequest pageable = PageRequest.of(0, 2);

        when(conversationRepository.findById("conversation123")).thenReturn(Optional.of(conversation));

        when(messageRepository.findByConversationId("conversation123", pageable))
                .thenReturn(new PageImpl<>(
                        List.of(message1, message2),
                        pageable,
                        2
                ));

        var result = messageService.getMessages(
                "conversation123",
                "user123",
                pageable
        );

        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());

        assertEquals("message123", result.getContent().get(0).getId());
        assertEquals("Hello", result.getContent().get(0).getContent());

        assertEquals("message124", result.getContent().get(1).getId());
        assertEquals("Hi!", result.getContent().get(1).getContent());

        assertEquals(0, result.getNumber());
        assertEquals(2, result.getSize());

        verify(conversationRepository).findById("conversation123");
        verify(messageRepository)
                .findByConversationId("conversation123", pageable);
    }

    @Test
    void getMessages_whenUserDoesNotOwnConversation_throwsAccessDeniedException() {
        Conversation conversation = Conversation.builder().id("conversation123").userId("user456").build();

        PageRequest pageable = PageRequest.of(0, 10);

        when(conversationRepository.findById("conversation123")).thenReturn(Optional.of(conversation));

        assertThrows(AccessDeniedException.class,
                () -> messageService.getMessages(
                        "conversation123",
                        "user123",
                        pageable
                )
        );

        verify(conversationRepository).findById("conversation123");

        verify(messageRepository, org.mockito.Mockito.never())
                .findByConversationId(
                        org.mockito.ArgumentMatchers.anyString(),
                        org.mockito.ArgumentMatchers.any(Pageable.class)
                );
    }

    @Test
    void getMessages_whenConversationDoesNotExist_throwsResourceNotFoundException() {
        PageRequest pageable = PageRequest.of(0, 10);

        when(conversationRepository.findById("conversation123")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> messageService.getMessages(
                        "conversation123",
                        "user123",
                        pageable
                )
        );

        verify(conversationRepository).findById("conversation123");

        verify(messageRepository, org.mockito.Mockito.never())
                .findByConversationId(org.mockito.ArgumentMatchers.anyString(),
                        org.mockito.ArgumentMatchers.any(Pageable.class));
    }
}