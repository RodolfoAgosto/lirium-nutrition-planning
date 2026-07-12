package com.lirium.nutrition.repository;

import com.lirium.nutrition.infrastructure.config.JpaConfig;
import com.lirium.nutrition.model.entity.User;
import com.lirium.nutrition.model.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaConfig.class)
class UserRepositoryIT {

    @Autowired
    private UserRepository repository;

    @Autowired
    private TestEntityManager em;

    @Test
    void shouldFindUserByEmail() {

        User expected = createUser(
                "user1@test.com",
                "11111111"
        );

        em.flush();
        em.clear();

        User found = repository.findByEmail("user1@test.com")
                .orElseThrow();

        assertThat(found.getId()).isEqualTo(expected.getId());
        assertThat(found.getEmail()).isEqualTo(expected.getEmail());
    }

    @Test
    void shouldFindUserByDni() {

        User expected = createUser(
                "user2@test.com",
                "22222222"
        );

        em.flush();
        em.clear();

        User found = repository.findByDni("22222222")
                .orElseThrow();

        assertThat(found.getId()).isEqualTo(expected.getId());
        assertThat(found.getDni()).isEqualTo(expected.getDni());
    }

    @Test
    void shouldFindEnabledUserByEmail() {

        User expected = createUser(
                "user3@test.com",
                "33333333"
        );

        em.flush();
        em.clear();

        User found = repository.findByEmailAndEnabledTrue(expected.getEmail())
                .orElseThrow();

        assertThat(found.getId()).isEqualTo(expected.getId());

    }

    @Test
    void shouldReturnEmptyWhenEnabledUserDoesNotExist() {

        Optional<User> result =
                repository.findByEmailAndEnabledTrue(
                        "unknown@test.com"
                );

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnTrueWhenEmailExists() {

        createUser(
                "user4@test.com",
                "44444444"
        );

        em.flush();
        em.clear();

        assertThat(
                repository.existsByEmail("user4@test.com")
        ).isTrue();
    }

    @Test
    void shouldReturnFalseWhenEmailDoesNotExist() {

        assertThat(
                repository.existsByEmail("missing@test.com")
        ).isFalse();
    }

    private User createUser(
            String email,
            String dni
    ) {

        User user = new User(
                email,
                "123456",
                "Juan",
                "Perez",
                Role.PATIENT
        );

        user.setDni(dni);

        em.persist(user);

        return user;
    }

}