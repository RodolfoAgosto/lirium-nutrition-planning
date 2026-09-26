package com.lirium.nutrition.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Date;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class TokenBlacklistServiceTest {

  private static final String KEY = "blacklist:jti:abc";

  @Mock private StringRedisTemplate redisTemplate;
  @Mock private ValueOperations<String, String> valueOperations;

  @InjectMocks private TokenBlacklistService tokenBlacklistService;

  // ------------------------------------------------------------ blacklist

  @Test
  @DisplayName("Stores the revoked token only until it would have expired anyway")
  void shouldStoreRevokedTokenWithRemainingLifetimeAsTtl() {
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    Date expiration = new Date(System.currentTimeMillis() + 60_000);

    tokenBlacklistService.blacklist("abc", expiration);

    verify(valueOperations)
        .set(
            eq(KEY),
            eq("revoked"),
            argThat((Duration ttl) -> ttl.toMillis() > 0 && ttl.toMillis() <= 60_000));
  }

  @Test
  @DisplayName("Ignores tokens without an id")
  void shouldIgnoreTokenWithoutId() {
    tokenBlacklistService.blacklist(null, new Date(System.currentTimeMillis() + 60_000));

    verifyNoInteractions(redisTemplate);
  }

  @Test
  @DisplayName("Doesn't store tokens that are already expired")
  void shouldNotStoreExpiredToken() {
    tokenBlacklistService.blacklist("abc", new Date(System.currentTimeMillis() - 1_000));

    verifyNoInteractions(redisTemplate);
  }

  @Test
  @DisplayName("Logout continues when Redis is unreachable")
  void shouldNotFailLogoutWhenRedisIsDown() {
    when(redisTemplate.opsForValue()).thenThrow(new RedisConnectionFailureException("down"));

    assertThatCode(
            () ->
                tokenBlacklistService.blacklist(
                    "abc", new Date(System.currentTimeMillis() + 60_000)))
        .doesNotThrowAnyException();
  }

  // ------------------------------------------------------------ isBlacklisted

  @Test
  @DisplayName("A token is blacklisted when its key exists in Redis")
  void shouldReportBlacklistedTokenWhenKeyExists() {
    when(redisTemplate.hasKey(KEY)).thenReturn(true);

    assertThat(tokenBlacklistService.isBlacklisted("abc")).isTrue();
  }

  @Test
  @DisplayName("A token is not blacklisted when its key doesn't exist")
  void shouldReportValidTokenWhenKeyDoesNotExist() {
    when(redisTemplate.hasKey(KEY)).thenReturn(false);

    assertThat(tokenBlacklistService.isBlacklisted("abc")).isFalse();
  }

  @Test
  @DisplayName("A null answer from Redis counts as not blacklisted")
  void shouldTreatNullAnswerAsNotBlacklisted() {
    when(redisTemplate.hasKey(KEY)).thenReturn(null);

    assertThat(tokenBlacklistService.isBlacklisted("abc")).isFalse();
  }

  @Test
  @DisplayName("Tokens without an id are never blacklisted")
  void shouldNeverBlacklistTokenWithoutId() {
    assertThat(tokenBlacklistService.isBlacklisted(null)).isFalse();

    verifyNoInteractions(redisTemplate);
  }

  @Test
  @DisplayName("Fails open when Redis is unreachable: the token is accepted")
  void shouldFailOpenWhenRedisIsDown() {
    when(redisTemplate.hasKey(KEY)).thenThrow(new RedisConnectionFailureException("down"));

    assertThat(tokenBlacklistService.isBlacklisted("abc")).isFalse();
  }
}
