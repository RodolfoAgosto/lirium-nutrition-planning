package com.lirium.nutrition.infrastructure.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.lirium.nutrition.exception.InvalidRefreshTokenException;
import com.lirium.nutrition.model.entity.RefreshToken;
import com.lirium.nutrition.model.entity.User;
import com.lirium.nutrition.repository.RefreshTokenRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

  @InjectMocks private RefreshTokenService refreshTokenService;

  @Mock private RefreshTokenRepository refreshTokenRepository;

  @MockBean private TokenBlacklistService tokenBlacklistService;

  @Mock private User user;

  @BeforeEach
  void setup() throws Exception {
    ReflectionTestUtils.setField(refreshTokenService, "refreshExpiration", 86400000L);
  }

  @Test
  void shouldCreateRefreshTokenWhenUserHasNoPreviousToken() {

    when(refreshTokenRepository.findAllByUserAndRevokedFalse(user)).thenReturn(List.of());

    when(refreshTokenRepository.save(any(RefreshToken.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    RefreshToken token = refreshTokenService.createRefreshToken(user);

    assertNotNull(token);
    assertEquals(user, token.getUser());
    assertNotNull(token.getToken());

    verify(refreshTokenRepository).findAllByUserAndRevokedFalse(user);
    verify(refreshTokenRepository).save(any(RefreshToken.class));
  }

  @Test
  void shouldRevokePreviousTokenAndCreateNewOne() {

    RefreshToken existingToken =
        spy(new RefreshToken(user, "old-token", Instant.now().plusSeconds(3600)));
    when(refreshTokenRepository.findAllByUserAndRevokedFalse(user))
        .thenReturn(List.of(existingToken));
    when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));
    refreshTokenService.createRefreshToken(user);
    verify(existingToken).revoke();
    verify(refreshTokenRepository).saveAll(List.of(existingToken));
    verify(refreshTokenRepository).save(any(RefreshToken.class));
  }

  @Test
  void shouldReturnTokenWhenTokenIsValid() {

    RefreshToken token = mock(RefreshToken.class);

    when(token.isRevoked()).thenReturn(false);
    when(token.isExpired()).thenReturn(false);

    when(refreshTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(token));

    RefreshToken result = refreshTokenService.validate("valid-token");

    assertEquals(token, result);
  }

  @Test
  void shouldThrowExceptionWhenTokenIsInvalid() {

    when(refreshTokenRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

    InvalidRefreshTokenException exception =
        assertThrows(
            InvalidRefreshTokenException.class,
            () -> refreshTokenService.validate("invalid-token"));

    assertEquals("Refresh token invalid or expired", exception.getMessage());
  }

  @Test
  void shouldThrowExceptionWhenTokenIsRevoked() {

    RefreshToken token = mock(RefreshToken.class);

    when(token.isRevoked()).thenReturn(true);

    when(refreshTokenRepository.findByToken("token")).thenReturn(Optional.of(token));

    assertThrows(InvalidRefreshTokenException.class, () -> refreshTokenService.validate("token"));
  }
}
