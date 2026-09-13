package dev.ramtin.aiplatform.security;

import dev.ramtin.aiplatform.user.User;
import dev.ramtin.aiplatform.user.UserRepository;
import dev.ramtin.aiplatform.user.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    @Test
    void loadUserByUsername_whenUserExists_returnsUserDetails() {
        User user = User.builder().id("user123").username("ramtin").password("hashedPassword").userRole(UserRole.USER).build();

        when(userRepository.findByUsername("ramtin")).thenReturn(Optional.of(user));

        UserDetails result = userDetailsService.loadUserByUsername("ramtin");

        assertEquals("ramtin", result.getUsername());
        assertEquals("hashedPassword", result.getPassword());
        assertEquals("ROLE_USER", result.getAuthorities().iterator().next().getAuthority());

        CustomUserDetails customUserDetails = (CustomUserDetails) result;

        assertEquals("user123", customUserDetails.getId());

        verify(userRepository).findByUsername("ramtin");
    }

    @Test
    void loadUserByUsername_whenUserDoesNotExist_throwsUsernameNotFoundException() {
        when(userRepository.findByUsername("ramtin")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername("ramtin"));

        verify(userRepository).findByUsername("ramtin");
    }
}