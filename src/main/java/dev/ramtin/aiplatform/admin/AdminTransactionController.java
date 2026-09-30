package dev.ramtin.aiplatform.admin;

import dev.ramtin.aiplatform.transaction.Transaction;
import dev.ramtin.aiplatform.transaction.TransactionStatus;
import dev.ramtin.aiplatform.transaction.TransactionType;
import dev.ramtin.aiplatform.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/transactions")
@RequiredArgsConstructor
public class AdminTransactionController {
    private final TransactionService transactionService;

    @GetMapping
    public ResponseEntity<Page<Transaction>> getTransactions(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) TransactionStatus status,
            Pageable pageable) {
        Page<Transaction> transactions = transactionService.getTransactions(userId, type, status, pageable);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable String transactionId) {
        Transaction transaction = transactionService.getTransactionById(transactionId);
        return ResponseEntity.ok(transaction);
    }

    @PutMapping("/{transactionId}/status")
    public ResponseEntity<Transaction> updateStatus(@PathVariable String transactionId, @RequestParam TransactionStatus status) {
        Transaction transaction = transactionService.updateStatus(transactionId, status);
        return ResponseEntity.ok(transaction);
    }
}