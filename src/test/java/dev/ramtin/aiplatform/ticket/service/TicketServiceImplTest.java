package dev.ramtin.aiplatform.ticket.service;

import dev.ramtin.aiplatform.exception.ResourceNotFoundException;
import dev.ramtin.aiplatform.ticket.*;
import dev.ramtin.aiplatform.ticket.dto.CreateTicketRequest;
import dev.ramtin.aiplatform.ticket.dto.ReplyToTicketRequest;
import dev.ramtin.aiplatform.ticket.dto.TicketDTO;
import dev.ramtin.aiplatform.ticket.dto.TicketHistoryDTO;
import dev.ramtin.aiplatform.ticket.dto.UpdateTicketStatusRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketServiceImplTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private TicketHistoryRepository ticketHistoryRepository;

    @Mock
    private TicketMapper ticketMapper;

    @Mock
    private TicketHistoryMapper ticketHistoryMapper;

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private TicketServiceImpl ticketService;

    @Test
    void createTicket_createsAndReturnsTicketDTO() {
        CreateTicketRequest request = new CreateTicketRequest();
        request.setSubject("Cannot login");
        request.setDescription("I cannot access my account.");

        Ticket savedTicket = Ticket.builder()
                .id("ticket123")
                .userId("user123")
                .subject("Cannot login")
                .description("I cannot access my account.")
                .status(TicketStatus.OPEN)
                .build();

        TicketDTO ticketDTO = TicketDTO.builder()
                .id("ticket123")
                .userId("user123")
                .subject("Cannot login")
                .description("I cannot access my account.")
                .status(TicketStatus.OPEN)
                .build();

        when(ticketRepository.save(any(Ticket.class)))
                .thenReturn(savedTicket);

        when(ticketMapper.toDTO(savedTicket))
                .thenReturn(ticketDTO);

        TicketDTO result = ticketService.createTicket(
                "user123",
                request
        );

        assertNotNull(result);
        assertEquals("ticket123", result.getId());
        assertEquals("user123", result.getUserId());
        assertEquals("Cannot login", result.getSubject());
        assertEquals(TicketStatus.OPEN, result.getStatus());

        ArgumentCaptor<Ticket> captor =
                ArgumentCaptor.forClass(Ticket.class);

        verify(ticketRepository).save(captor.capture());

        Ticket ticketToSave = captor.getValue();

        assertEquals("user123", ticketToSave.getUserId());
        assertEquals("Cannot login", ticketToSave.getSubject());
        assertEquals(
                "I cannot access my account.",
                ticketToSave.getDescription()
        );
        assertEquals(
                TicketStatus.OPEN,
                ticketToSave.getStatus()
        );
        assertNotNull(ticketToSave.getCreatedAt());
        assertNotNull(ticketToSave.getUpdatedAt());

        verify(ticketMapper).toDTO(savedTicket);
    }

    @Test
    void getUserTickets_returnsPaginatedTickets() {
        Pageable pageable = PageRequest.of(0, 20);

        Ticket ticket1 = Ticket.builder()
                .id("ticket1")
                .userId("user123")
                .subject("Login issue")
                .status(TicketStatus.OPEN)
                .build();

        Ticket ticket2 = Ticket.builder()
                .id("ticket2")
                .userId("user123")
                .subject("Payment issue")
                .status(TicketStatus.IN_PROGRESS)
                .build();

        TicketDTO dto1 = TicketDTO.builder()
                .id("ticket1")
                .userId("user123")
                .subject("Login issue")
                .status(TicketStatus.OPEN)
                .build();

        TicketDTO dto2 = TicketDTO.builder()
                .id("ticket2")
                .userId("user123")
                .subject("Payment issue")
                .status(TicketStatus.IN_PROGRESS)
                .build();

        when(mongoTemplate.count(
                any(Query.class),
                eq(Ticket.class)
        )).thenReturn(2L);

        when(mongoTemplate.find(
                any(Query.class),
                eq(Ticket.class)
        )).thenReturn(List.of(ticket1, ticket2));

        when(ticketMapper.toDTO(ticket1))
                .thenReturn(dto1);

        when(ticketMapper.toDTO(ticket2))
                .thenReturn(dto2);

        var result = ticketService.getUserTickets(
                "user123",
                null,
                null,
                pageable
        );

        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertEquals(
                "ticket1",
                result.getContent().get(0).getId()
        );
        assertEquals(
                "ticket2",
                result.getContent().get(1).getId()
        );

        verify(mongoTemplate).count(
                any(Query.class),
                eq(Ticket.class)
        );

        verify(mongoTemplate).find(
                any(Query.class),
                eq(Ticket.class)
        );
    }

    @Test
    void getUserTicket_whenUserOwnsTicket_returnsTicketDTO() {
        Ticket ticket = Ticket.builder()
                .id("ticket123")
                .userId("user123")
                .subject("Cannot login")
                .status(TicketStatus.OPEN)
                .build();

        TicketDTO ticketDTO = TicketDTO.builder()
                .id("ticket123")
                .userId("user123")
                .subject("Cannot login")
                .status(TicketStatus.OPEN)
                .build();

        when(ticketRepository.findById("ticket123"))
                .thenReturn(Optional.of(ticket));

        when(ticketMapper.toDTO(ticket))
                .thenReturn(ticketDTO);

        TicketDTO result = ticketService.getUserTicket(
                "ticket123",
                "user123"
        );

        assertNotNull(result);
        assertEquals("ticket123", result.getId());
        assertEquals("user123", result.getUserId());
        assertEquals("Cannot login", result.getSubject());

        verify(ticketRepository).findById("ticket123");
        verify(ticketMapper).toDTO(ticket);
    }

    @Test
    void getUserTicket_whenTicketDoesNotExist_throwsResourceNotFoundException() {
        when(ticketRepository.findById("ticket123"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> ticketService.getUserTicket(
                        "ticket123",
                        "user123"
                )
        );

        verify(ticketRepository).findById("ticket123");
        verifyNoInteractions(ticketMapper);
    }

    @Test
    void getUserTicket_whenUserDoesNotOwnTicket_throwsAccessDeniedException() {
        Ticket ticket = Ticket.builder()
                .id("ticket123")
                .userId("owner123")
                .subject("Cannot login")
                .status(TicketStatus.OPEN)
                .build();

        when(ticketRepository.findById("ticket123"))
                .thenReturn(Optional.of(ticket));

        assertThrows(
                AccessDeniedException.class,
                () -> ticketService.getUserTicket(
                        "ticket123",
                        "otherUser"
                )
        );

        verify(ticketRepository).findById("ticket123");
        verifyNoInteractions(ticketMapper);
    }

    @Test
    void getAllTickets_returnsPaginatedTickets() {
        Pageable pageable = PageRequest.of(0, 20);

        Ticket ticket1 = Ticket.builder()
                .id("ticket1")
                .userId("user1")
                .subject("Login issue")
                .status(TicketStatus.OPEN)
                .build();

        Ticket ticket2 = Ticket.builder()
                .id("ticket2")
                .userId("user2")
                .subject("Payment issue")
                .status(TicketStatus.IN_PROGRESS)
                .build();

        TicketDTO dto1 = TicketDTO.builder()
                .id("ticket1")
                .userId("user1")
                .subject("Login issue")
                .status(TicketStatus.OPEN)
                .build();

        TicketDTO dto2 = TicketDTO.builder()
                .id("ticket2")
                .userId("user2")
                .subject("Payment issue")
                .status(TicketStatus.IN_PROGRESS)
                .build();

        when(mongoTemplate.count(
                any(Query.class),
                eq(Ticket.class)
        )).thenReturn(2L);

        when(mongoTemplate.find(
                any(Query.class),
                eq(Ticket.class)
        )).thenReturn(List.of(ticket1, ticket2));

        when(ticketMapper.toDTO(ticket1))
                .thenReturn(dto1);

        when(ticketMapper.toDTO(ticket2))
                .thenReturn(dto2);

        var result = ticketService.getAllTickets(
                null,
                null,
                null,
                pageable
        );

        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertEquals(
                "ticket1",
                result.getContent().get(0).getId()
        );
        assertEquals(
                "ticket2",
                result.getContent().get(1).getId()
        );

        verify(mongoTemplate).count(
                any(Query.class),
                eq(Ticket.class)
        );

        verify(mongoTemplate).find(
                any(Query.class),
                eq(Ticket.class)
        );
    }

    @Test
    void getAdminTicket_whenTicketExists_returnsTicketDTO() {
        Ticket ticket = Ticket.builder()
                .id("ticket123")
                .userId("user123")
                .subject("Cannot login")
                .status(TicketStatus.OPEN)
                .build();

        TicketDTO ticketDTO = TicketDTO.builder()
                .id("ticket123")
                .userId("user123")
                .subject("Cannot login")
                .status(TicketStatus.OPEN)
                .build();

        when(ticketRepository.findById("ticket123"))
                .thenReturn(Optional.of(ticket));

        when(ticketMapper.toDTO(ticket))
                .thenReturn(ticketDTO);

        TicketDTO result =
                ticketService.getAdminTicket("ticket123");

        assertNotNull(result);
        assertEquals("ticket123", result.getId());
        assertEquals("Cannot login", result.getSubject());

        verify(ticketRepository).findById("ticket123");
        verify(ticketMapper).toDTO(ticket);
    }

    @Test
    void getAdminTicket_whenTicketDoesNotExist_throwsResourceNotFoundException() {
        when(ticketRepository.findById("ticket123"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> ticketService.getAdminTicket("ticket123")
        );

        verify(ticketRepository).findById("ticket123");
        verifyNoInteractions(ticketMapper);
    }

    @Test
    void replyToTicket_createsHistoryAndUpdatesTicket() {
        ReplyToTicketRequest request =
                new ReplyToTicketRequest();

        request.setMessage(
                "We are working on your issue."
        );

        Ticket ticket = Ticket.builder()
                .id("ticket123")
                .userId("user123")
                .subject("Cannot login")
                .status(TicketStatus.OPEN)
                .build();

        when(ticketRepository.findById("ticket123"))
                .thenReturn(Optional.of(ticket));

        ticketService.replyToTicket(
                "ticket123",
                "admin123",
                request
        );

        ArgumentCaptor<TicketHistory> historyCaptor =
                ArgumentCaptor.forClass(TicketHistory.class);

        verify(ticketHistoryRepository)
                .save(historyCaptor.capture());

        TicketHistory history = historyCaptor.getValue();

        assertEquals(
                "ticket123",
                history.getTicketId()
        );
        assertEquals(
                "admin123",
                history.getUserId()
        );
        assertEquals(
                TicketHistoryType.REPLY,
                history.getType()
        );
        assertEquals(
                "We are working on your issue.",
                history.getMessage()
        );
        assertNotNull(history.getCreatedAt());

        verify(ticketRepository).save(ticket);
        assertNotNull(ticket.getUpdatedAt());
    }

    @Test
    void replyToTicket_whenTicketDoesNotExist_throwsResourceNotFoundException() {
        ReplyToTicketRequest request =
                new ReplyToTicketRequest();

        request.setMessage(
                "We are working on your issue."
        );

        when(ticketRepository.findById("ticket123"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> ticketService.replyToTicket(
                        "ticket123",
                        "admin123",
                        request
                )
        );

        verify(ticketRepository).findById("ticket123");
        verifyNoInteractions(ticketHistoryRepository);
    }

    @Test
    void updateTicketStatus_changesStatusAndCreatesHistory() {
        UpdateTicketStatusRequest request =
                new UpdateTicketStatusRequest();

        request.setStatus(TicketStatus.IN_PROGRESS);

        Ticket ticket = Ticket.builder()
                .id("ticket123")
                .userId("user123")
                .subject("Cannot login")
                .status(TicketStatus.OPEN)
                .build();

        when(ticketRepository.findById("ticket123"))
                .thenReturn(Optional.of(ticket));

        ticketService.updateTicketStatus(
                "ticket123",
                "admin123",
                request
        );

        assertEquals(
                TicketStatus.IN_PROGRESS,
                ticket.getStatus()
        );

        assertNotNull(ticket.getUpdatedAt());

        verify(ticketRepository).save(ticket);

        ArgumentCaptor<TicketHistory> historyCaptor =
                ArgumentCaptor.forClass(TicketHistory.class);

        verify(ticketHistoryRepository)
                .save(historyCaptor.capture());

        TicketHistory history = historyCaptor.getValue();

        assertEquals(
                "ticket123",
                history.getTicketId()
        );
        assertEquals(
                "admin123",
                history.getUserId()
        );
        assertEquals(
                TicketHistoryType.STATUS_CHANGE,
                history.getType()
        );
        assertEquals(
                TicketStatus.IN_PROGRESS,
                history.getStatus()
        );
        assertNotNull(history.getCreatedAt());
    }

    @Test
    void updateTicketStatus_whenStatusIsAlreadySame_doesNothing() {
        UpdateTicketStatusRequest request =
                new UpdateTicketStatusRequest();

        request.setStatus(TicketStatus.OPEN);

        Ticket ticket = Ticket.builder()
                .id("ticket123")
                .userId("user123")
                .subject("Cannot login")
                .status(TicketStatus.OPEN)
                .build();

        when(ticketRepository.findById("ticket123"))
                .thenReturn(Optional.of(ticket));

        ticketService.updateTicketStatus(
                "ticket123",
                "admin123",
                request
        );

        verify(ticketRepository).findById("ticket123");

        verify(
                ticketRepository,
                never()
        ).save(any(Ticket.class));

        verifyNoInteractions(ticketHistoryRepository);
    }

    @Test
    void updateTicketStatus_whenTicketDoesNotExist_throwsResourceNotFoundException() {
        UpdateTicketStatusRequest request =
                new UpdateTicketStatusRequest();

        request.setStatus(TicketStatus.IN_PROGRESS);

        when(ticketRepository.findById("ticket123"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> ticketService.updateTicketStatus(
                        "ticket123",
                        "admin123",
                        request
                )
        );

        verify(ticketRepository).findById("ticket123");

        verify(
                ticketRepository,
                never()
        ).save(any(Ticket.class));

        verifyNoInteractions(ticketHistoryRepository);
    }

    @Test
    void getTicketHistory_returnsPaginatedHistory() {
        Pageable pageable = PageRequest.of(0, 20);

        TicketHistory history1 = TicketHistory.builder()
                .id("history1")
                .ticketId("ticket123")
                .userId("admin123")
                .type(TicketHistoryType.STATUS_CHANGE)
                .status(TicketStatus.IN_PROGRESS)
                .build();

        TicketHistory history2 = TicketHistory.builder()
                .id("history2")
                .ticketId("ticket123")
                .userId("admin123")
                .type(TicketHistoryType.REPLY)
                .message("We are working on it.")
                .build();

        TicketHistoryDTO dto1 = TicketHistoryDTO.builder()
                .id("history1")
                .ticketId("ticket123")
                .userId("admin123")
                .type(TicketHistoryType.STATUS_CHANGE)
                .status(TicketStatus.IN_PROGRESS)
                .build();

        TicketHistoryDTO dto2 = TicketHistoryDTO.builder()
                .id("history2")
                .ticketId("ticket123")
                .userId("admin123")
                .type(TicketHistoryType.REPLY)
                .message("We are working on it.")
                .build();

        when(ticketRepository.existsById("ticket123")).thenReturn(true);

        when(ticketHistoryRepository.findByTicketId(
                "ticket123", pageable)).thenReturn(
                new PageImpl<>(
                        List.of(history1, history2),
                        pageable,
                        2
                )
        );

        when(ticketHistoryMapper.toDTO(history1)).thenReturn(dto1);

        when(ticketHistoryMapper.toDTO(history2)).thenReturn(dto2);

        var result = ticketService.getTicketHistory("ticket123", pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertEquals(
                "history1",
                result.getContent().get(0).getId()
        );
        assertEquals(
                "history2",
                result.getContent().get(1).getId()
        );

        verify(ticketRepository)
                .existsById("ticket123");

        verify(ticketHistoryRepository)
                .findByTicketId(
                        "ticket123",
                        pageable
                );
    }

    @Test
    void getTicketHistory_whenTicketDoesNotExist_throwsResourceNotFoundException() {
        Pageable pageable = PageRequest.of(0, 20);

        when(ticketRepository.existsById("ticket123")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> ticketService.getTicketHistory(
                        "ticket123",
                        pageable
                )
        );

        verify(ticketRepository).existsById("ticket123");

        verifyNoInteractions(ticketHistoryRepository);
        verifyNoInteractions(ticketHistoryMapper);
    }
}