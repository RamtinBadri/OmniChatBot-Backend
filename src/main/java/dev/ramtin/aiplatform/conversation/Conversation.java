package dev.ramtin.aiplatform.conversation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "conversations")
@CompoundIndexes({
        @CompoundIndex(name = "userId_updatedAt",
                def = "{'userId': 1, 'updatedAt': -1}"),
        @CompoundIndex(name = "userId_status_updatedAt",
                def = "{'userId': 1, 'status': 1,'updatedAt': -1}")
})
public class Conversation {
    @Id
    private String id;
    private String userId;
    private String title;
    private String model;
    private ConversationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}