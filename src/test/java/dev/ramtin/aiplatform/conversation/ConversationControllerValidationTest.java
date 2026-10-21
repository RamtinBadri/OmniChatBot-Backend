package dev.ramtin.aiplatform.conversation;

import dev.ramtin.aiplatform.conversation.dto.ConversationDTO;
import dev.ramtin.aiplatform.conversation.service.ConversationService;
import dev.ramtin.aiplatform.exception.GlobalExceptionHandler;
import dev.ramtin.aiplatform.model_pricing.dto.ModelCatalogDTO;
import dev.ramtin.aiplatform.model_pricing.service.ModelPricingService;
import dev.ramtin.aiplatform.security.CustomUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ConversationControllerValidationTest {
    @Mock
    private ConversationService conversationService;
    @Mock
    private ModelPricingService modelPricingService;

    private MockMvc mockMvc;
    private LocalValidatorFactoryBean validator;

    @BeforeEach
    void setUp() {
        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new ConversationController(conversationService, modelPricingService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
        CustomUserDetails user = new CustomUserDetails("user123", "user", "password", List.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        validator.close();
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"title\":null}", "{\"title\":\"\"}", "{\"title\":\"   \"}"})
    void create_withMissingOrBlankTitle_returnsValidationError(String body) throws Exception {
        mockMvc.perform(post("/api/conversations").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"));

        verifyNoInteractions(conversationService, modelPricingService);
    }

    @Test
    void create_withExplicitModel_delegatesForAuthenticatedUser() throws Exception {
        when(conversationService.createConversation("user123", "Title", "chosen-model"))
                .thenReturn(ConversationDTO.builder().id("conversation123").title("Title").build());

        mockMvc.perform(post("/api/conversations").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Title\",\"model\":\"chosen-model\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("conversation123"));

        verify(conversationService).createConversation("user123", "Title", "chosen-model");
        verifyNoInteractions(modelPricingService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"{\"title\":\"Title\"}", "{\"title\":\"Title\",\"model\":null}",
            "{\"title\":\"Title\",\"model\":\"\"}", "{\"title\":\"Title\",\"model\":\"   \"}"})
    void create_withMissingOrBlankModel_preservesDefaultModelFallback(String body) throws Exception {
        when(modelPricingService.getModelCatalog(PageRequest.of(0, 1)))
                .thenReturn(new ModelCatalogDTO("default-model", Page.empty()));

        mockMvc.perform(post("/api/conversations").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        verify(conversationService).createConversation("user123", "Title", "default-model");
    }

    @Test
    void create_withLongTitle_doesNotAddAnUpdateOnlyConstraint() throws Exception {
        String title = "x".repeat(201);

        mockMvc.perform(post("/api/conversations").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"" + title + "\",\"model\":\"chosen-model\"}"))
                .andExpect(status().isCreated());

        verify(conversationService).createConversation("user123", title, "chosen-model");
    }

    @Test
    void update_withTitleOver200Characters_returnsValidationError() throws Exception {
        mockMvc.perform(put("/api/conversations/conversation123").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"" + "x".repeat(201) + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"));

        verifyNoInteractions(conversationService, modelPricingService);
    }

    @Test
    void update_withTitleAt200CharacterLimit_delegatesForAuthenticatedUser() throws Exception {
        String title = "x".repeat(200);

        mockMvc.perform(put("/api/conversations/conversation123").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"" + title + "\",\"model\":\"chosen-model\"}"))
                .andExpect(status().isOk());

        verify(conversationService).updateConversation("conversation123", "user123", title, "chosen-model");
        verifyNoInteractions(modelPricingService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"title\":null}"})
    void update_withOptionalTitleOmittedOrNull_remainsValid(String body) throws Exception {
        mockMvc.perform(put("/api/conversations/conversation123").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());

        verify(conversationService).updateConversation("conversation123", "user123", null, null);
        verifyNoInteractions(modelPricingService);
    }

    @Test
    void update_withModelOnly_remainsValid() throws Exception {
        mockMvc.perform(put("/api/conversations/conversation123").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"model\":\"chosen-model\"}"))
                .andExpect(status().isOk());

        verify(conversationService).updateConversation("conversation123", "user123", null, "chosen-model");
        verifyNoInteractions(modelPricingService);
    }

    @Test
    void update_withBlankTitle_remainsValidUnderExistingSizeConstraint() throws Exception {
        mockMvc.perform(put("/api/conversations/conversation123").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"   \"}"))
                .andExpect(status().isOk());

        verify(conversationService).updateConversation("conversation123", "user123", "   ", null);
    }
}
