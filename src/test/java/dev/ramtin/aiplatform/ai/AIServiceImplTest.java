package dev.ramtin.aiplatform.ai;

import dev.ramtin.aiplatform.ai.request.AIRequest;
import dev.ramtin.aiplatform.ai.request.AIRequestStatus;
import dev.ramtin.aiplatform.ai.request.repository.AIRequestRepository;
import dev.ramtin.aiplatform.config.AIProperties;
import dev.ramtin.aiplatform.credit.CreditSourceHandler;
import dev.ramtin.aiplatform.exception.AIProviderException;
import dev.ramtin.aiplatform.exception.ModelPricingNotFoundException;
import dev.ramtin.aiplatform.message.Message;
import dev.ramtin.aiplatform.message.MessageRole;
import dev.ramtin.aiplatform.message.repository.MessageRepository;
import dev.ramtin.aiplatform.model_pricing.CostCalculator;
import dev.ramtin.aiplatform.model_pricing.ModelPricing;
import dev.ramtin.aiplatform.model_pricing.service.ModelPricingService;
import dev.ramtin.aiplatform.omniroute.response.OmniRouteChoice;
import dev.ramtin.aiplatform.omniroute.response.OmniRouteResponse;
import dev.ramtin.aiplatform.omniroute.response.OmniRouteResponseMessage;
import dev.ramtin.aiplatform.omniroute.response.OmniRouteUsage;
import dev.ramtin.aiplatform.omniroute.service.OmniRouteClient;
import dev.ramtin.aiplatform.usage_record.UsageRecord;
import dev.ramtin.aiplatform.usage_record.repository.UsageRecordRepository;
import dev.ramtin.aiplatform.user.CreditSource;
import dev.ramtin.aiplatform.user.User;
import dev.ramtin.aiplatform.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
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
    private ModelPricingService modelPricingService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CreditSourceHandler creditSourceHandler;

    private CostCalculator costCalculator;

    private AIServiceImpl aiService;

    @BeforeEach
    void setUp() {
        costCalculator = new CostCalculator();

        aiService = new AIServiceImpl(
                omniRouteClient,
                aiProperties,
                messageRepository,
                aiRequestRepository,
                usageRecordRepository,
                modelPricingService,
                costCalculator,
                userRepository,
                List.of(creditSourceHandler)
        );
    }

    @Test
    void generateResponse_whenProviderSucceeds_updatesAIRequestSuccessfully() {
        when(aiProperties.getDefaultModel())
                .thenReturn("auto/best-coding");

        ModelPricing pricing = ModelPricing.builder()
                .model("glm-5")
                .inputPricePerMillion(new BigDecimal("0.50"))
                .outputPricePerMillion(new BigDecimal("2.00"))
                .active(true)
                .build();

        when(modelPricingService.getPricing("glm-5"))
                .thenReturn(pricing);

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

        OmniRouteChoice choice =
                new OmniRouteChoice(
                        0,
                        "stop",
                        responseMessage
                );

        OmniRouteUsage usage =
                new OmniRouteUsage(
                        100,
                        20,
                        120
                );

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
                    AIRequest request =
                            invocation.getArgument(0);

                    if (request.getId() == null) {
                        request.setId("ai-request-123");
                    }

                    return request;
                });

        when(usageRecordRepository.save(any(UsageRecord.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        User user = User.builder()
                .id("user123")
                .creditSource(CreditSource.SUBSCRIPTION)
                .build();

        when(userRepository.findById("user123"))
                .thenReturn(Optional.of(user));

        when(creditSourceHandler.getCreditSource())
                .thenReturn(CreditSource.SUBSCRIPTION);

        String result =
                aiService.generateResponse(
                        "user123",
                        "conversation123"
                );

        assertEquals(
                "Hello! How can I help you?",
                result
        );

        ArgumentCaptor<AIRequest> captor =
                ArgumentCaptor.forClass(AIRequest.class);

        verify(aiRequestRepository, times(3))
                .save(captor.capture());

        List<AIRequest> savedRequests =
                captor.getAllValues();

        AIRequest finalRequest =
                savedRequests.get(
                        savedRequests.size() - 1
                );

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
                "glm-5",
                finalRequest.getActualModel()
        );

        assertEquals(
                AIRequestStatus.SUCCESS,
                finalRequest.getStatus()
        );

        assertNotNull(
                finalRequest.getRequestTime()
        );

        assertNotNull(
                finalRequest.getResponseTime()
        );

        verify(omniRouteClient)
                .sendMessage(
                        eq("auto/best-coding"),
                        any(List.class)
                );

        verify(modelPricingService)
                .getPricing("glm-5");

        ArgumentCaptor<UsageRecord> usageCaptor =
                ArgumentCaptor.forClass(
                        UsageRecord.class
                );

        verify(usageRecordRepository)
                .save(usageCaptor.capture());

        UsageRecord savedUsage =
                usageCaptor.getValue();

        assertEquals(
                "user123",
                savedUsage.getUserId()
        );

        assertEquals(
                "conversation123",
                savedUsage.getConversationId()
        );

        assertEquals(
                "glm-5",
                savedUsage.getModel()
        );

        assertEquals(
                100,
                savedUsage.getInputTokens()
        );

        assertEquals(
                20,
                savedUsage.getOutputTokens()
        );

        assertEquals(
                120,
                savedUsage.getTotalTokens()
        );

        assertNotNull(
                savedUsage.getAiRequestId()
        );

        assertNotNull(
                savedUsage.getTimestamp()
        );

        assertEquals(
                0,
                new BigDecimal("0.00009")
                        .compareTo(
                                savedUsage.getProviderCost()
                        )
        );

        assertEquals(
                0,
                new BigDecimal("0.00009")
                        .compareTo(
                                savedUsage.getChargedAmount()
                        )
        );

        verify(userRepository)
                .findById("user123");

        verify(creditSourceHandler)
                .getCreditSource();

        verify(creditSourceHandler)
                .consume(
                        eq("user123"),
                        argThat(amount ->
                                amount.compareTo(
                                        new BigDecimal("0.00009")
                                ) == 0
                        )
                );
    }

    @Test
    void generateResponse_whenProviderFails_marksAIRequestAsProviderError() {
        when(aiProperties.getDefaultModel())
                .thenReturn("auto/best-coding");

        when(messageRepository.findByConversationIdOrderByCreatedAtAsc(
                "conversation123"
        )).thenReturn(List.of());

        when(aiRequestRepository.save(any(AIRequest.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        when(omniRouteClient.sendMessage(
                eq("auto/best-coding"),
                any(List.class)
        )).thenThrow(
                new AIProviderException(
                        "AI provider request failed"
                )
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
                savedRequests.get(
                        savedRequests.size() - 1
                );

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

        assertNotNull(
                finalRequest.getRequestTime()
        );

        assertNotNull(
                finalRequest.getResponseTime()
        );

        verify(usageRecordRepository, never())
                .save(any(UsageRecord.class));

        verify(modelPricingService, never())
                .getPricing(any());

        verify(userRepository, never())
                .findById(any());

        verify(creditSourceHandler, never())
                .consume(any(), any());
    }

    @Test
    void generateResponse_whenPricingServiceReturnsDefaultPricing_succeeds() {
        when(aiProperties.getDefaultModel())
                .thenReturn("auto/best-coding");

        ModelPricing defaultPricing = ModelPricing.builder()
                .model("glm-5")
                .inputPricePerMillion(new BigDecimal("0.50"))
                .outputPricePerMillion(new BigDecimal("2.00"))
                .active(true)
                .build();

        when(modelPricingService.getPricing("glm-5"))
                .thenReturn(defaultPricing);

        when(messageRepository.findByConversationIdOrderByCreatedAtAsc(
                "conversation123"
        )).thenReturn(List.of());

        OmniRouteResponse response =
                new OmniRouteResponse(
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
                        new OmniRouteUsage(
                                100,
                                20,
                                120
                        )
                );

        when(omniRouteClient.sendMessage(
                eq("auto/best-coding"),
                anyList()
        )).thenReturn(response);

        when(aiRequestRepository.save(any(AIRequest.class)))
                .thenAnswer(invocation -> {
                    AIRequest request =
                            invocation.getArgument(0);

                    if (request.getId() == null) {
                        request.setId("ai-request-123");
                    }

                    return request;
                });

        when(usageRecordRepository.save(any(UsageRecord.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        User user = User.builder()
                .id("user123")
                .creditSource(CreditSource.SUBSCRIPTION)
                .build();

        when(userRepository.findById("user123"))
                .thenReturn(Optional.of(user));

        when(creditSourceHandler.getCreditSource())
                .thenReturn(CreditSource.SUBSCRIPTION);

        String result =
                aiService.generateResponse(
                        "user123",
                        "conversation123"
                );

        assertEquals(
                "Hello",
                result
        );

        verify(modelPricingService)
                .getPricing("glm-5");

        verify(usageRecordRepository)
                .save(any(UsageRecord.class));

        verify(creditSourceHandler)
                .consume(
                        eq("user123"),
                        argThat(amount ->
                                amount.compareTo(
                                        new BigDecimal("0.00009")
                                ) == 0
                        )
                );
    }

    @Test
    void generateResponse_whenCreditSourceIsPayAsYouGo_usesPayAsYouGoHandler() {
        when(aiProperties.getDefaultModel())
                .thenReturn("auto/best-coding");

        ModelPricing pricing = ModelPricing.builder()
                .model("glm-5")
                .inputPricePerMillion(new BigDecimal("0.50"))
                .outputPricePerMillion(new BigDecimal("2.00"))
                .active(true)
                .build();

        when(modelPricingService.getPricing("glm-5"))
                .thenReturn(pricing);

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

        OmniRouteChoice choice =
                new OmniRouteChoice(
                        0,
                        "stop",
                        responseMessage
                );

        OmniRouteUsage usage =
                new OmniRouteUsage(
                        100,
                        20,
                        120
                );

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
                    AIRequest request =
                            invocation.getArgument(0);

                    if (request.getId() == null) {
                        request.setId("ai-request-123");
                    }

                    return request;
                });

        when(usageRecordRepository.save(any(UsageRecord.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        User user = User.builder()
                .id("user123")
                .creditSource(CreditSource.PAY_AS_YOU_GO)
                .build();

        when(userRepository.findById("user123"))
                .thenReturn(Optional.of(user));

        when(creditSourceHandler.getCreditSource())
                .thenReturn(CreditSource.PAY_AS_YOU_GO);

        String result =
                aiService.generateResponse(
                        "user123",
                        "conversation123"
                );

        assertEquals(
                "Hello! How can I help you?",
                result
        );

        verify(modelPricingService)
                .getPricing("glm-5");

        verify(userRepository)
                .findById("user123");

        verify(creditSourceHandler)
                .getCreditSource();

        verify(creditSourceHandler)
                .consume(
                        eq("user123"),
                        argThat(amount ->
                                amount.compareTo(
                                        new BigDecimal("0.00009")
                                ) == 0
                        )
                );
    }

    @Test
    void generateResponse_whenPricingFails_propagatesException() {
        when(aiProperties.getDefaultModel())
                .thenReturn("auto/best-coding");

        when(messageRepository.findByConversationIdOrderByCreatedAtAsc(
                "conversation123"
        )).thenReturn(List.of());

        when(aiRequestRepository.save(any(AIRequest.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        OmniRouteResponse response =
                new OmniRouteResponse(
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
                        new OmniRouteUsage(
                                100,
                                20,
                                120
                        )
                );

        when(omniRouteClient.sendMessage(
                eq("auto/best-coding"),
                anyList()
        )).thenReturn(response);

        when(modelPricingService.getPricing("glm-5"))
                .thenThrow(
                        new ModelPricingNotFoundException(
                                "Pricing not found for model: glm-5"
                        )
                );

        assertThrows(
                ModelPricingNotFoundException.class,
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
                savedRequests.get(
                        savedRequests.size() - 1
                );

        assertEquals(
                AIRequestStatus.FAILED,
                finalRequest.getStatus()
        );

        verify(omniRouteClient)
                .sendMessage(
                        eq("auto/best-coding"),
                        anyList()
                );

        verify(modelPricingService)
                .getPricing("glm-5");

        verify(usageRecordRepository, never())
                .save(any(UsageRecord.class));

        verify(userRepository, never())
                .findById(any());

        verify(creditSourceHandler, never())
                .consume(any(), any());
    }

    @Test
    void generateResponse_whenCreditConsumptionFails_marksAIRequestAsFailed() {
        when(aiProperties.getDefaultModel())
                .thenReturn("auto/best-coding");

        when(messageRepository.findByConversationIdOrderByCreatedAtAsc(
                "conversation123"
        )).thenReturn(List.of());

        when(aiRequestRepository.save(any(AIRequest.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        OmniRouteResponse response =
                new OmniRouteResponse(
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
                        new OmniRouteUsage(
                                100,
                                20,
                                120
                        )
                );

        when(omniRouteClient.sendMessage(
                eq("auto/best-coding"),
                anyList()
        )).thenReturn(response);

        ModelPricing pricing =
                ModelPricing.builder()
                        .model("glm-5")
                        .inputPricePerMillion(
                                new BigDecimal("0.50")
                        )
                        .outputPricePerMillion(
                                new BigDecimal("2.00")
                        )
                        .active(true)
                        .build();

        when(modelPricingService.getPricing("glm-5"))
                .thenReturn(pricing);

        User user = User.builder()
                .id("user123")
                .creditSource(CreditSource.SUBSCRIPTION)
                .build();

        when(userRepository.findById("user123"))
                .thenReturn(Optional.of(user));

        when(creditSourceHandler.getCreditSource())
                .thenReturn(CreditSource.SUBSCRIPTION);

        doThrow(
                new RuntimeException("Credit consumption failed")
        ).when(creditSourceHandler)
                .consume(
                        eq("user123"),
                        any(BigDecimal.class)
                );

        assertThrows(
                RuntimeException.class,
                () -> aiService.generateResponse(
                        "user123",
                        "conversation123"
                )
        );

        ArgumentCaptor<AIRequest> captor =
                ArgumentCaptor.forClass(AIRequest.class);

        verify(aiRequestRepository, times(3))
                .save(captor.capture());

        AIRequest finalRequest =
                captor.getAllValues()
                        .get(
                                captor.getAllValues().size() - 1
                        );

        assertEquals(
                AIRequestStatus.FAILED,
                finalRequest.getStatus()
        );

        assertNotNull(
                finalRequest.getResponseTime()
        );

        verify(usageRecordRepository, never())
                .save(any(UsageRecord.class));

        verify(creditSourceHandler)
                .consume(
                        eq("user123"),
                        argThat(amount ->
                                amount.compareTo(
                                        new BigDecimal("0.00009")
                                ) == 0
                        )
                );
    }

    @Test
    void generateResponse_whenFinalAIRequestSaveFails_marksAIRequestAsFailed() {
        when(aiProperties.getDefaultModel())
                .thenReturn("auto/best-coding");

        when(messageRepository.findByConversationIdOrderByCreatedAtAsc(
                "conversation123"
        )).thenReturn(List.of());

        when(aiRequestRepository.save(any(AIRequest.class)))
                .thenAnswer(invocation -> {
                    AIRequest request =
                            invocation.getArgument(0);

                    if (request.getStatus() == AIRequestStatus.SUCCESS) {
                        throw new RuntimeException(
                                "AI request save failed"
                        );
                    }

                    return request;
                });

        OmniRouteResponse response =
                new OmniRouteResponse(
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
                        new OmniRouteUsage(
                                100,
                                20,
                                120
                        )
                );

        when(omniRouteClient.sendMessage(
                eq("auto/best-coding"),
                anyList()
        )).thenReturn(response);

        ModelPricing pricing =
                ModelPricing.builder()
                        .model("glm-5")
                        .inputPricePerMillion(
                                new BigDecimal("0.50")
                        )
                        .outputPricePerMillion(
                                new BigDecimal("2.00")
                        )
                        .active(true)
                        .build();

        when(modelPricingService.getPricing("glm-5"))
                .thenReturn(pricing);

        User user = User.builder()
                .id("user123")
                .creditSource(CreditSource.SUBSCRIPTION)
                .build();

        when(userRepository.findById("user123"))
                .thenReturn(Optional.of(user));

        when(creditSourceHandler.getCreditSource())
                .thenReturn(CreditSource.SUBSCRIPTION);

        doNothing()
                .when(creditSourceHandler)
                .consume(
                        eq("user123"),
                        any(BigDecimal.class)
                );

        UsageRecord usageRecord =
                UsageRecord.builder()
                        .aiRequestId("request123")
                        .userId("user123")
                        .conversationId("conversation123")
                        .model("glm-5")
                        .inputTokens(100)
                        .outputTokens(20)
                        .totalTokens(120)
                        .providerCost(new BigDecimal("0.00009"))
                        .chargedAmount(new BigDecimal("0.00009"))
                        .timestamp(LocalDateTime.now())
                        .build();

        when(usageRecordRepository.save(any(UsageRecord.class)))
                .thenReturn(usageRecord);

        assertThrows(
                RuntimeException.class,
                () -> aiService.generateResponse(
                        "user123",
                        "conversation123"
                )
        );

        ArgumentCaptor<AIRequest> captor =
                ArgumentCaptor.forClass(AIRequest.class);

        verify(aiRequestRepository, times(4))
                .save(captor.capture());

        List<AIRequest> savedRequests =
                captor.getAllValues();

        AIRequest finalRequest =
                savedRequests.get(
                        savedRequests.size() - 1
                );

        assertEquals(
                AIRequestStatus.FAILED,
                finalRequest.getStatus()
        );

        assertNotNull(
                finalRequest.getResponseTime()
        );

        verify(usageRecordRepository)
                .save(any(UsageRecord.class));

        verify(creditSourceHandler)
                .consume(
                        eq("user123"),
                        argThat(amount ->
                                amount.compareTo(
                                        new BigDecimal("0.00009")
                                ) == 0
                        )
                );
    }
}