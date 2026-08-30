package dev.ramtin.aiplatform.model.dto.message;

import dev.ramtin.aiplatform.model.entity.MessageRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {
    private String id;
    private String conversationId;
    private MessageRole role;
    private String content;
    private LocalDateTime createdAt;
}