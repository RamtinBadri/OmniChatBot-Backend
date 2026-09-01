package dev.ramtin.aiplatform.user.service;

import dev.ramtin.aiplatform.user.dto.UserDTO;

public interface UserService {
    UserDTO getUserById(String userId);

    // TODO: UserDTO createUser(CreateUserRequest request);

    // TODO: UserDTO updateUser(String userId, UpdateUserRequest request);
}