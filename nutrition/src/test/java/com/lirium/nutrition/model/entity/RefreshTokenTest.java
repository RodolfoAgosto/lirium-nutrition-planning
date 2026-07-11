package com.lirium.nutrition.model.entity;

import com.lirium.nutrition.model.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class RefreshTokenTest {

    User user;

    @BeforeEach
    void setup(){
        user = createUser();
    }

    @Test
    void shouldCreateRefreshToken() {



        Instant expiration =
                Instant.now().plusSeconds(3600);

        RefreshToken refreshToken =
                new RefreshToken(
                        user,
                        "token123",
                        expiration
                );


        assertThat(refreshToken.getUser())
                .isEqualTo(user);

        assertThat(refreshToken.getToken())
                .isEqualTo("token123");

        assertThat(refreshToken.getExpiresAt())
                .isEqualTo(expiration);

        assertThat(refreshToken.isRevoked())
                .isFalse();
    }


    @Test
    void shouldReturnFalseWhenTokenIsNotExpired() {

        RefreshToken refreshToken =
                new RefreshToken(
                        createUser(),
                        "token123",
                        Instant.now().plusSeconds(3600)
                );


        assertThat(refreshToken.isExpired())
                .isFalse();
    }


    @Test
    void shouldReturnTrueWhenTokenIsExpired() {

        RefreshToken refreshToken =
                new RefreshToken(
                        createUser(),
                        "token123",
                        Instant.now().minusSeconds(3600)
                );


        assertThat(refreshToken.isExpired())
                .isTrue();
    }


    @Test
    void shouldRevokeToken() {

        RefreshToken refreshToken =
                new RefreshToken(
                        createUser(),
                        "token123",
                        Instant.now().plusSeconds(3600)
                );


        refreshToken.revoke();


        assertThat(refreshToken.isRevoked())
                .isTrue();
    }


    private User createUser() {

        return new User(
                "test@test.com",
                "password",
                "John",
                "Doe",
                Role.PATIENT
        );
    }

    @Test
    void shouldDetectExpiredToken() {

        RefreshToken token =
                new RefreshToken(
                        user,
                        "abc",
                        Instant.now().minusSeconds(1)
                );

        assertThat(token.isExpired())
                .isTrue();
    }

    @Test
    void shouldRemainRevoked() {

        RefreshToken token =
                new RefreshToken(
                        user,
                        "abc",
                        Instant.now().plusSeconds(3600)
                );

        token.revoke();
        token.revoke();

        assertThat(token.isRevoked())
                .isTrue();
    }


}