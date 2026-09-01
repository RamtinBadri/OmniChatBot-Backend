package dev.ramtin.aiplatform.conversation;

import dev.ramtin.aiplatform.conversation.dto.ConversationDTO;
import org.springframework.stereotype.Component;

@Component
public class ConversationMapper {
    public static ConversationDTO toDTO(Conversation conversation) {
        if (conversation == null) {
            return null;
        }
        return ConversationDTO.builder()
                .id(conversation.getId())
                .userId(conversation.getUserId())
                .title(conversation.getTitle())
                .model(conversation.getModel())
                .status(conversation.getStatus())
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .build();
    }
}