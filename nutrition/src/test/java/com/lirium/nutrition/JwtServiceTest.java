package com.lirium.nutrition;

import com.lirium.nutrition.infrastructure.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JwtServiceTest {

    @Autowired
    private JwtService jwtService;

    @Test
    void generateAndValidateToken() {
        UserDetails user = new org.springframework.security.core.userdetails.User(
                "test@test.com", "password", List.of()
        );
        String token = jwtService.generateToken(user);
        assertNotNull(token);
        assertEquals("test@test.com", jwtService.extractUsername(token));
        assertTrue(jwtService.isTokenValid(token, user));
    }
}