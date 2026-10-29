package com.foodflow.service;

import com.foodflow.service.impl.JwtServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class JwtServiceTest {

    private JwtServiceImpl jwtService;
    
    // Minimum 256-bit signing key for HMAC-SHA256
    private final String testSecret = "9a763845fc1a8f3b145d6f8e71a4f3d2e1b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8";

    @BeforeEach
    public void setUp() {
        jwtService = new JwtServiceImpl();
        ReflectionTestUtils.setField(jwtService, "secretKey", testSecret);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 3600000L); // 1 hour duration
    }

    @Test
    public void testGenerateAndValidateToken() {
        UserDetails userDetails = new User("test@foodflow.com", "password", List.of());

        String token = jwtService.generateToken(userDetails);
        assertNotNull(token);

        String email = jwtService.extractEmail(token);
        assertEquals("test@foodflow.com", email);

        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    public void testInvalidUserToken() {
        UserDetails userDetails = new User("test@foodflow.com", "password", List.of());
        UserDetails otherDetails = new User("other@foodflow.com", "password", List.of());

        String token = jwtService.generateToken(userDetails);

        assertFalse(jwtService.isTokenValid(token, otherDetails));
    }

    @Test
    public void testTamperedTokenRejection() {
        UserDetails userDetails = new User("test@foodflow.com", "password", List.of());
        String token = jwtService.generateToken(userDetails);

        String tamperedToken = token + "tampered";

        assertThrows(Exception.class, () -> jwtService.extractEmail(tamperedToken));
    }
}
