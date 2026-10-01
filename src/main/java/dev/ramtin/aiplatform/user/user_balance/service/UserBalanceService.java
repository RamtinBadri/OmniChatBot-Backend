package dev.ramtin.aiplatform.user.user_balance.service;

import dev.ramtin.aiplatform.transaction.Transaction;
import dev.ramtin.aiplatform.user.user_balance.UserBalance;

import java.math.BigDecimal;

public interface UserBalanceService {
    UserBalance createBalance(String userId);

    UserBalance getBalanceByUserId(String userId);

    void increaseBalance(String userId, BigDecimal amount);

    void decreaseBalance(String userId, BigDecimal amount);

    Transaction purchaseCredit(String userId, BigDecimal amount, String currency, String reference);
}