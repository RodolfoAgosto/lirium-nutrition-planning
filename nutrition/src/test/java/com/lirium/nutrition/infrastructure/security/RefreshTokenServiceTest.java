package com.lirium.nutrition.infrastructure.security;

import com.lirium.nutrition.exception.InvalidRefreshTokenException;
import com.lirium.nutrition.model.entity.RefreshToken;
import com.lirium.nutrition.model.entity.User;
import com.lirium.nutrition.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private User user;

    @BeforeEach
    void setup() throws Exception {
        ReflectionTestUtils.setField(
                refreshTokenService,
                "refreshExpiration",
                86400000L
        );
    }

    @Test
    void shouldCreateRefreshTokenWhenUserHasNoPreviousToken() {

        when(refreshTokenRepository.findByUserAndRevokedFalse(user)).thenReturn(Optional.empty());

        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RefreshToken token =
                refreshTokenService.createRefreshToken(user);

        assertNotNull(token);
        assertEquals(user, token.getUser());
        assertNotNull(token.getToken());

        verify(refreshTokenRepository).findByUserAndRevokedFalse(user);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void shouldRevokePreviousTokenAndCreateNewOne() {

        RefreshToken existingToken = spy(new RefreshToken(user, "old-token", Instant.now().plusSeconds(3600)));
        when(refreshTokenRepository.findByUserAndRevokedFalse(user)).thenReturn(Optional.of(existingToken));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));
        refreshTokenService.createRefreshToken(user);
        verify(existingToken).revoke();
        verify(refreshTokenRepository, times(2)).save(any(RefreshToken.class));
    }


    @Test
    void shouldReturnTokenWhenTokenIsValid() {

        RefreshToken token = mock(RefreshToken.class);

        when(token.isRevoked()).thenReturn(false);
        when(token.isExpired()).thenReturn(false);

        when(refreshTokenRepository.findByToken("valid-token"))
                .thenReturn(Optional.of(token));

        RefreshToken result =
                refreshTokenService.validate("valid-token");

        assertEquals(token, result);
    }

    @Test
    void shouldThrowExceptionWhenTokenIsInvalid() {

        when(refreshTokenRepository.findByToken("invalid-token"))
                .thenReturn(Optional.empty());

        InvalidRefreshTokenException exception =
                assertThrows(
                        InvalidRefreshTokenException.class,
                        () -> refreshTokenService.validate("invalid-token")
                );

        assertEquals(
                "Refresh token invalid or expired",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowExceptionWhenTokenIsRevoked() {

        RefreshToken token = mock(RefreshToken.class);

        when(token.isRevoked()).thenReturn(true);

        when(refreshTokenRepository.findByToken("token"))
                .thenReturn(Optional.of(token));

        assertThrows(
                InvalidRefreshTokenException.class,
                () -> refreshTokenService.validate("token")
        );
    }

}