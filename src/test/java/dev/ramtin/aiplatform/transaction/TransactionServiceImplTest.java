package dev.ramtin.aiplatform.transaction;

import dev.ramtin.aiplatform.exception.ResourceNotFoundException;
import dev.ramtin.aiplatform.transaction.service.TransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionServiceImplTest {
    @Mock
    private TransactionRepository transactionRepository;

    private TransactionServiceImpl transactionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        transactionService =
                new TransactionServiceImpl(transactionRepository);
    }

    @Test
    void shouldCreateTransactionSuccessfully() {
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result =
                transactionService.createTransaction(
                        "user-1",
                        TransactionType.CREDIT_PURCHASE,
                        BigDecimal.TEN,
                        "USD",
                        "credit-ref-1"
                );

        assertNotNull(result);
        assertEquals("user-1", result.getUserId());

        assertEquals(
                TransactionType.CREDIT_PURCHASE,
                result.getType()
        );

        assertEquals(BigDecimal.TEN, result.getAmount());

        assertEquals("USD", result.getCurrency());

        assertEquals(TransactionStatus.PENDING, result.getStatus());

        assertEquals("credit-ref-1", result.getReference());

        assertNotNull(result.getCreatedAt());

        verify(transactionRepository)
                .save(any(Transaction.class));
    }

    @Test
    void shouldGetTransactionSuccessfully() {
        Transaction transaction = Transaction.builder()
                .id("transaction-1")
                .userId("user-1")
                .type(TransactionType.CREDIT_PURCHASE)
                .amount(BigDecimal.TEN)
                .currency("USD")
                .status(TransactionStatus.PENDING)
                .build();

        when(transactionRepository.findById("transaction-1"))
                .thenReturn(Optional.of(transaction));

        Transaction result =
                transactionService.getTransactionById(
                        "transaction-1"
                );

        assertEquals(
                "transaction-1",
                result.getId()
        );

        assertEquals(
                "user-1",
                result.getUserId()
        );
    }

    @Test
    void shouldThrowExceptionWhenTransactionDoesNotExist() {
        when(transactionRepository.findById("transaction-1"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> transactionService.getTransactionById(
                        "transaction-1"
                )
        );
    }

    @Test
    void shouldUpdateTransactionStatusSuccessfully() {
        Transaction transaction = Transaction.builder()
                .id("transaction-1")
                .userId("user-1")
                .type(TransactionType.CREDIT_PURCHASE)
                .amount(BigDecimal.TEN)
                .currency("USD")
                .status(TransactionStatus.PENDING)
                .build();

        when(transactionRepository.findById("transaction-1"))
                .thenReturn(Optional.of(transaction));

        when(transactionRepository.save(transaction))
                .thenReturn(transaction);

        Transaction result =
                transactionService.updateStatus(
                        "transaction-1",
                        TransactionStatus.SUCCESS
                );

        assertEquals(
                TransactionStatus.SUCCESS,
                result.getStatus()
        );

        verify(transactionRepository)
                .save(transaction);
    }

    @Test
    void shouldUpdateTransactionToFailed() {
        Transaction transaction = Transaction.builder()
                .id("transaction-1")
                .userId("user-1")
                .type(TransactionType.CREDIT_PURCHASE)
                .amount(BigDecimal.TEN)
                .currency("USD")
                .status(TransactionStatus.PENDING)
                .build();

        when(transactionRepository.findById("transaction-1")).thenReturn(Optional.of(transaction));

        when(transactionRepository.save(transaction)).thenReturn(transaction);

        Transaction result = transactionService.updateStatus("transaction-1", TransactionStatus.FAILED);

        assertEquals(TransactionStatus.FAILED, result.getStatus());
    }

    @Test
    void shouldRejectInvalidAmount() {
        assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.createTransaction(
                        "user-1",
                        TransactionType.CREDIT_PURCHASE,
                        BigDecimal.ZERO,
                        "USD",
                        "ref-1"
                )
        );

        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void shouldRejectNullTransactionType() {
        assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.createTransaction(
                        "user-1",
                        null,
                        BigDecimal.TEN,
                        "USD",
                        "ref-1"
                )
        );

        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void shouldRejectEmptyCurrency() {
        assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.createTransaction(
                        "user-1",
                        TransactionType.CREDIT_PURCHASE,
                        BigDecimal.TEN,
                        "",
                        "ref-1"
                )
        );

        verify(transactionRepository, never()).save(any(Transaction.class));
    }
}