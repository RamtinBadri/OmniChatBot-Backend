package dev.ramtin.aiplatform.user.user_balance.service;

import dev.ramtin.aiplatform.exception.InsufficientCreditException;
import dev.ramtin.aiplatform.exception.ResourceNotFoundException;
import dev.ramtin.aiplatform.transaction.Transaction;
import dev.ramtin.aiplatform.transaction.TransactionStatus;
import dev.ramtin.aiplatform.transaction.TransactionType;
import dev.ramtin.aiplatform.transaction.service.TransactionService;
import dev.ramtin.aiplatform.user.user_balance.UserBalance;
import dev.ramtin.aiplatform.user.user_balance.repository.UserBalanceRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class UserBalanceServiceImpl implements UserBalanceService {
    private final UserBalanceRepository userBalanceRepository;
    private final TransactionService transactionService;

    public UserBalanceServiceImpl(UserBalanceRepository userBalanceRepository,
                                  TransactionService transactionService) {
        this.userBalanceRepository = userBalanceRepository;
        this.transactionService = transactionService;
    }

    @Override
    public UserBalance createBalance(String userId) {
        LocalDateTime now = LocalDateTime.now();

        UserBalance balance = UserBalance.builder()
                .userId(userId)
                .balance(BigDecimal.ZERO)
                .currency("USD")
                .createdAt(now)
                .updatedAt(now)
                .build();

        return userBalanceRepository.save(balance);
    }

    @Override
    public UserBalance getBalanceByUserId(String userId) {
        return userBalanceRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Balance not found for user: " + userId
                        )
                );
    }

    @Override
    public void increaseBalance(String userId, BigDecimal amount) {
        validateAmount(amount);

        UserBalance balance = getBalanceByUserId(userId);

        balance.setBalance(balance.getBalance().add(amount));

        balance.setUpdatedAt(LocalDateTime.now());

        userBalanceRepository.save(balance);
    }

    @Override
    public void decreaseBalance(String userId, BigDecimal amount) {
        validateAmount(amount);

        UserBalance balance = getBalanceByUserId(userId);

        if (balance.getBalance().compareTo(amount) < 0) {
            throw new InsufficientCreditException("Insufficient balance");
        }

        balance.setBalance(balance.getBalance().subtract(amount));

        balance.setUpdatedAt(LocalDateTime.now());

        userBalanceRepository.save(balance);
    }

    @Override
    public Transaction purchaseCredit(String userId, BigDecimal amount, String currency, String reference) {
        Transaction transaction =
                transactionService.createTransaction(userId, TransactionType.CREDIT_PURCHASE,
                        amount, currency, reference);

        transaction = transactionService.updateStatus(transaction.getId(), TransactionStatus.SUCCESS);

        increaseBalance(userId, amount);
        return transaction;
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
    }
}