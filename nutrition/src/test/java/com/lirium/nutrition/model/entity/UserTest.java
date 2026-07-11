package com.lirium.nutrition.model.entity;

import com.lirium.nutrition.model.enums.Role;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTest {


    @Test
    void shouldCreatePatientUserWithProfile() {

        User user =
                new User(
                        "patient@test.com",
                        "hash",
                        "John",
                        "Doe",
                        Role.PATIENT
                );


        assertThat(user.getEmail())
                .isEqualTo("patient@test.com");

        assertThat(user.getPassword())
                .isEqualTo("hash");

        assertThat(user.getRole())
                .isEqualTo(Role.PATIENT);

        assertThat(user.getPatientProfile())
                .isNotNull();

        assertThat(user.getPatientProfile().getUser())
                .isEqualTo(user);
    }


    @Test
    void shouldNotCreatePatientProfileForNonPatientRole() {

        User user =
                new User(
                        "admin@test.com",
                        "hash",
                        "Admin",
                        "User",
                        Role.ADMIN
                );


        assertThat(user.getPatientProfile())
                .isNull();
    }


    @Test
    void shouldReturnEmailAsUsername() {

        User user =
                new User(
                        "user@test.com",
                        "hash",
                        "John",
                        "Doe",
                        Role.PATIENT
                );


        assertThat(user.getUsername())
                .isEqualTo("user@test.com");
    }


    @Test
    void shouldReturnPasswordHash() {

        User user =
                new User(
                        "user@test.com",
                        "hash123",
                        "John",
                        "Doe",
                        Role.PATIENT
                );


        assertThat(user.getPassword())
                .isEqualTo("hash123");
    }


    @Test
    void shouldBeAccountValidByDefault() {

        User user =
                new User(
                        "user@test.com",
                        "hash",
                        "John",
                        "Doe",
                        Role.PATIENT
                );


        assertThat(user.isAccountNonExpired())
                .isTrue();

        assertThat(user.isAccountNonLocked())
                .isTrue();

        assertThat(user.isCredentialsNonExpired())
                .isTrue();
    }


    @Test
    void shouldReturnAuthoritiesFromRole() {

        User user =
                new User(
                        "admin@test.com",
                        "hash",
                        "Admin",
                        "User",
                        Role.ADMIN
                );


        assertThat(user.getAuthorities())
                .isNotEmpty();
    }


    @Test
    void shouldNotAllowNullEmail() {

        assertThatThrownBy(() ->
                new User(
                        null,
                        "hash",
                        "John",
                        "Doe",
                        Role.PATIENT
                )
        )
                .isInstanceOf(NullPointerException.class);
    }


    @Test
    void shouldNotAllowNullPassword() {

        assertThatThrownBy(() ->
                new User(
                        "user@test.com",
                        null,
                        "John",
                        "Doe",
                        Role.PATIENT
                )
        )
                .isInstanceOf(NullPointerException.class);
    }

}