package dev.ramtin.aiplatform.admin;

import dev.ramtin.aiplatform.security.CustomUserDetails;
import dev.ramtin.aiplatform.ticket.TicketStatus;
import dev.ramtin.aiplatform.ticket.dto.ReplyToTicketRequest;
import dev.ramtin.aiplatform.ticket.dto.TicketDTO;
import dev.ramtin.aiplatform.ticket.dto.TicketHistoryDTO;
import dev.ramtin.aiplatform.ticket.dto.UpdateTicketStatusRequest;
import dev.ramtin.aiplatform.ticket.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/tickets")
@RequiredArgsConstructor
public class AdminTicketController {
    private final TicketService ticketService;

    @GetMapping
    public ResponseEntity<Page<TicketDTO>> getAllTickets(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) String userId,
            Pageable pageable) {
        Page<TicketDTO> tickets = ticketService.getAllTickets(search, status, userId, pageable);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/{ticketId}")
    public ResponseEntity<TicketDTO> getTicket(@PathVariable String ticketId) {
        return ResponseEntity.ok(ticketService.getAdminTicket(ticketId));
    }

    @PostMapping("/{ticketId}/replies")
    public ResponseEntity<Void> replyToTicket(@PathVariable String ticketId, @Valid @RequestBody ReplyToTicketRequest request) {
        CustomUserDetails admin = getCurrentUser();
        ticketService.replyToTicket(ticketId, admin.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/{ticketId}/status")
    public ResponseEntity<Void> updateTicketStatus(@PathVariable String ticketId, @Valid @RequestBody UpdateTicketStatusRequest request) {
        CustomUserDetails admin = getCurrentUser();
        ticketService.updateTicketStatus(ticketId, admin.getId(), request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{ticketId}/history")
    public ResponseEntity<Page<TicketHistoryDTO>> getTicketHistory(@PathVariable String ticketId, Pageable pageable) {
        Page<TicketHistoryDTO> history = ticketService.getTicketHistory(ticketId, pageable);
        return ResponseEntity.ok(history);
    }

    private CustomUserDetails getCurrentUser() {
        return (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}