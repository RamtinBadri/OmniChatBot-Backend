package dev.ramtin.aiplatform.user;

import dev.ramtin.aiplatform.security.CustomUserDetails;
import dev.ramtin.aiplatform.user.dto.UpdateCreditSourceRequest;
import dev.ramtin.aiplatform.user.dto.UpdateUserRequest;
import dev.ramtin.aiplatform.user.dto.UserDTO;
import dev.ramtin.aiplatform.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/{userId}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable String userId) {
        UserDTO userDTO = userService.getUserById(userId);
        return ResponseEntity.ok(userDTO);
    }

    @GetMapping
    public ResponseEntity<Page<UserDTO>> getUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) UserStatus status,
            Pageable pageable
    ) {
        Page<UserDTO> users = userService.getUsers(search, role, status, pageable);
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable String userId,
                                              @Valid @RequestBody UpdateUserRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User is not authenticated");
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof CustomUserDetails userDetails)) {
            throw new AccessDeniedException("Invalid authenticated user");
        }

        if (userDetails.getId() == null) {
            throw new AccessDeniedException("Authenticated user has no ID");
        }

        if (!userDetails.getId().equals(userId)) {
            throw new AccessDeniedException("You can only update your own profile");
        }

        UserDTO userDTO = userService.updateUser(userId, request);
        return ResponseEntity.ok(userDTO);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        String userId = userDetails.getId();
        UserDTO userDTO = userService.getUserById(userId);
        return ResponseEntity.ok(userDTO);
    }

    @PutMapping("/{userId}/credit-source")
    public ResponseEntity<Void> updateCreditSource(@PathVariable String userId, @RequestBody UpdateCreditSourceRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User is not authenticated");
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof CustomUserDetails userDetails)) {
            throw new AccessDeniedException("Invalid authenticated user");
        }

        if (userDetails.getId() == null) {
            throw new AccessDeniedException("Authenticated user has no ID");
        }

        if (!userDetails.getId().equals(userId)) {
            throw new AccessDeniedException("You can only update your own credit source");
        }

        userService.updateCreditSource(userId, request.getCreditSource());
        return ResponseEntity.noContent().build();
    }
}