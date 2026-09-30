package dev.ramtin.aiplatform.transaction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TransactionCustomRepository {
    Page<Transaction> findTransactions(String userId, TransactionType type, TransactionStatus status, Pageable pageable);
}