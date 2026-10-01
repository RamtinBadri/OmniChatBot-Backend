package dev.ramtin.aiplatform.subscription;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "subscriptions")
@CompoundIndex(name = "userId_status", def = "{'userId': 1, 'status': 1}")
public class Subscription {
    @Id
    private String id;
    private String userId;
    @Indexed(name = "planId")
    private String planId;
    private BigDecimal pricePaid;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private SubscriptionStatus status;
    private BigDecimal remainingCredit;
}