package dev.ramtin.aiplatform.ticket;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TicketHistoryRepository extends MongoRepository<TicketHistory, String> {
    Page<TicketHistory> findByTicketId(String ticketId, Pageable pageable);
}