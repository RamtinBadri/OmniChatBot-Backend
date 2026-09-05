package dev.ramtin.aiplatform.auth;

import dev.ramtin.aiplatform.user.dto.LoginRequest;
import dev.ramtin.aiplatform.user.dto.RegisterRequest;

public interface AuthService {
    void register(RegisterRequest request);
    String login(LoginRequest request);
}