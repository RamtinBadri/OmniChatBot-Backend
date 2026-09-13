package dev.ramtin.aiplatform.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {
    private final JwtTokenProvider jwtTokenProvider = new JwtTokenProvider();


    @Test
    void generateToken_returnsToken() {
        String token = jwtTokenProvider.generateToken("ramtin");

        assertNotNull(token);
        assertFalse(token.isBlank());
    }


    @Test
    void getUsernameFromToken_returnsCorrectUsername() {
        String token = jwtTokenProvider.generateToken("ramtin");

        String username = jwtTokenProvider.getUsernameFromToken(token);

        assertEquals("ramtin", username);
    }


    @Test
    void validateToken_whenTokenIsValid_returnsTrue() {
        String token = jwtTokenProvider.generateToken("ramtin");

        boolean result = jwtTokenProvider.validateToken(token);

        assertTrue(result);
    }


    @Test
    void validateToken_whenTokenIsInvalid_returnsFalse() {
        String invalidToken = "this.is.not.a.valid.jwt";

        boolean result = jwtTokenProvider.validateToken(invalidToken);

        assertFalse(result);
    }
}