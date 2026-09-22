package dev.ramtin.aiplatform.credit;

import dev.ramtin.aiplatform.subscription.service.SubscriptionService;
import dev.ramtin.aiplatform.user.CreditSource;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class SubscriptionCreditHandler implements CreditSourceHandler {
    private final SubscriptionService subscriptionService;

    public SubscriptionCreditHandler(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @Override
    public CreditSource getCreditSource() {
        return CreditSource.SUBSCRIPTION;
    }

    @Override
    public void consume(String userId, BigDecimal amount) {
        subscriptionService.consumeCredit(userId, amount);
    }
}