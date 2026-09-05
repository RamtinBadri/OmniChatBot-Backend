package dev.ramtin.aiplatform.ticket.dto;

import dev.ramtin.aiplatform.ticket.TicketStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTicketStatusRequest {
    @NotNull
    private TicketStatus status;
}