package dev.ramtin.aiplatform.user.dto;

import dev.ramtin.aiplatform.user.UserRole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUserRoleRequest {
    @NotNull
    private UserRole role;
}