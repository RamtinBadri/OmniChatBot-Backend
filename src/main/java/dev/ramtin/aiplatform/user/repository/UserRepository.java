package dev.ramtin.aiplatform.user.repository;

import dev.ramtin.aiplatform.user.User;
import org.jspecify.annotations.NonNull;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String>, UserRepositoryCustom {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(@NonNull String username);
}