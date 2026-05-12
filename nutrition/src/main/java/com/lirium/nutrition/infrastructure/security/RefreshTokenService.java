package com.lirium.nutrition.infrastructure.security;

import com.lirium.nutrition.exception.InvalidRefreshTokenException;
import com.lirium.nutrition.model.entity.RefreshToken;
import com.lirium.nutrition.model.entity.User;
import com.lirium.nutrition.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Value("${app.jwt.refresh-expiration}")
    private long refreshExpiration;

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshToken createRefreshToken(User user) {
        // Revocar el anterior si existe
        refreshTokenRepository.findByUser(user)
                .ifPresent(t -> {
                    t.revoke();
                    refreshTokenRepository.save(t);
                });

        RefreshToken token = new RefreshToken(
                user,
                UUID.randomUUID().toString(),
                Instant.now().plusMillis(refreshExpiration)
        );
        return refreshTokenRepository.save(token);
    }

    public RefreshToken validate(String token) {
        return refreshTokenRepository.findByToken(token)
                .filter(t -> !t.isRevoked())
                .filter(t -> !t.isExpired())
                .orElseThrow(() ->
                        new InvalidRefreshTokenException("Refresh token inválido o expirado"));
    }
}