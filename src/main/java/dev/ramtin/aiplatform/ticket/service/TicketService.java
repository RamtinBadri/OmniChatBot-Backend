package dev.ramtin.aiplatform.ticket.service;

import dev.ramtin.aiplatform.ticket.TicketStatus;
import dev.ramtin.aiplatform.ticket.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TicketService {
    TicketDTO createTicket(String userId, CreateTicketRequest request);

    Page<TicketDTO> getUserTickets(String userId, String search, TicketStatus status, Pageable pageable);

    TicketDTO getUserTicket(String ticketId, String userId);

    Page<TicketDTO> getAllTickets(String search, TicketStatus status, String userId, Pageable pageable);

    TicketDTO getAdminTicket(String ticketId);

    void replyToTicket(String ticketId, String adminId, ReplyToTicketRequest request);

    void updateTicketStatus(String ticketId, String adminId, UpdateTicketStatusRequest request);

    Page<TicketHistoryDTO> getTicketHistory(String ticketId, Pageable pageable);
}