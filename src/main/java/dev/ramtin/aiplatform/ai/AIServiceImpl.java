package dev.ramtin.aiplatform.ai;

import dev.ramtin.aiplatform.config.AIProperties;
import dev.ramtin.aiplatform.message.Message;
import dev.ramtin.aiplatform.message.MessageRepository;
import dev.ramtin.aiplatform.omniroute.request.OmniRouteMessage;
import dev.ramtin.aiplatform.omniroute.response.OmniRouteResponse;
import dev.ramtin.aiplatform.omniroute.service.OmniRouteClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AIServiceImpl implements AIService {
    private final OmniRouteClient omniRouteClient;
    private final AIProperties aiProperties;
    private final MessageRepository messageRepository;

    public AIServiceImpl(OmniRouteClient omniRouteClient, AIProperties aiProperties, MessageRepository messageRepository) {
        this.omniRouteClient = omniRouteClient;
        this.aiProperties = aiProperties;
        this.messageRepository = messageRepository;
    }

    @Override
    public String generateResponse(String userId, String conversationId) {

        List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);

        List<OmniRouteMessage> omniRouteMessages = messages.stream()
                .map(message -> OmniRouteMessage.builder()
                        .role(message.getRole().name().toLowerCase())
                        .content(message.getContent())
                        .build())
                .toList();

        OmniRouteResponse response = omniRouteClient.sendMessage(aiProperties.getDefaultModel(), omniRouteMessages);

        return response.getChoices().get(0).getMessage().getContent();
    }
}