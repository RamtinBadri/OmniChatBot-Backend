package dev.ramtin.aiplatform.security;

import dev.ramtin.aiplatform.config.CorsProperties;
import dev.ramtin.aiplatform.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class CorsConfigurationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(PropertiesConfiguration.class);

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(CorsProperties.class)
    static class PropertiesConfiguration {
    }

    @Test
    void defaults_allowOnlyLocalFrontendAndPreserveExistingPolicy() {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();
            CorsConfigurationSource source = source(context.getBean(CorsProperties.class));
            CorsConfiguration configuration = source.getCorsConfiguration(
                    new MockHttpServletRequest("OPTIONS", "/api/auth/login"));

            assertThat(configuration).isNotNull();
            assertThat(configuration.getAllowedOrigins()).containsExactly("http://localhost:5173");
            assertThat(configuration.getAllowedMethods()).containsExactly("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
            assertThat(configuration.getAllowedHeaders()).containsExactly("Authorization", "Content-Type");
            assertThat(configuration.getAllowedOriginPatterns()).isNull();
            assertThat(configuration.getAllowCredentials()).isNull();
            assertThat(source.getCorsConfiguration(new MockHttpServletRequest("OPTIONS", "/other"))).isNull();

            MockHttpServletResponse response = preflight(source, "http://localhost:5173", "POST", "authorization,content-type");
            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN)).isEqualTo("http://localhost:5173");
            assertThat(response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS)).isNull();
        });
    }

    @Test
    void configuredOrigins_replaceDefaultAndAllowEachListedOrigin() {
        contextRunner.withPropertyValues("cors.allowed-origins=https://app.example.com,https://admin.example.com")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    CorsProperties properties = context.getBean(CorsProperties.class);
                    assertThat(properties.getAllowedOrigins()).containsExactly("https://app.example.com", "https://admin.example.com");
                    CorsConfigurationSource source = source(properties);
                    for (String origin : properties.getAllowedOrigins()) {
                        MockHttpServletResponse response = preflight(source, origin, "PATCH", "authorization,content-type");
                        assertThat(response.getStatus()).isEqualTo(200);
                        assertThat(response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN)).isEqualTo(origin);
                    }
                    assertRejected(preflight(source, "http://localhost:5173", "POST", "content-type"));
                    assertRejected(preflight(source, "https://unlisted.example.com", "POST", "content-type"));
                });
    }

    @Test
    void emptyOriginList_disablesCrossOriginAccess() {
        contextRunner.withPropertyValues("cors.allowed-origins=").run(context -> {
            assertThat(context).hasNotFailed();
            CorsProperties properties = context.getBean(CorsProperties.class);
            assertThat(properties.getAllowedOrigins()).isEmpty();
            assertRejected(preflight(source(properties), "http://localhost:5173", "POST", "content-type"));
        });
    }

    @Test
    void defaultPolicy_rejectsUnlistedOrigin() throws Exception {
        assertRejected(preflight(source(new CorsProperties()), "http://localhost:3000", "POST", "content-type"));
    }

    @Test
    void defaultPolicy_rejectsUnlistedMethodsAndHeaders() throws Exception {
        CorsConfigurationSource source = source(new CorsProperties());
        assertRejected(preflight(source, "http://localhost:5173", "TRACE", "content-type"));
        assertRejected(preflight(source, "http://localhost:5173", "POST", "x-custom-header"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"*", "https://*.example.com", ""})
    void invalidOrigins_failConfigurationBinding(String origin) {
        contextRunner.withPropertyValues("cors.allowed-origins[0]=" + origin).run(context ->
                assertThat(context).hasFailed());
    }

    private CorsConfigurationSource source(CorsProperties properties) {
        return new SecurityConfig(mock(JwtAuthenticationFilter.class), mock(CustomAuthenticationEntryPoint.class),
                mock(CustomAccessDeniedHandler.class), properties).corsConfigurationSource();
    }

    private MockHttpServletResponse preflight(CorsConfigurationSource source, String origin, String method,
                                              String headers) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/auth/login");
        request.addHeader(HttpHeaders.ORIGIN, origin);
        request.addHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, method);
        request.addHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, headers);
        MockHttpServletResponse response = new MockHttpServletResponse();
        new CorsFilter(source).doFilter(request, response, (ignoredRequest, ignoredResponse) -> {
            throw new AssertionError("Preflight must not reach the application");
        });
        return response;
    }

    private void assertRejected(MockHttpServletResponse response) {
        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN)).isNull();
    }
}
