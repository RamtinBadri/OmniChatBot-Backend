package dev.ramtin.aiplatform.ticket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "ticket_history")
@CompoundIndex(
        name = "ticketId_createdAt",
        def = "{'ticketId': 1, 'createdAt': 1}"
)
public class TicketHistory {
    @Id
    private String id;
    private String ticketId;
    private String userId;   // id of user who performed the action
    private TicketHistoryType type;
    private TicketStatus status;  // New status, only meaningful for STATUS_CHANGE
    private String message;   // Reply text, only meaningful for REPLY
    private LocalDateTime createdAt;
}