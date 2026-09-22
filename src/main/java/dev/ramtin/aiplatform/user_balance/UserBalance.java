package dev.ramtin.aiplatform.user_balance;

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
@Document(collection = "user_balances")
public class UserBalance {
    @Id
    private String id;

    private String userId;

    private BigDecimal balance;

    private String currency;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}