package com.lirium.nutrition.infrastructure.security;

import java.time.Duration;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

  private static final String KEY_PREFIX = "blacklist:jti:";

  private final StringRedisTemplate redisTemplate;

  public void blacklist(String jti, Date tokenExpiration) {
    if (jti == null) {
      return;
    }

    long ttlMillis = tokenExpiration.getTime() - System.currentTimeMillis();
    if (ttlMillis <= 0) {
      return;
    }

    try {
      redisTemplate.opsForValue().set(KEY_PREFIX + jti, "revoked", Duration.ofMillis(ttlMillis));
    } catch (Exception e) {
      log.warn("Redis unreachable while blacklisting token, logout continues: {}", e.getMessage());
    }
  }

  public boolean isBlacklisted(String jti) {
    if (jti == null) {
      return false;
    }
    try {
      return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_PREFIX + jti));
    } catch (Exception e) {
      // Fail-open, not fail-closed: if Redis is unreachable, authentication for every
      // valid token would otherwise break for everyone, over a problem that has nothing
      // to do with whether the token itself is legitimate. A logged-out-but-still-valid
      // token slipping through during a Redis outage is a much smaller risk than a full
      // API outage every time Redis has a hiccup.
      log.warn(
          "Redis unreachable while checking token blacklist, failing open: {}", e.getMessage());
      return false;
    }
  }
}
