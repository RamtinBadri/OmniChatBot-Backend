package dev.ramtin.aiplatform.ai;

public interface AIService {
    String generateResponse(String userId, String conversationId, String requestedModel);
}