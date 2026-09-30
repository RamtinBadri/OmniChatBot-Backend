package dev.ramtin.aiplatform.user.service;

import dev.ramtin.aiplatform.credit.CreditSourceHandler;
import dev.ramtin.aiplatform.user.CreditSource;
import dev.ramtin.aiplatform.user.UserRole;
import dev.ramtin.aiplatform.user.UserStatus;
import dev.ramtin.aiplatform.user.dto.UpdateUserRequest;
import dev.ramtin.aiplatform.user.dto.UserDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface UserService {
    UserDTO getUserById(String userId);

    Page<UserDTO> getUsers(String search, String role, UserStatus status, Pageable pageable);

    UserDTO updateUser(String userId, UpdateUserRequest request);

    void updateUserRole(String userId, UserRole role);

    void updateCreditSource(String userId, CreditSource creditSource);

    void deleteUser(String userId);
}