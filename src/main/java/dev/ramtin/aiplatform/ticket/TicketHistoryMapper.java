package dev.ramtin.aiplatform.ticket;

import dev.ramtin.aiplatform.ticket.dto.TicketHistoryDTO;
import org.springframework.stereotype.Component;

@Component
public class TicketHistoryMapper {
    public TicketHistoryDTO toDTO(TicketHistory history) {
        return TicketHistoryDTO.builder()
                .id(history.getId())
                .ticketId(history.getTicketId())
                .userId(history.getUserId())
                .type(history.getType())
                .status(history.getStatus())
                .message(history.getMessage())
                .createdAt(history.getCreatedAt())
                .build();
    }
}