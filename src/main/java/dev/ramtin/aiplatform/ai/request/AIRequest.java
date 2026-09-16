package dev.ramtin.aiplatform.ai.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "ai_requests")
public class AIRequest {
    @Id
    private String id;

    private String userId;

    private String conversationId;

    private String requestedModel;

    private String actualModel;

    private AIRequestStatus status;

    private LocalDateTime requestTime;

    private LocalDateTime responseTime;
}