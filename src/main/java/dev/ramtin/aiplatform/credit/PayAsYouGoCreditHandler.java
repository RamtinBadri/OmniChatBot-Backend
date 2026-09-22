package dev.ramtin.aiplatform.credit;

import dev.ramtin.aiplatform.user.CreditSource;
import dev.ramtin.aiplatform.user_balance.service.UserBalanceService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PayAsYouGoCreditHandler implements CreditSourceHandler {
    private final UserBalanceService userBalanceService;

    public PayAsYouGoCreditHandler(UserBalanceService userBalanceService) {
        this.userBalanceService = userBalanceService;
    }

    @Override
    public CreditSource getCreditSource() {
        return CreditSource.PAY_AS_YOU_GO;
    }

    @Override
    public void consume(String userId, BigDecimal amount) {
        userBalanceService.decreaseBalance(userId, amount);
    }
}