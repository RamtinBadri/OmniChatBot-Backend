package dev.ramtin.aiplatform.transaction.service;

import dev.ramtin.aiplatform.transaction.Transaction;
import dev.ramtin.aiplatform.transaction.TransactionStatus;
import dev.ramtin.aiplatform.transaction.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface TransactionService {
    Transaction createTransaction(String userId, TransactionType type, BigDecimal amount,
                                  String currency, String reference);

    Transaction getTransactionById(String transactionId);

    Transaction updateStatus(String transactionId, TransactionStatus status);

    Page<Transaction> getTransactionHistory(String userId, Pageable pageable);
}
