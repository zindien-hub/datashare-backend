package com.datashare.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        ReflectionTestUtils.setField(
                jwtService,
                "secret",
                "this-is-a-very-long-test-secret-key-for-jwt-signing-123456"
        );
        ReflectionTestUtils.setField(jwtService, "expirationMs", 3600000L);

        jwtService.init();
    }

    @Test
    void shouldGenerateToken() {
        String token = jwtService.generateToken("test@datashare.com");

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void shouldExtractUsernameFromToken() {
        String token = jwtService.generateToken("test@datashare.com");

        String username = jwtService.extractUsername(token);

        assertEquals("test@datashare.com", username);
    }

    @Test
    void shouldValidateGeneratedToken() {
        String token = jwtService.generateToken("test@datashare.com");

        boolean valid = jwtService.isTokenValid(token);

        assertTrue(valid);
    }

    @Test
    void shouldThrowExceptionForExpiredToken() throws InterruptedException {
        JwtService shortLivedJwtService = new JwtService();

        ReflectionTestUtils.setField(
                shortLivedJwtService,
                "secret",
                "this-is-a-very-long-test-secret-key-for-jwt-signing-123456"
        );
        ReflectionTestUtils.setField(shortLivedJwtService, "expirationMs", 1L);

        shortLivedJwtService.init();

        String token = shortLivedJwtService.generateToken("test@datashare.com");

        Thread.sleep(10);

        assertThrows(io.jsonwebtoken.ExpiredJwtException.class, () ->
                shortLivedJwtService.isTokenValid(token)
        );
    }
}