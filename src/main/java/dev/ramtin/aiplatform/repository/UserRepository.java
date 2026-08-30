package dev.ramtin.aiplatform.repository;

import dev.ramtin.aiplatform.model.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository
        extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
}