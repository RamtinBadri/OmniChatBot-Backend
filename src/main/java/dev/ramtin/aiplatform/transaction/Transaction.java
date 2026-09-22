package dev.ramtin.aiplatform.transaction;

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
@Document(collection = "transactions")
public class Transaction {
    @Id
    private String id;

    private String userId;

    private TransactionType type;

    private BigDecimal amount;

    private String currency;

    private TransactionStatus status;

    private String reference;

    private LocalDateTime createdAt;
}