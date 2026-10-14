package dev.ramtin.aiplatform.conversation.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UpdateConversationRequest {
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    private String model;
}