package dev.ramtin.aiplatform.user.repository;

import dev.ramtin.aiplatform.user.User;
import dev.ramtin.aiplatform.user.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserRepositoryCustom {
    Page<User> search(String search, String role, UserStatus status, Pageable pageable);
}