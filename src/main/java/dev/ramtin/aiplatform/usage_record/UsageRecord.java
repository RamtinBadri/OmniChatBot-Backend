package dev.ramtin.aiplatform.usage_record;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "usage_records")
public class UsageRecord {
    @Id
    private String id;

    private String aiRequestId;

    private String userId;

    private String conversationId;

    private String model;

    private long inputTokens;

    private long outputTokens;

    private long totalTokens;

    private BigDecimal providerCost;

    private BigDecimal chargedAmount;

    private LocalDateTime timestamp;
}