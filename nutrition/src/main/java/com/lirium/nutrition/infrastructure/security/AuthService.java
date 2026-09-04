package com.lirium.nutrition.infrastructure.security;

import com.lirium.nutrition.dto.request.LoginRequestDTO;
import com.lirium.nutrition.dto.response.AuthResponseDTO;
import com.lirium.nutrition.exception.UnauthorizedException;
import com.lirium.nutrition.model.entity.RefreshToken;
import com.lirium.nutrition.model.entity.User;
import com.lirium.nutrition.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;
  private final RefreshTokenService refreshTokenService;
  private final OAuth2AuthorizationCodeService authorizationCodeService;
  private final UserRepository userRepository;

  public AuthResponseDTO login(LoginRequestDTO request) {

    Authentication authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.password()));

    User user = (User) authentication.getPrincipal();

    RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
    String token = jwtService.generateToken(user);

    return new AuthResponseDTO(token, refreshToken.getToken());
  }

  public AuthResponseDTO refresh(String refreshToken) {
    RefreshToken token = refreshTokenService.validate(refreshToken);
    String newAccessToken = jwtService.generateToken(token.getUser());
    return new AuthResponseDTO(newAccessToken, refreshToken);
  }

  public AuthResponseDTO exchangeOAuth2Code(String code) {
    Long userId = authorizationCodeService.consumeCode(code);

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new UnauthorizedException("User not found"));

    String token = jwtService.generateToken(user);
    RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
    return new AuthResponseDTO(token, refreshToken.getToken());
  }
}
