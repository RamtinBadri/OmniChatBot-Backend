package dev.ramtin.aiplatform.user_balance;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserBalanceRepository extends MongoRepository<UserBalance, String> {
    Optional<UserBalance> findByUserId(String userId);
}