package dev.ramtin.aiplatform.transaction.repository;

import dev.ramtin.aiplatform.kpi.dto.TransactionKpiSummary;
import dev.ramtin.aiplatform.transaction.Transaction;
import dev.ramtin.aiplatform.transaction.TransactionStatus;
import dev.ramtin.aiplatform.transaction.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TransactionCustomRepository {
    Page<Transaction> findTransactions(String userId, TransactionType type, TransactionStatus status, Pageable pageable);

    TransactionKpiSummary getTransactionKpiSummary();
}