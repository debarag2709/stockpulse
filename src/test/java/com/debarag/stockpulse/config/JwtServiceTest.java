
package com.debarag.stockpulse.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private SecretKey signingKey;

    @BeforeEach
    void setUp() {
        byte[] keyBytes = new byte[32];
        new SecureRandom().nextBytes(keyBytes);

        signingKey = Keys.hmacShaKeyFor(keyBytes);

        String secret = Base64.getEncoder()
                .encodeToString(keyBytes);

        jwtService = new JwtService(secret);
    }

    @Test
    void shouldGenerateAndValidateToken() {
        String token = jwtService.generateToken(1L, "test@example.com");

        assertEquals("test@example.com", jwtService.extractEmail(token));
        assertTrue(jwtService.isTokenValid(token, "test@example.com"));
    }

    @Test
    void shouldRejectTokenForDifferentEmail() {
        String token = jwtService.generateToken(1L, "test@example.com");

        assertFalse(jwtService.isTokenValid(token, "other@example.com"));
    }

    @Test
    void shouldRejectExpiredToken() {
        Instant now = Instant.now();

        String expiredToken = Jwts.builder()
                .subject("test@example.com")
                .issuedAt(Date.from(now.minusSeconds(3600)))
                .expiration(Date.from(now.minusSeconds(1800)))
                .signWith(signingKey)
                .compact();

        assertFalse(jwtService.isTokenValid(expiredToken, "test@example.com"));
    }

    @Test
    void shouldRejectTamperedToken() {
        String token = jwtService.generateToken(1L, "test@example.com");
        String tamperedToken = token.substring(0, token.length() - 2) + "xx";

        assertFalse(jwtService.isTokenValid(tamperedToken, "test@example.com"));
    }
}
