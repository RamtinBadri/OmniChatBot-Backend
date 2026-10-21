package dev.ramtin.aiplatform.message;

import dev.ramtin.aiplatform.exception.GlobalExceptionHandler;
import dev.ramtin.aiplatform.message.dto.MessageDTO;
import dev.ramtin.aiplatform.message.dto.MessageExchangeDTO;
import dev.ramtin.aiplatform.message.service.MessageService;
import dev.ramtin.aiplatform.security.CustomUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MessageControllerContractTest {
    private MessageService messageService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        messageService = mock(MessageService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new MessageController(messageService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
        CustomUserDetails user = new CustomUserDetails("user1", "user", "password", List.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createMessage_acceptsLegacyModelButPassesOnlyContentAndIdentity() throws Exception {
        MessageDTO userMessage = MessageDTO.builder().id("message1").role(MessageRole.USER)
                .content("hello").build();
        MessageDTO assistantMessage = MessageDTO.builder().id("message2").role(MessageRole.ASSISTANT)
                .content("reply").build();
        when(messageService.createMessage("conversation1", "hello", "user1"))
                .thenReturn(new MessageExchangeDTO(userMessage, assistantMessage));

        mockMvc.perform(post("/api/conversations/conversation1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"hello\",\"model\":\"ignored-model\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userMessage.id").value("message1"))
                .andExpect(jsonPath("$.assistantMessage.content").value("reply"));

        verify(messageService).createMessage("conversation1", "hello", "user1");
    }

    @Test
    void getMessages_keepsFlatPageMetadataAndZeroBasedPageIndex() throws Exception {
        PageRequest pageable = PageRequest.of(1, 2);
        when(messageService.getMessages("conversation1", "user1", pageable))
                .thenReturn(new PageImpl<>(List.of(MessageDTO.builder().id("message3").build()), pageable, 3));

        mockMvc.perform(get("/api/conversations/conversation1/messages?page=1&size=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("message3"))
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.page").doesNotExist());
    }
}
