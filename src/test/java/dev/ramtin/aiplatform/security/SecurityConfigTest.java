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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
    void loginPreflight_fromFrontendOrigin_isAllowed() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "authorization,content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(header().string("Access-Control-Allow-Methods", org.hamcrest.Matchers.containsString("POST")))
                .andExpect(header().string("Access-Control-Allow-Headers", org.hamcrest.Matchers.allOf(
                        org.hamcrest.Matchers.containsString("authorization"),
                        org.hamcrest.Matchers.containsString("content-type"))));
    }

    @Test
    void loginPreflight_fromOtherOrigin_isRejected() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "content-type"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

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