package com.lirium.nutrition.repository;

import com.lirium.nutrition.infrastructure.config.JpaConfig;
import com.lirium.nutrition.model.entity.RefreshToken;
import com.lirium.nutrition.model.entity.User;
import com.lirium.nutrition.model.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(JpaConfig.class)
class RefreshTokenRepositoryTest {

    @Autowired
    private RefreshTokenRepository repository;

    @Autowired
    private TestEntityManager em;

    @Test
    void shouldFindByUser() {

        User user = createUser("user1@test.com");

        RefreshToken expected = createRefreshToken(
                user,
                "token-1"
        );

        em.flush();
        em.clear();

        Optional<RefreshToken> result =
                repository.findByUser(user);

        assertThat(result)
                .contains(expected);
    }

    @Test
    void shouldFindByToken() {

        User user = createUser("user2@test.com");

        RefreshToken expected = createRefreshToken(
                user,
                "token-2"
        );

        em.flush();
        em.clear();

        Optional<RefreshToken> result =
                repository.findByToken("token-2");

        assertThat(result)
                .contains(expected);
    }

    @Test
    void shouldReturnEmptyWhenUserDoesNotHaveRefreshToken() {

        User user = createUser("user3@test.com");

        em.flush();
        em.clear();

        Optional<RefreshToken> result =
                repository.findByUser(user);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenTokenDoesNotExist() {

        Optional<RefreshToken> result =
                repository.findByToken("unknown-token");

        assertThat(result).isEmpty();
    }

    private User createUser(String email) {

        User user = new User(
                email,
                "123456",
                "Juan",
                "Perez",
                Role.PATIENT
        );

        em.persist(user);

        return user;
    }

    private RefreshToken createRefreshToken(
            User user,
            String token
    ) {

        RefreshToken refreshToken = new RefreshToken(
                user,
                token,
                Instant.now().plus(Duration.ofDays(30))
        );

        em.persist(refreshToken);

        return refreshToken;
    }
}