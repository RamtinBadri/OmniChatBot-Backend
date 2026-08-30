package dev.ramtin.aiplatform.message.dto;

import dev.ramtin.aiplatform.message.MessageRole;
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