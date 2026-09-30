package dev.ramtin.aiplatform.auth;

import dev.ramtin.aiplatform.exception.ResourceConflictException;
import dev.ramtin.aiplatform.security.JwtTokenProvider;
import dev.ramtin.aiplatform.user.User;
import dev.ramtin.aiplatform.user.repository.UserRepository;
import dev.ramtin.aiplatform.user.UserRole;
import dev.ramtin.aiplatform.user.UserStatus;
import dev.ramtin.aiplatform.user.dto.LoginRequest;
import dev.ramtin.aiplatform.user.dto.RegisterRequest;
import dev.ramtin.aiplatform.user_balance.UserBalance;
import dev.ramtin.aiplatform.user_balance.service.UserBalanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserBalanceService userBalanceService;

    @Override
    public void register(RegisterRequest request) {
        if(userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new ResourceConflictException("Username already exists: " + request.getUsername());
        }

        if(userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ResourceConflictException("Email already exists: " + request.getEmail());
        }

        LocalDateTime now = LocalDateTime.now();

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .userRole(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .build();

        User savedUser = userRepository.save(user);

        UserBalance balance = userBalanceService.createBalance(savedUser.getId());

        savedUser.setBalanceId(balance.getId());
        userRepository.save(savedUser);
    }

    @Override
    public String login(LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
        ));
        return jwtTokenProvider.generateToken(request.getUsername());
    }
}