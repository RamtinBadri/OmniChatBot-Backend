package dev.ramtin.aiplatform.ai;

import dev.ramtin.aiplatform.ai.request.AIRequest;
import dev.ramtin.aiplatform.ai.request.AIRequestRepository;
import dev.ramtin.aiplatform.ai.request.AIRequestStatus;
import dev.ramtin.aiplatform.config.AIProperties;
import dev.ramtin.aiplatform.exception.AIProviderException;
import dev.ramtin.aiplatform.exception.ModelPricingNotFoundException;
import dev.ramtin.aiplatform.message.Message;
import dev.ramtin.aiplatform.message.MessageRepository;
import dev.ramtin.aiplatform.message.MessageRole;
import dev.ramtin.aiplatform.model_pricing.CostCalculator;
import dev.ramtin.aiplatform.model_pricing.ModelPricing;
import dev.ramtin.aiplatform.model_pricing.ModelPricingRepository;
import dev.ramtin.aiplatform.omniroute.response.OmniRouteChoice;
import dev.ramtin.aiplatform.omniroute.response.OmniRouteResponse;
import dev.ramtin.aiplatform.omniroute.response.OmniRouteResponseMessage;
import dev.ramtin.aiplatform.omniroute.response.OmniRouteUsage;
import dev.ramtin.aiplatform.omniroute.service.OmniRouteClient;
import dev.ramtin.aiplatform.usage_record.UsageRecord;
import dev.ramtin.aiplatform.usage_record.UsageRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AIServiceImplTest {
    @Mock
    private OmniRouteClient omniRouteClient;

    @Mock
    private AIProperties aiProperties;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private AIRequestRepository aiRequestRepository;

    @Mock
    private UsageRecordRepository usageRecordRepository;

    @Mock
    private ModelPricingRepository modelPricingRepository;

    @Spy
    private CostCalculator costCalculator;

    @InjectMocks
    private AIServiceImpl aiService;

    @Test
    void generateResponse_whenProviderSucceeds_updatesAIRequestSuccessfully() {
        when(aiProperties.getDefaultModel()).thenReturn("auto/best-coding");

        ModelPricing pricing = ModelPricing.builder()
                .model("glm-5")
                .inputPricePerMillion(new BigDecimal("0.50"))
                .outputPricePerMillion(new BigDecimal("2.00"))
                .active(true)
                .build();

        when(modelPricingRepository.findByModelAndActiveTrue("glm-5"))
                .thenReturn(java.util.Optional.of(pricing));

        Message userMessage = Message.builder()
                .id("message123")
                .conversationId("conversation123")
                .role(MessageRole.USER)
                .content("Hello")
                .build();

        when(messageRepository.findByConversationIdOrderByCreatedAtAsc(
                "conversation123"
        )).thenReturn(List.of(userMessage));

        OmniRouteResponseMessage responseMessage =
                new OmniRouteResponseMessage(
                        "assistant",
                        "Hello! How can I help you?"
                );

        OmniRouteChoice choice = new OmniRouteChoice(0, "stop", responseMessage);

        OmniRouteUsage usage = new OmniRouteUsage(
                        100,
                        20,
                        120);

        OmniRouteResponse response =
                new OmniRouteResponse(
                        "response123",
                        "glm-5",
                        List.of(choice),
                        usage
                );

        when(omniRouteClient.sendMessage(
                eq("auto/best-coding"),
                any(List.class)
        )).thenReturn(response);

        when(aiRequestRepository.save(any(AIRequest.class)))
                .thenAnswer(invocation -> {
                    AIRequest request = invocation.getArgument(0);

                    if (request.getId() == null) {
                        request.setId("ai-request-123");
                    }

                    return request;
                });

        when(usageRecordRepository.save(any(UsageRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        String result = aiService.generateResponse("user123", "conversation123");

        assertEquals("Hello! How can I help you?", result);

        ArgumentCaptor<AIRequest> captor = ArgumentCaptor.forClass(AIRequest.class);

        verify(aiRequestRepository, times(3)).save(captor.capture());

        List<AIRequest> savedRequests = captor.getAllValues();

        AIRequest finalRequest = savedRequests.get(savedRequests.size() - 1);

        assertEquals("user123", finalRequest.getUserId());

        assertEquals("conversation123", finalRequest.getConversationId());

        assertEquals("auto/best-coding", finalRequest.getRequestedModel());

        assertEquals("glm-5", finalRequest.getActualModel());

        assertEquals(AIRequestStatus.SUCCESS, finalRequest.getStatus());

        assertNotNull(finalRequest.getRequestTime());
        assertNotNull(finalRequest.getResponseTime());

        verify(omniRouteClient).sendMessage(eq("auto/best-coding"), any(List.class));

        ArgumentCaptor<UsageRecord> usageCaptor = ArgumentCaptor.forClass(UsageRecord.class);

        verify(usageRecordRepository).save(usageCaptor.capture());

        UsageRecord savedUsage = usageCaptor.getValue();

        assertEquals("user123", savedUsage.getUserId());
        assertEquals("conversation123", savedUsage.getConversationId());
        assertEquals("glm-5", savedUsage.getModel());

        assertEquals(100, savedUsage.getInputTokens());
        assertEquals(20, savedUsage.getOutputTokens());
        assertEquals(120, savedUsage.getTotalTokens());

        assertNotNull(savedUsage.getAiRequestId());
        assertNotNull(savedUsage.getTimestamp());

        assertEquals(0, new BigDecimal("0.00009").compareTo(savedUsage.getProviderCost()));

        assertEquals(0, new BigDecimal("0.00009").compareTo(savedUsage.getChargedAmount()));
    }

    @Test
    void generateResponse_whenProviderFails_marksAIRequestAsProviderError() {
        when(aiProperties.getDefaultModel())
                .thenReturn("auto/best-coding");

        when(messageRepository.findByConversationIdOrderByCreatedAtAsc(
                "conversation123"
        )).thenReturn(List.of());

        when(aiRequestRepository.save(any(AIRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(omniRouteClient.sendMessage(
                eq("auto/best-coding"),
                any(List.class)
        )).thenThrow(
                new AIProviderException("AI provider request failed")
        );

        assertThrows(
                AIProviderException.class,
                () -> aiService.generateResponse(
                        "user123",
                        "conversation123"
                )
        );

        ArgumentCaptor<AIRequest> captor =
                ArgumentCaptor.forClass(AIRequest.class);

        verify(aiRequestRepository, times(3))
                .save(captor.capture());

        List<AIRequest> savedRequests =
                captor.getAllValues();

        AIRequest finalRequest =
                savedRequests.get(savedRequests.size() - 1);

        assertEquals(
                "user123",
                finalRequest.getUserId()
        );

        assertEquals(
                "conversation123",
                finalRequest.getConversationId()
        );

        assertEquals(
                "auto/best-coding",
                finalRequest.getRequestedModel()
        );

        assertEquals(
                AIRequestStatus.PROVIDER_ERROR,
                finalRequest.getStatus()
        );

        assertNotNull(finalRequest.getRequestTime());
        assertNotNull(finalRequest.getResponseTime());

        verify(usageRecordRepository, never()).save(any(UsageRecord.class));
    }

    @Test
    void generateResponse_shouldFailWhenModelPricingDoesNotExist() {
        when(aiProperties.getDefaultModel()).thenReturn("auto/best-coding");

        OmniRouteUsage usage = new OmniRouteUsage(100, 20, 120);

        OmniRouteResponse response = new OmniRouteResponse(
                "response-123",
                "glm-5",
                List.of(
                        new OmniRouteChoice(
                                0,
                                "stop",
                                new OmniRouteResponseMessage(
                                        "assistant",
                                        "Hello"
                                )
                        )
                ),
                usage
        );

        when(omniRouteClient.sendMessage(
                eq("auto/best-coding"),
                anyList()
        )).thenReturn(response);

        when(modelPricingRepository.findByModelAndActiveTrue("glm-5"))
                .thenReturn(Optional.empty());

        when(aiRequestRepository.save(any(AIRequest.class)))
                .thenAnswer(invocation -> {
                    AIRequest request = invocation.getArgument(0);

                    if (request.getId() == null) {
                        request.setId("ai-request-123");
                    }

                    return request;
                });

        assertThrows(
                ModelPricingNotFoundException.class,
                () -> aiService.generateResponse(
                        "user-123",
                        "conversation-123"
                )
        );

        verify(usageRecordRepository, never())
                .save(any(UsageRecord.class));

        verify(aiRequestRepository, never())
                .save(argThat(request ->
                        request.getStatus() == AIRequestStatus.SUCCESS
                ));
    }

    private static OmniRouteResponse getOmniRouteResponse() {
        OmniRouteUsage usage = new OmniRouteUsage(100, 20, 120);

        OmniRouteResponse response = new OmniRouteResponse(
                "response-123",
                "glm-5",
                List.of(
                        new OmniRouteChoice(
                                0,
                                "stop",
                                new OmniRouteResponseMessage(
                                        "assistant",
                                        "Hello"
                                )
                        )
                ),
                usage
        );
        return response;
    }
}