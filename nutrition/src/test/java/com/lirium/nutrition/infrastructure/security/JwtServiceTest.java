package com.lirium.nutrition.infrastructure.security;

import static org.junit.jupiter.api.Assertions.*;

import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    private UserDetails userDetails;

    @BeforeEach
    void setUp() {

        jwtService = new JwtService();

        ReflectionTestUtils.setField(
                jwtService,
                "secret",
                "mySuperSecretKeyThatHasAtLeastThirtyTwoCharacters123"
        );

        ReflectionTestUtils.setField(
                jwtService,
                "expiration",
                3600000L
        );

        userDetails = User.builder()
                .username("test@example.com")
                .password("password")
                .roles("USER")
                .build();
    }

    @Test
    void shouldGenerateToken() {

        String token = jwtService.generateToken(userDetails);

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void shouldExtractUsername() {

        String token = jwtService.generateToken(userDetails);

        String username = jwtService.extractUsername(token);

        assertEquals(
                "test@example.com",
                username
        );
    }

    @Test
    void shouldValidateTokenSuccessfully() {

        String token = jwtService.generateToken(userDetails);

        boolean valid = jwtService.isTokenValid(
                token,
                userDetails
        );

        assertTrue(valid);
    }

    @Test
    void shouldReturnFalseWhenTokenBelongsToAnotherUser() {

        String token = jwtService.generateToken(userDetails);

        UserDetails anotherUser = User.builder()
                .username("another@example.com")
                .password("password")
                .roles("USER")
                .build();

        boolean valid = jwtService.isTokenValid(
                token,
                anotherUser
        );

        assertFalse(valid);
    }

    @Test
    void shouldThrowExceptionWhenTokenIsExpired() {

        String expiredToken =
                jwtService.generateExpiredToken(userDetails);

        assertThrows(
                ExpiredJwtException.class,
                () -> jwtService.isTokenValid(
                        expiredToken,
                        userDetails
                )
        );
    }


    @Test
    void shouldGenerateExpiredToken() {

        String expiredToken =
                jwtService.generateExpiredToken(userDetails);

        assertNotNull(expiredToken);

        assertThrows(
                io.jsonwebtoken.ExpiredJwtException.class,
                () -> jwtService.extractUsername(expiredToken)
        );
    }
}
