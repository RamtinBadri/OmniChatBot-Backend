package dev.ramtin.aiplatform.auth;

import dev.ramtin.aiplatform.exception.ResourceConflictException;
import dev.ramtin.aiplatform.user.User;
import dev.ramtin.aiplatform.user.UserRepository;
import dev.ramtin.aiplatform.user.dto.LoginRequest;
import dev.ramtin.aiplatform.user.dto.RegisterRequest;
import dev.ramtin.aiplatform.user.UserRole;
import dev.ramtin.aiplatform.user.UserStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private org.springframework.security.authentication.AuthenticationManager authenticationManager;

    @Mock
    private dev.ramtin.aiplatform.security.JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void register_whenRequestIsValid_createsUser() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("ramtin");
        request.setEmail("ramtin@example.com");
        request.setPassword("password123");
        request.setFirstName("Ramtin");
        request.setLastName("Badri");

        when(userRepository.findByUsername("ramtin")).thenReturn(Optional.empty());

        when(userRepository.findByEmail("ramtin@example.com")).thenReturn(Optional.empty());

        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");

        authService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertNotNull(savedUser);
        assertEquals("ramtin", savedUser.getUsername());
        assertEquals("ramtin@example.com", savedUser.getEmail());
        assertEquals("hashedPassword", savedUser.getPassword());
        assertEquals("Ramtin", savedUser.getFirstName());
        assertEquals("Badri", savedUser.getLastName());
        assertEquals(UserRole.USER,
                savedUser.getUserRole());
        assertEquals(UserStatus.ACTIVE,
                savedUser.getStatus());
        assertNotNull(savedUser.getCreatedAt());
        assertNotNull(savedUser.getUpdatedAt());
    }

    @Test
    void register_whenUsernameAlreadyExists_throwsConflictException() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("ramtin");
        request.setEmail("ramtin@example.com");
        request.setPassword("password123");
        request.setFirstName("Ramtin");
        request.setLastName("Badri");

        User existingUser = User.builder().id("user123").username("ramtin").build();

        when(userRepository.findByUsername("ramtin")).thenReturn(Optional.of(existingUser));

        assertThrows(ResourceConflictException.class, () -> authService.register(request));

        verify(userRepository).findByUsername("ramtin");
    }

    @Test
    void register_whenEmailAlreadyExists_throwsConflictException() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("ramtin");
        request.setEmail("ramtin@example.com");
        request.setPassword("password123");
        request.setFirstName("Ramtin");
        request.setLastName("Badri");

        User existingUser = User.builder().id("user123").email("ramtin@example.com").build();

        when(userRepository.findByUsername("ramtin")).thenReturn(Optional.empty());

        when(userRepository.findByEmail("ramtin@example.com")).thenReturn(Optional.of(existingUser));

        assertThrows(ResourceConflictException.class, () -> authService.register(request));

        verify(userRepository).findByUsername("ramtin");
        verify(userRepository).findByEmail("ramtin@example.com");
    }

    @Test
    void login_whenCredentialsAreValid_returnsJwt() {
        LoginRequest request = new LoginRequest();
        request.setUsername("ramtin");
        request.setPassword("password123");

        String expectedToken = "jwt-token";

        when(jwtTokenProvider.generateToken("ramtin")).thenReturn(expectedToken);

        String result = authService.login(request);

        assertEquals(expectedToken, result);

        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken("ramtin", "password123"));

        verify(jwtTokenProvider).generateToken("ramtin");
    }

    @Test
    void login_whenAuthenticationFails_throwsException() {
        LoginRequest request = new LoginRequest();
        request.setUsername("ramtin");
        request.setPassword("wrongPassword");

        when(authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken("ramtin", "wrongPassword")))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> authService.login(request));

        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken("ramtin", "wrongPassword"));
    }
}