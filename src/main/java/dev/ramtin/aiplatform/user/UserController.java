package dev.ramtin.aiplatform.user;

import dev.ramtin.aiplatform.user.dto.UserDTO;
import dev.ramtin.aiplatform.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
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
            @RequestParam(required = false) String status,
            Pageable pageable
    ) {
        Page<UserDTO> users = userService.getUsers(search, role, status, pageable);
        return ResponseEntity.ok(users);
    }
}