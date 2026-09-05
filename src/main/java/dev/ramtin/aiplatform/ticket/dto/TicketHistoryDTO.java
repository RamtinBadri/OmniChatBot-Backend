package dev.ramtin.aiplatform.ticket.dto;

import dev.ramtin.aiplatform.ticket.TicketHistoryType;
import dev.ramtin.aiplatform.ticket.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketHistoryDTO {
    private String id;
    private String ticketId;
    private String userId;
    private TicketHistoryType type;
    private TicketStatus status;
    private String message;
    private LocalDateTime createdAt;
}