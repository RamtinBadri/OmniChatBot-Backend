package dev.ramtin.aiplatform.user.service;

import dev.ramtin.aiplatform.user.dto.UserDTO;

public interface UserService {
    UserDTO getUserById(String userId);
}