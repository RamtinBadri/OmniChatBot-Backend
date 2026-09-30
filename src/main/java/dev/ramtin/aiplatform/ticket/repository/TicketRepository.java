package dev.ramtin.aiplatform.ticket.repository;

import dev.ramtin.aiplatform.ticket.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TicketRepository extends MongoRepository<Ticket, String> {
    Page<Ticket> findByUserId(String userId, Pageable pageable);
}