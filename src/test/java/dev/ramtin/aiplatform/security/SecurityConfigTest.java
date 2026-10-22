package dev.ramtin.aiplatform.security;

import dev.ramtin.aiplatform.user.User;
import dev.ramtin.aiplatform.user.repository.UserRepository;
import dev.ramtin.aiplatform.user.UserRole;
import dev.ramtin.aiplatform.user.service.UserService;
import dev.ramtin.aiplatform.model_pricing.service.ModelPricingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
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

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private ModelPricingService modelPricingService;

    @ParameterizedTest
    @CsvSource({"PUT,/api/users/target123/role", "PATCH,/api/admin/users/target123/role"})
    void roleAliases_withoutToken_returnUnauthorized(String method, String path) throws Exception {
        mockMvc.perform(request(HttpMethod.valueOf(method), path)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"role\":\"ADMIN\"}"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(userService);
    }

    @ParameterizedTest
    @CsvSource({"PUT,/api/users/target123/role", "PATCH,/api/admin/users/target123/role"})
    void roleAliases_withUserToken_returnForbidden(String method, String path) throws Exception {
        User user = User.builder().id("user123").username("ramtin").password("password").userRole(UserRole.USER).build();
        when(userRepository.findByUsername("ramtin")).thenReturn(Optional.of(user));

        mockMvc.perform(request(HttpMethod.valueOf(method), path)
                        .header("Authorization", "Bearer " + jwtTokenProvider.generateToken("ramtin"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"role\":\"ADMIN\"}"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

    @ParameterizedTest
    @CsvSource({"PUT,/api/users/target123/role", "PATCH,/api/admin/users/target123/role"})
    void roleAliases_withAdminToken_delegateToMockedService(String method, String path) throws Exception {
        User admin = User.builder().id("admin123").username("admin").password("password").userRole(UserRole.ADMIN).build();
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));

        mockMvc.perform(request(HttpMethod.valueOf(method), path)
                        .header("Authorization", "Bearer " + jwtTokenProvider.generateToken("admin"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"role\":\"ADMIN\"}"))
                .andExpect(status().isNoContent());

        verify(userService).updateUserRole("target123", UserRole.ADMIN);
    }

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
    void modelPricingEndpoint_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/admin/model-pricing")).andExpect(status().isUnauthorized());
        verifyNoInteractions(modelPricingService);
    }

    @Test
    void modelPricingEndpoint_withUserToken_returnsForbidden() throws Exception {
        User user = User.builder().id("user123").username("ramtin").password("password").userRole(UserRole.USER).build();
        when(userRepository.findByUsername("ramtin")).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/admin/model-pricing")
                        .header("Authorization", "Bearer " + jwtTokenProvider.generateToken("ramtin")))
                .andExpect(status().isForbidden());
        verifyNoInteractions(modelPricingService);
    }

    @Test
    void modelPricingEndpoint_withAdminToken_delegatesToService() throws Exception {
        User admin = User.builder().id("admin123").username("admin").password("password").userRole(UserRole.ADMIN).build();
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(modelPricingService.getDefaultInputPricePerMillion()).thenReturn(java.math.BigDecimal.ONE);
        when(modelPricingService.getDefaultOutputPricePerMillion()).thenReturn(java.math.BigDecimal.TEN);
        when(modelPricingService.getAllPricing()).thenReturn(java.util.List.of());

        mockMvc.perform(get("/api/admin/model-pricing")
                        .header("Authorization", "Bearer " + jwtTokenProvider.generateToken("admin")))
                .andExpect(status().isOk());
        verify(modelPricingService).getAllPricing();
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