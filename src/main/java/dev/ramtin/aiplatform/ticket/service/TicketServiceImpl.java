package dev.ramtin.aiplatform.ticket.service;

import dev.ramtin.aiplatform.ticket.Ticket;
import dev.ramtin.aiplatform.ticket.TicketHistory;
import dev.ramtin.aiplatform.ticket.TicketHistoryMapper;
import dev.ramtin.aiplatform.ticket.TicketHistoryRepository;
import dev.ramtin.aiplatform.ticket.TicketHistoryType;
import dev.ramtin.aiplatform.ticket.TicketMapper;
import dev.ramtin.aiplatform.ticket.TicketRepository;
import dev.ramtin.aiplatform.ticket.TicketStatus;
import dev.ramtin.aiplatform.ticket.dto.CreateTicketRequest;
import dev.ramtin.aiplatform.ticket.dto.ReplyToTicketRequest;
import dev.ramtin.aiplatform.ticket.dto.TicketDTO;
import dev.ramtin.aiplatform.ticket.dto.TicketHistoryDTO;
import dev.ramtin.aiplatform.ticket.dto.UpdateTicketStatusRequest;
import dev.ramtin.aiplatform.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {
    private final TicketRepository ticketRepository;
    private final TicketHistoryRepository ticketHistoryRepository;
    private final TicketMapper ticketMapper;
    private final TicketHistoryMapper ticketHistoryMapper;
    private final MongoTemplate mongoTemplate;

    @Override
    public TicketDTO createTicket(String userId, CreateTicketRequest request) {
        LocalDateTime now = LocalDateTime.now();

        Ticket ticket = Ticket.builder()
                .userId(userId)
                .subject(request.getSubject())
                .description(request.getDescription())
                .status(TicketStatus.OPEN)
                .createdAt(now)
                .updatedAt(now)
                .build();

        Ticket savedTicket = ticketRepository.save(ticket);
        return ticketMapper.toDTO(savedTicket);
    }

    @Override
    public Page<TicketDTO> getUserTickets(String userId, String search, TicketStatus status, Pageable pageable) {
        Query query = new Query();

        query.addCriteria(Criteria.where("userId").is(userId));

        addSearchAndStatusCriteria(query, search, status);

        long total = mongoTemplate.count(query, Ticket.class);

        query.with(pageable);

        List<Ticket> tickets = mongoTemplate.find(query, Ticket.class);

        List<TicketDTO> ticketDTOs = tickets.stream().map(ticketMapper::toDTO).toList();

        return new PageImpl<>(ticketDTOs, pageable, total);
    }

    @Override
    public TicketDTO getUserTicket(String ticketId, String userId) {
        Ticket ticket = getTicketForUser(ticketId, userId);
        return ticketMapper.toDTO(ticket);
    }

    @Override
    public Page<TicketDTO> getAllTickets(String search, TicketStatus status, String userId, Pageable pageable) {
        Query query = new Query();

        if (userId != null && !userId.isBlank()) {
            query.addCriteria(Criteria.where("userId").is(userId));
        }

        addSearchAndStatusCriteria(query, search, status);
        long total = mongoTemplate.count(query, Ticket.class);
        query.with(pageable);

        List<Ticket> tickets = mongoTemplate.find(query, Ticket.class);

        List<TicketDTO> ticketDTOs = tickets.stream().map(ticketMapper::toDTO).toList();
        return new PageImpl<>(ticketDTOs, pageable, total);
    }

    @Override
    public TicketDTO getAdminTicket(String ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(() ->
                new ResourceNotFoundException("Ticket not found with id: " + ticketId));
        return ticketMapper.toDTO(ticket);
    }

    @Override
    public void replyToTicket(String ticketId, String adminId, ReplyToTicketRequest request) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(() ->
                new ResourceNotFoundException("Ticket not found with id: " + ticketId));

        TicketHistory history = TicketHistory.builder()
                .ticketId(ticket.getId())
                .userId(adminId)
                .type(TicketHistoryType.REPLY)
                .message(request.getMessage())
                .createdAt(LocalDateTime.now())
                .build();

        ticketHistoryRepository.save(history);

        ticket.setUpdatedAt(LocalDateTime.now());
        ticketRepository.save(ticket);
    }

    @Override
    public void updateTicketStatus(String ticketId, String adminId, UpdateTicketStatusRequest request) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(() ->
                new ResourceNotFoundException("Ticket not found with id: " + ticketId));

        TicketStatus newStatus = request.getStatus();

        if (ticket.getStatus() == newStatus) {
            return;
        }

        ticket.setStatus(newStatus);
        ticket.setUpdatedAt(LocalDateTime.now());

        ticketRepository.save(ticket);

        TicketHistory history = TicketHistory.builder()
                .ticketId(ticket.getId())
                .userId(adminId)
                .type(TicketHistoryType.STATUS_CHANGE)
                .status(newStatus)
                .createdAt(LocalDateTime.now())
                .build();

        ticketHistoryRepository.save(history);
    }

    @Override
    public Page<TicketHistoryDTO> getTicketHistory(String ticketId, Pageable pageable) {
        if (!ticketRepository.existsById(ticketId)) {
            throw new ResourceNotFoundException("Ticket not found with id: " + ticketId);
        }

        Page<TicketHistory> history = ticketHistoryRepository.findByTicketId(ticketId, pageable);
        return history.map(ticketHistoryMapper::toDTO);
    }

    private Ticket getTicketForUser(String ticketId, String userId) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(() ->
                new ResourceNotFoundException("Ticket not found with id: " + ticketId));

        if (!ticket.getUserId().equals(userId)) {
            throw new org.springframework.security.access.AccessDeniedException("You do not have access to this ticket");
        }
        return ticket;
    }

    private void addSearchAndStatusCriteria(Query query, String search, TicketStatus status) {
        if (search != null && !search.isBlank()) {
            Criteria searchCriteria = new Criteria().orOperator(
                    Criteria.where("subject").regex(search, "i"),
                    Criteria.where("description").regex(search, "i")
            );
            query.addCriteria(searchCriteria);
        }
        if (status != null) {
            query.addCriteria(Criteria.where("status").is(status));
        }
    }
}