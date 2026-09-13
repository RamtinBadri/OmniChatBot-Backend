package dev.ramtin.aiplatform.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private CustomUserDetailsService userDetailsService;
    @Mock
    private FilterChain filterChain;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilter_whenValidToken_authenticatesUser() throws ServletException, IOException {
        String token = "valid-token";

        CustomUserDetails userDetails = new CustomUserDetails("user123", "ramtin", "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER")));

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        when(jwtTokenProvider.validateToken(token)).thenReturn(true);

        when(jwtTokenProvider.getUsernameFromToken(token)).thenReturn("ramtin");

        when(userDetailsService.loadUserByUsername("ramtin")).thenReturn(userDetails);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());

        assertEquals("ramtin", SecurityContextHolder.getContext()
                .getAuthentication().getName());

        assertEquals("ROLE_USER", SecurityContextHolder.getContext()
                .getAuthentication()
                .getAuthorities()
                .iterator()
                .next()
                .getAuthority());

        verify(jwtTokenProvider).validateToken(token);
        verify(jwtTokenProvider).getUsernameFromToken(token);
        verify(userDetailsService).loadUserByUsername("ramtin");

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_whenTokenIsMissing_returnsUnauthorized() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        when(request.getRequestURI()).thenReturn("/api/users/me");

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        verifyNoInteractions(jwtTokenProvider);
        verifyNoInteractions(userDetailsService);
        verifyNoInteractions(filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilter_whenTokenIsInvalid_doesNotAuthenticateUser() throws ServletException, IOException {
        String token = "invalid-token";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtTokenProvider.validateToken(token)).thenReturn(false);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verify(jwtTokenProvider).validateToken(token);
        verify(jwtTokenProvider, never()).getUsernameFromToken(anyString());
        verifyNoInteractions(userDetailsService);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_whenTokenIsMissingForLoginEndpoint_continuesFilterChain() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/api/auth/login");

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);

        verifyNoInteractions(jwtTokenProvider);
        verifyNoInteractions(userDetailsService);
        verify(response, never()).setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilter_whenTokenIsMissingForRegisterEndpoint_continuesFilterChain()
            throws ServletException, IOException {

        when(request.getHeader("Authorization"))
                .thenReturn(null);

        when(request.getRequestURI())
                .thenReturn("/api/auth/register");

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);

        verifyNoInteractions(jwtTokenProvider);
        verifyNoInteractions(userDetailsService);
        verify(response, never()).setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}