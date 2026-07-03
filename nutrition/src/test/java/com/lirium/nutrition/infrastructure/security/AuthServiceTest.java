package com.lirium.nutrition.infrastructure.security;

import com.lirium.nutrition.dto.request.LoginRequestDTO;
import com.lirium.nutrition.dto.response.AuthResponseDTO;
import com.lirium.nutrition.model.entity.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.*;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthService authService;

    // login()

    @Test
    void shouldLoginSuccessfully() {
        LoginRequestDTO request = new LoginRequestDTO("test@mail.com", "password");

        User user = new User();

        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authentication);

        when(authentication.getPrincipal()).thenReturn(user);

        RefreshToken refreshToken = createRefreshToken(user);

        when(refreshTokenService.createRefreshToken(user)).thenReturn(refreshToken);

        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        AuthResponseDTO response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.token());
        assertEquals("refresh-token", response.refreshToken());

        verify(authenticationManager).authenticate(any(Authentication.class));
        verify(refreshTokenService).createRefreshToken(user);
        verify(jwtService).generateToken(user);
    }

    @Test
    void shouldThrowExceptionWhenCredentialsAreInvalid() {

        LoginRequestDTO request =
                new LoginRequestDTO("test@mail.com", "bad-password");

        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThrows(
                BadCredentialsException.class,
                () -> authService.login(request)
        );

        verifyNoInteractions(jwtService);
        verifyNoInteractions(refreshTokenService);
    }

    // refresh()

    @Test
    void shouldRefreshTokenSuccessfully() {

        User user = new User();

        RefreshToken refreshToken = createRefreshToken(user);

        when(refreshTokenService.validate("refresh-token"))
                .thenReturn(refreshToken);

        when(jwtService.generateToken(user))
                .thenReturn("new-access-token");

        AuthResponseDTO response =
                authService.refresh("refresh-token");

        assertEquals("new-access-token", response.token());
        assertEquals("refresh-token", response.refreshToken());

        verify(refreshTokenService).validate("refresh-token");
        verify(jwtService).generateToken(user);
    }

    @Test
    void shouldThrowExceptionWhenRefreshTokenIsInvalid() {

        when(refreshTokenService.validate("invalid-token"))
                .thenThrow(new RuntimeException("Invalid refresh token"));

        assertThrows(
                RuntimeException.class,
                () -> authService.refresh("invalid-token")
        );

        verifyNoInteractions(jwtService);
    }

    private RefreshToken createRefreshToken(User user) {
        return new RefreshToken(
                user,
                "refresh-token",
                Instant.now().plusSeconds(3600)
        );
    }

}