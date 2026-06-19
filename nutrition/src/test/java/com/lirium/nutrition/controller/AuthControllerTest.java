package com.lirium.nutrition.controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lirium.nutrition.dto.request.RefreshRequestDTO;
import com.lirium.nutrition.exception.InvalidRefreshTokenException;
import com.lirium.nutrition.infrastructure.security.AuthService;
import com.lirium.nutrition.infrastructure.security.JwtService;
import com.lirium.nutrition.infrastructure.security.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lirium.nutrition.dto.request.LoginRequestDTO;
import com.lirium.nutrition.dto.response.AuthResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @MockBean
    private AuthService authService;

    @MockBean
    JwtService jwtService;

    @MockBean
    UserDetailsServiceImpl userDetailsService;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void shouldLoginSuccessfully() throws Exception {

        // Given
        LoginRequestDTO request = new LoginRequestDTO("user@mail.com", "password");
        AuthResponseDTO response = new AuthResponseDTO("access-token", "refresh-token");

        // When + Then
        when(authService.login(any(LoginRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));

        verify(authService).login(any(LoginRequestDTO.class));
    }

    @Test
    void shouldRefreshTokenSuccessfully() throws Exception{

        // Given
        RefreshRequestDTO request = new RefreshRequestDTO("refresh-token");
        AuthResponseDTO response = new AuthResponseDTO("new-access-token", "refresh-token");

        when(authService.refresh(any(String.class)))
                .thenReturn(response);

        // When
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("new-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));

        verify(authService).refresh(any(String.class));

    }

    @Test
    void shouldReturnUnauthorizedWhenAuthenticationFails() throws Exception {

        // Given
        LoginRequestDTO request = new LoginRequestDTO("user", "wrong-password");

        // When + Then
        when(authService.login(any(LoginRequestDTO.class)))
            .thenThrow(new BadCredentialsException("Invalid credentials"));
        mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Invalid email or password"));

        verify(authService).login(any(LoginRequestDTO.class));

    }

    @Test
    void shouldReturnUnauthorizedWhenRefreshTokenIsInvalid() throws Exception {

        // Given
        RefreshRequestDTO request = new RefreshRequestDTO("wrong-refresh-token");

        when(authService.refresh(any(String.class)))
                .thenThrow(new InvalidRefreshTokenException("Invalid token."));

        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());

        verify(authService).refresh(any(String.class));

    }

}