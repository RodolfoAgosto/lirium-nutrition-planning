package com.lirium.nutrition.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.lirium.nutrition.exception.UnauthorizedException;
import com.lirium.nutrition.model.entity.User;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class OAuth2AuthorizationCodeServiceTest {

  private final OAuth2AuthorizationCodeService service = new OAuth2AuthorizationCodeService();

  @Test
  @DisplayName("A generated code can be exchanged for the user's id")
  void shouldExchangeCodeForUserId() {
    String code = service.generateCode(user(42L));

    assertThat(service.consumeCode(code)).isEqualTo(42L);
  }

  @Test
  @DisplayName("Each generated code is different")
  void shouldGenerateUniqueCodes() {
    User user = user(42L);

    assertThat(service.generateCode(user)).isNotEqualTo(service.generateCode(user));
  }

  @Test
  @DisplayName("A code can only be used once")
  void shouldRejectCodeUsedTwice() {
    String code = service.generateCode(user(42L));
    service.consumeCode(code);

    assertThatThrownBy(() -> service.consumeCode(code)).isInstanceOf(UnauthorizedException.class);
  }

  @Test
  @DisplayName("An unknown code is rejected")
  void shouldRejectUnknownCode() {
    assertThatThrownBy(() -> service.consumeCode("unknown"))
        .isInstanceOf(UnauthorizedException.class);
  }

  @Test
  @DisplayName("An expired code is rejected")
  @SuppressWarnings("unchecked")
  void shouldRejectExpiredCode() {
    Map<String, OAuth2AuthorizationCode> codes =
        (Map<String, OAuth2AuthorizationCode>) ReflectionTestUtils.getField(service, "codes");
    codes.put("expired", new OAuth2AuthorizationCode(42L, Instant.now().minusSeconds(1)));

    assertThatThrownBy(() -> service.consumeCode("expired"))
        .isInstanceOf(UnauthorizedException.class);
  }

  private static User user(Long id) {
    User user = new User();
    user.setId(id);
    return user;
  }
}
