package dev.ramtin.aiplatform.ai;

import dev.ramtin.aiplatform.ai.request.AIRequest;
import dev.ramtin.aiplatform.ai.request.AIRequestRepository;
import dev.ramtin.aiplatform.ai.request.AIRequestStatus;
import dev.ramtin.aiplatform.config.AIProperties;
import dev.ramtin.aiplatform.exception.AIProviderException;
import dev.ramtin.aiplatform.exception.ModelPricingNotFoundException;
import dev.ramtin.aiplatform.message.Message;
import dev.ramtin.aiplatform.message.MessageRepository;
import dev.ramtin.aiplatform.model_pricing.CostCalculator;
import dev.ramtin.aiplatform.model_pricing.ModelPricing;
import dev.ramtin.aiplatform.model_pricing.ModelPricingRepository;
import dev.ramtin.aiplatform.omniroute.request.OmniRouteMessage;
import dev.ramtin.aiplatform.omniroute.response.OmniRouteResponse;
import dev.ramtin.aiplatform.omniroute.response.OmniRouteUsage;
import dev.ramtin.aiplatform.omniroute.service.OmniRouteClient;
import dev.ramtin.aiplatform.usage_record.UsageRecord;
import dev.ramtin.aiplatform.usage_record.UsageRecordRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AIServiceImpl implements AIService {
    private final OmniRouteClient omniRouteClient;
    private final AIProperties aiProperties;
    private final MessageRepository messageRepository;
    private final AIRequestRepository aiRequestRepository;
    private final UsageRecordRepository usageRecordRepository;
    private final ModelPricingRepository modelPricingRepository;
    private final CostCalculator costCalculator;

    public AIServiceImpl(OmniRouteClient omniRouteClient, AIProperties aiProperties,
                         MessageRepository messageRepository, AIRequestRepository aiRequestRepository,
                         UsageRecordRepository usageRecordRepository,
                         ModelPricingRepository modelPricingRepository, CostCalculator costCalculator) {
        this.omniRouteClient = omniRouteClient;
        this.aiProperties = aiProperties;
        this.messageRepository = messageRepository;
        this.aiRequestRepository = aiRequestRepository;
        this.usageRecordRepository = usageRecordRepository;
        this.modelPricingRepository = modelPricingRepository;
        this.costCalculator = costCalculator;
    }

    @Override
    public String generateResponse(String userId, String conversationId) {
        String requestedModel = aiProperties.getDefaultModel();

        AIRequest aiRequest = AIRequest.builder()
                .userId(userId)
                .conversationId(conversationId)
                .requestedModel(requestedModel)
                .status(AIRequestStatus.PENDING)
                .requestTime(LocalDateTime.now())
                .build();

        aiRequest = aiRequestRepository.save(aiRequest);

        aiRequest.setStatus(AIRequestStatus.PROCESSING);
        aiRequestRepository.save(aiRequest);

        List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);

        List<OmniRouteMessage> omniRouteMessages = messages.stream()
                .map(message -> OmniRouteMessage.builder()
                        .role(message.getRole().name().toLowerCase())
                        .content(message.getContent())
                        .build())
                .toList();
        try{
            OmniRouteResponse response = omniRouteClient.sendMessage(requestedModel, omniRouteMessages);

            ModelPricing pricing = modelPricingRepository
                    .findByModelAndActiveTrue(response.getModel())
                    .orElseThrow(() ->
                            new ModelPricingNotFoundException(
                                    "No active pricing found for model: "
                                            + response.getModel()
                            )
                    );

            OmniRouteUsage usage = response.getUsage();

            BigDecimal providerCost = costCalculator.calculate(
                    usage.getPromptTokens(),
                    usage.getCompletionTokens(),
                    pricing
            );

            UsageRecord usageRecord = UsageRecord.builder()
                    .aiRequestId(aiRequest.getId())
                    .userId(userId)
                    .conversationId(conversationId)
                    .model(response.getModel())
                    .inputTokens(usage.getPromptTokens())
                    .outputTokens(usage.getCompletionTokens())
                    .totalTokens(usage.getTotalTokens())
                    .providerCost(providerCost)
                    .chargedAmount(providerCost)
                    .timestamp(LocalDateTime.now())
                    .build();

            usageRecordRepository.save(usageRecord);

            aiRequest.setActualModel(response.getModel());
            aiRequest.setStatus(AIRequestStatus.SUCCESS);
            aiRequest.setResponseTime(LocalDateTime.now());

            aiRequestRepository.save(aiRequest);

            return response.getChoices().get(0).getMessage().getContent();
        } catch (AIProviderException exception) {
            aiRequest.setStatus(AIRequestStatus.PROVIDER_ERROR);
            aiRequest.setResponseTime(LocalDateTime.now());

            aiRequestRepository.save(aiRequest);

            throw exception;
        }
    }
}