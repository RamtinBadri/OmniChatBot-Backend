package dev.ramtin.aiplatform.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserRepositoryCustom {
    Page<User> search(String search, String role, String status, Pageable pageable);
}