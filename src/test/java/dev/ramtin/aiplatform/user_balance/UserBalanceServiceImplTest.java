package dev.ramtin.aiplatform.user_balance;

import dev.ramtin.aiplatform.exception.ResourceNotFoundException;
import dev.ramtin.aiplatform.transaction.Transaction;
import dev.ramtin.aiplatform.transaction.TransactionStatus;
import dev.ramtin.aiplatform.transaction.TransactionType;
import dev.ramtin.aiplatform.transaction.service.TransactionService;
import dev.ramtin.aiplatform.user_balance.service.UserBalanceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserBalanceServiceImplTest {
    @Mock
    private UserBalanceRepository userBalanceRepository;
    @Mock
    private TransactionService transactionService;

    private UserBalanceServiceImpl userBalanceService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userBalanceService = new UserBalanceServiceImpl(userBalanceRepository, transactionService);
    }

    @Test
    void shouldCreateBalanceSuccessfully() {
        when(userBalanceRepository.save(any(UserBalance.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserBalance result = userBalanceService.createBalance("user-1");

        assertNotNull(result);
        assertEquals("user-1", result.getUserId());
        assertEquals(
                BigDecimal.ZERO,
                result.getBalance()
        );
        assertEquals("USD", result.getCurrency());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());

        verify(userBalanceRepository).save(any(UserBalance.class));
    }

    @Test
    void shouldGetBalanceSuccessfully() {
        UserBalance balance = UserBalance.builder()
                .id("balance-1")
                .userId("user-1")
                .balance(BigDecimal.TEN)
                .currency("USD")
                .build();

        when(userBalanceRepository.findByUserId("user-1")).thenReturn(Optional.of(balance));

        UserBalance result = userBalanceService.getBalanceByUserId("user-1");

        assertEquals("balance-1", result.getId());
        assertEquals("user-1", result.getUserId());
        assertEquals(
                BigDecimal.TEN,
                result.getBalance()
        );
    }

    @Test
    void shouldThrowExceptionWhenBalanceDoesNotExist() {
        when(userBalanceRepository.findByUserId("user-1")).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userBalanceService
                        .getBalanceByUserId("user-1")
        );
    }

    @Test
    void shouldIncreaseBalanceSuccessfully() {
        UserBalance balance = UserBalance.builder()
                .id("balance-1")
                .userId("user-1")
                .balance(BigDecimal.TEN)
                .currency("USD")
                .build();

        when(userBalanceRepository.findByUserId("user-1")).thenReturn(Optional.of(balance));

        when(userBalanceRepository.save(balance)).thenReturn(balance);

        userBalanceService.increaseBalance(
                "user-1",
                BigDecimal.valueOf(5)
        );

        assertEquals(
                BigDecimal.valueOf(15),
                balance.getBalance()
        );

        verify(userBalanceRepository)
                .save(balance);
    }

    @Test
    void shouldDecreaseBalanceSuccessfully() {
        UserBalance balance = UserBalance.builder()
                .id("balance-1")
                .userId("user-1")
                .balance(BigDecimal.TEN)
                .currency("USD")
                .build();

        when(userBalanceRepository.findByUserId("user-1"))
                .thenReturn(Optional.of(balance));

        when(userBalanceRepository.save(balance))
                .thenReturn(balance);

        userBalanceService.decreaseBalance(
                "user-1",
                BigDecimal.valueOf(4)
        );

        assertEquals(
                BigDecimal.valueOf(6),
                balance.getBalance()
        );

        verify(userBalanceRepository)
                .save(balance);
    }

    @Test
    void shouldRejectDecreaseWhenBalanceIsInsufficient() {
        UserBalance balance = UserBalance.builder()
                .id("balance-1")
                .userId("user-1")
                .balance(BigDecimal.TEN)
                .currency("USD")
                .build();

        when(userBalanceRepository.findByUserId("user-1"))
                .thenReturn(Optional.of(balance));

        assertThrows(
                IllegalStateException.class,
                () -> userBalanceService.decreaseBalance(
                        "user-1",
                        BigDecimal.valueOf(15)
                )
        );

        verify(userBalanceRepository, never())
                .save(any(UserBalance.class));
    }

    @Test
    void shouldRejectInvalidAmountWhenIncreasingBalance() {
        assertThrows(
                IllegalArgumentException.class,
                () -> userBalanceService.increaseBalance(
                        "user-1",
                        BigDecimal.ZERO
                )
        );

        verify(userBalanceRepository, never())
                .findByUserId(anyString());
    }

    @Test
    void shouldRejectInvalidAmountWhenDecreasingBalance() {
        assertThrows(
                IllegalArgumentException.class,
                () -> userBalanceService.decreaseBalance(
                        "user-1",
                        BigDecimal.valueOf(-5)
                )
        );

        verify(userBalanceRepository, never())
                .findByUserId(anyString());
    }

    @Test
    void shouldPurchaseCreditSuccessfully() {
        Transaction pendingTransaction = Transaction.builder()
                .id("transaction-1")
                .userId("user-1")
                .type(TransactionType.CREDIT_PURCHASE)
                .amount(BigDecimal.TEN)
                .currency("USD")
                .status(TransactionStatus.PENDING)
                .reference("credit-ref-1")
                .build();

        Transaction successfulTransaction = Transaction.builder()
                .id("transaction-1")
                .userId("user-1")
                .type(TransactionType.CREDIT_PURCHASE)
                .amount(BigDecimal.TEN)
                .currency("USD")
                .status(TransactionStatus.SUCCESS)
                .reference("credit-ref-1")
                .build();

        when(transactionService.createTransaction(
                "user-1",
                TransactionType.CREDIT_PURCHASE,
                BigDecimal.TEN,
                "USD",
                "credit-ref-1"
        )).thenReturn(pendingTransaction);

        when(transactionService.updateStatus(
                "transaction-1",
                TransactionStatus.SUCCESS
        )).thenReturn(successfulTransaction);

        UserBalance balance = UserBalance.builder()
                .id("balance-1")
                .userId("user-1")
                .balance(BigDecimal.valueOf(5))
                .currency("USD")
                .build();

        when(userBalanceRepository.findByUserId("user-1"))
                .thenReturn(Optional.of(balance));

        when(userBalanceRepository.save(balance))
                .thenReturn(balance);

        Transaction result =
                userBalanceService.purchaseCredit(
                        "user-1",
                        BigDecimal.TEN,
                        "USD",
                        "credit-ref-1"
                );

        assertNotNull(result);

        assertEquals(
                TransactionStatus.SUCCESS,
                result.getStatus()
        );

        assertEquals(
                BigDecimal.valueOf(15),
                balance.getBalance()
        );

        verify(transactionService).createTransaction(
                "user-1",
                TransactionType.CREDIT_PURCHASE,
                BigDecimal.TEN,
                "USD",
                "credit-ref-1"
        );

        verify(transactionService).updateStatus(
                "transaction-1",
                TransactionStatus.SUCCESS
        );

        verify(userBalanceRepository).save(balance);
    }
}