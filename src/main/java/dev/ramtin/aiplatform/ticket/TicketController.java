package dev.ramtin.aiplatform.ticket;

import dev.ramtin.aiplatform.security.CustomUserDetails;
import dev.ramtin.aiplatform.ticket.dto.CreateTicketRequest;
import dev.ramtin.aiplatform.ticket.dto.TicketDTO;
import dev.ramtin.aiplatform.ticket.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {
    private final TicketService ticketService;

    @PostMapping
    public ResponseEntity<TicketDTO> createTicket(@Valid @RequestBody CreateTicketRequest request) {
        CustomUserDetails userDetails = getCurrentUser();
        TicketDTO ticket = ticketService.createTicket(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ticket);
    }

    @GetMapping
    public ResponseEntity<Page<TicketDTO>> getUserTickets(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) TicketStatus status,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        CustomUserDetails userDetails = getCurrentUser();
        Page<TicketDTO> tickets = ticketService.getUserTickets(userDetails.getId(), search, status, pageable);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/{ticketId}")
    public ResponseEntity<TicketDTO> getUserTicket(@PathVariable String ticketId) {
        CustomUserDetails userDetails = getCurrentUser();
        TicketDTO ticket = ticketService.getUserTicket(ticketId, userDetails.getId());
        return ResponseEntity.ok(ticket);
    }

    private CustomUserDetails getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (CustomUserDetails) authentication.getPrincipal();
    }
}