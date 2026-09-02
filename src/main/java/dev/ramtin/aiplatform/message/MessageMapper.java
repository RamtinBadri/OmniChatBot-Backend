package dev.ramtin.aiplatform.message;

import dev.ramtin.aiplatform.message.dto.MessageDTO;
import org.springframework.stereotype.Component;

@Component
public class MessageMapper {

    public static MessageDTO toDTO(Message message) {
        if (message == null) {
            return null;
        }
        return MessageDTO.builder()
                .id(message.getId())
                .conversationId(message.getConversationId())
                .role(message.getRole())
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .build();
    }
}