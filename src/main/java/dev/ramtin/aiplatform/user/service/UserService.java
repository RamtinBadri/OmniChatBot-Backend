package dev.ramtin.aiplatform.user.service;

import dev.ramtin.aiplatform.user.dto.UserDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    UserDTO getUserById(String userId);

    Page<UserDTO> getUsers(String search, String role, String status, Pageable pageable);

    // TODO: UserDTO createUser(CreateUserRequest request);

    // TODO: UserDTO updateUser(String userId, UpdateUserRequest request);
}