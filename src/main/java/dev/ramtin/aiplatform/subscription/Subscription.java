package dev.ramtin.aiplatform.subscription;

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
@Document(collection = "subscriptions")
public class Subscription {
    @Id
    private String id;

    private String userId;

    private String planId;

    private BigDecimal pricePaid;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private SubscriptionStatus status;

    private BigDecimal remainingCredit;
}