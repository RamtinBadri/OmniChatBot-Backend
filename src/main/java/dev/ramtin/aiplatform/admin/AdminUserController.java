package dev.ramtin.aiplatform.admin;

import dev.ramtin.aiplatform.admin.dto.UpdateUserRoleRequest;
import dev.ramtin.aiplatform.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {
    private final UserService userService;

    @PatchMapping("/{userId}/role")
    public ResponseEntity<Void> updateUserRole(@PathVariable String userId,
            @Valid @RequestBody UpdateUserRoleRequest request) {
        userService.updateUserRole(userId, request.getRole());
        return ResponseEntity.noContent().build();
    }
}