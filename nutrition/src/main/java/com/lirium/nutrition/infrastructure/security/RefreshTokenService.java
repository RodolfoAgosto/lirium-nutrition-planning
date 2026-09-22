package com.lirium.nutrition.infrastructure.security;

import com.lirium.nutrition.exception.InvalidRefreshTokenException;
import com.lirium.nutrition.model.entity.RefreshToken;
import com.lirium.nutrition.model.entity.User;
import com.lirium.nutrition.repository.RefreshTokenRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

  @Value("${app.jwt.refresh-expiration}")
  private long refreshExpiration;

  private final RefreshTokenRepository refreshTokenRepository;

  public RefreshToken createRefreshToken(User user) {
    revokeAssets(user);

    RefreshToken token =
        new RefreshToken(
            user, UUID.randomUUID().toString(), Instant.now().plusMillis(refreshExpiration));
    return refreshTokenRepository.save(token);
  }

  public RefreshToken validate(String token) {
    return refreshTokenRepository
        .findByToken(token)
        .filter(t -> !t.isRevoked())
        .filter(t -> !t.isExpired())
        .orElseThrow(() -> new InvalidRefreshTokenException("Refresh token invalid or expired"));
  }

  public void revokeAllForUser(User user) {
    revokeAssets(user);
  }

  private void revokeAssets(User user) {
    List<RefreshToken> tokens = refreshTokenRepository.findAllByUserAndRevokedFalse(user);
    tokens.forEach(RefreshToken::revoke);
    refreshTokenRepository.saveAll(tokens);
  }
}
