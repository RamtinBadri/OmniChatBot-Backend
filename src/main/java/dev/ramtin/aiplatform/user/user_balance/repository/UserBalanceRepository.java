package dev.ramtin.aiplatform.user.user_balance.repository;

import dev.ramtin.aiplatform.user.user_balance.UserBalance;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserBalanceRepository extends MongoRepository<UserBalance, String> {
    Optional<UserBalance> findByUserId(String userId);
}