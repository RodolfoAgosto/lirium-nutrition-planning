package com.lirium.nutrition.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.lirium.nutrition.model.entity.PatientProfile;
import com.lirium.nutrition.model.entity.User;
import com.lirium.nutrition.model.enums.Role;
import com.lirium.nutrition.repository.RefreshTokenRepository;
import com.lirium.nutrition.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerIT  extends AbstractIntegrationTest{

    private UserDetails admin;

    @BeforeEach
    void setup() {

        refreshTokenRepository.deleteAll();
        patientRepository.deleteAll();
        userRepository.deleteAll();
        foodRepository.deleteAll();

        admin = userRepository.save(new User(
                "admin@test.com",
                passwordEncoder.encode("1234"),
                "Admin", "Test", Role.ADMIN));

        User nutritionist = userRepository.save(new User(
                "nutri@test.com",
                passwordEncoder.encode("1234"),
                "Nutri", "Test", Role.NUTRITIONIST));

        User patient = userRepository.save(new User(
                "patient@test.com",
                passwordEncoder.encode("1234"),
                "Patient", "Test", Role.PATIENT));

        User otherPatient = userRepository.save(new User(
                "other@test.com",
                passwordEncoder.encode("1234"),
                "Other", "Test", Role.PATIENT));

    }

    // Tests de Autenticación
    @Test
    @DisplayName("Login exitoso devuelve token")
    void shouldReturnAccessAndRefreshTokenWhenCredentialsAreValid() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                    "email": "admin@test.com",
                    "password": "1234"
                }
            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.refreshToken").isString())
                .andReturn();

        String token = JsonPath.read(result.getResponse().getContentAsString(), "$.token");

        assertTrue(jwtService.isTokenValid(token, admin));
        assertEquals("admin@test.com", jwtService.extractUsername(token));
    }

    @Test
    @DisplayName("Login con password incorrecta devuelve 401")
    void shouldReturnUnauthorizedWhenPasswordIsIncorrect() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                    "email": "admin@test.com",
                    "password": "wrongpassword"
                }
            """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Login con usuario inexistente devuelve 401")
    void shouldReturnUnauthorizedWhenUserDoesNotExist() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                    "email": "noexiste@test.com",
                    "password": "1234"
                }
            """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Refresh token válido devuelve nuevo access token")
    void shouldReturnNewAccessTokenWhenRefreshTokenIsValid() throws Exception {
        // Primero login para obtener refresh token
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {"email": "admin@test.com", "password": "1234"}
            """))
                .andExpect(status().isOk())
                .andReturn();

        String refreshToken = JsonPath.read(
                result.getResponse().getContentAsString(),
                "$.refreshToken");

        // Usar refresh token
        MvcResult resultRefresh = mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {"refreshToken": "%s"}
            """.formatted(refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        String newToken = JsonPath.read(result.getResponse().getContentAsString(), "$.token");

        assertTrue(jwtService.isTokenValid(newToken, admin));
        assertEquals("admin@test.com", jwtService.extractUsername(newToken));
    }

    @Test
    @DisplayName("Refresh token inválido devuelve 401")
    void shouldReturnUnauthorizedWhenRefreshTokenIsInvalid() throws Exception {
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {"refreshToken": "tokenfalso"}
            """))
                .andExpect(status().isUnauthorized());
    }


}