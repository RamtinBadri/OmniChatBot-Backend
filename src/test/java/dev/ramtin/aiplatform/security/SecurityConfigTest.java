package dev.ramtin.aiplatform.security;

import dev.ramtin.aiplatform.user.User;
import dev.ramtin.aiplatform.user.repository.UserRepository;
import dev.ramtin.aiplatform.user.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void protectedEndpoint_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/users/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void adminEndpoint_withUserToken_returnsForbidden() throws Exception {
        User user = User.builder().id("user123").username("ramtin").password("password").userRole(UserRole.USER).build();

        when(userRepository.findByUsername("ramtin")).thenReturn(Optional.of(user));

        String token = jwtTokenProvider.generateToken("ramtin");

        mockMvc.perform(get("/api/admin/tickets")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminEndpoint_withAdminToken_isAllowed() throws Exception {
        User admin = User.builder().id("admin123").username("admin").password("password").userRole(UserRole.ADMIN).build();

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));

        String token = jwtTokenProvider.generateToken("admin");

        mockMvc.perform(get("/api/admin/tickets")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}