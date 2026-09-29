package dev.ramtin.aiplatform.transaction.service;

import dev.ramtin.aiplatform.exception.ResourceNotFoundException;
import dev.ramtin.aiplatform.transaction.Transaction;
import dev.ramtin.aiplatform.transaction.TransactionRepository;
import dev.ramtin.aiplatform.transaction.TransactionStatus;
import dev.ramtin.aiplatform.transaction.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;

    public TransactionServiceImpl(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public Transaction createTransaction(String userId, TransactionType type, BigDecimal amount,
                                         String currency, String reference) {
        validateTransaction(type, amount, currency);

        Transaction transaction = Transaction.builder().userId(userId).type(type).amount(amount)
                .currency(currency).status(TransactionStatus.PENDING)
                .reference(reference).createdAt(LocalDateTime.now()).build();

        return transactionRepository.save(transaction);
    }

    @Override
    public Transaction getTransactionById(String transactionId) {
        return transactionRepository.findById(transactionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Transaction not found with id: " + transactionId
                        ));
    }

    @Override
    public Transaction updateStatus(String transactionId, TransactionStatus status) {
        Transaction transaction = getTransactionById(transactionId);

        transaction.setStatus(status);

        return transactionRepository.save(transaction);
    }

    @Override
    public Page<Transaction> getTransactionHistory(String userId, Pageable pageable) {
        return transactionRepository.findByUserId(userId, pageable);
    }

    // Transaction Validator
    private void validateTransaction(TransactionType type, BigDecimal amount, String currency) {
        if(type == null) {
            throw new IllegalArgumentException("Transaction type cannot be null");
        }

        if(amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transaction amount must be greater than zero");
        }

        if(currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("Transaction currency cannot be empty");
        }
    }
}