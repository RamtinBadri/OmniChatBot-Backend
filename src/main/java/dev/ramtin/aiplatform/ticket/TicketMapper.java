package dev.ramtin.aiplatform.ticket;

import dev.ramtin.aiplatform.ticket.dto.TicketDTO;
import org.springframework.stereotype.Component;

@Component
public class TicketMapper {
    public TicketDTO toDTO(Ticket ticket) {
        return TicketDTO.builder()
                .id(ticket.getId())
                .userId(ticket.getUserId())
                .subject(ticket.getSubject())
                .description(ticket.getDescription())
                .status(ticket.getStatus())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .build();
    }
}