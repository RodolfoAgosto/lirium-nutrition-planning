package com.lirium.nutrition;

import com.lirium.nutrition.infrastructure.security.JwtService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JwtServiceTest {

    @Value("${app.jwt.secret}")
    private String secret;

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