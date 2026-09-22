package dev.ramtin.aiplatform.credit;

import dev.ramtin.aiplatform.user.CreditSource;

import java.math.BigDecimal;

public interface CreditSourceHandler {
    CreditSource getCreditSource();

    void consume(String userId, BigDecimal amount);
}