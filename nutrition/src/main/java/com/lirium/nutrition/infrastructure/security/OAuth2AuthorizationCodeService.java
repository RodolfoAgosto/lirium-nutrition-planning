package com.lirium.nutrition.infrastructure.security;

import com.lirium.nutrition.exception.UnauthorizedException;
import com.lirium.nutrition.model.entity.User;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class OAuth2AuthorizationCodeService {

  private final Map<String, OAuth2AuthorizationCode> codes = new ConcurrentHashMap<>();

  public String generateCode(User user) {
    String code = UUID.randomUUID().toString();

    codes.put(code, new OAuth2AuthorizationCode(user.getId(), Instant.now().plusSeconds(60)));

    return code;
  }

  public Long consumeCode(String code) {
    OAuth2AuthorizationCode authorizationCode = codes.remove(code);

    if (authorizationCode == null || authorizationCode.expiresAt().isBefore(Instant.now())) {
      throw new UnauthorizedException("Invalid or expired OAuth2 code");
    }

    return authorizationCode.userId();
  }
}
