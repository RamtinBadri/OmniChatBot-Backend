package dev.ramtin.aiplatform.plan;

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
@Document(collection = "plans")
public class Plan {
    @Id
    private String id;

    private String name;

    private BigDecimal price;

    private int duration;

    private BigDecimal creditLimit;

    private boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}