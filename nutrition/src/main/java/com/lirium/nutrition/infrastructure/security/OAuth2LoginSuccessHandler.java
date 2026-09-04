package com.lirium.nutrition.infrastructure.security;

import com.lirium.nutrition.model.entity.User;
import com.lirium.nutrition.model.enums.Role;
import com.lirium.nutrition.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

  @Value("${app.frontend-url:}")
  private String frontendUrl;

  private final UserRepository userRepository;
  private final JwtService jwtService;
  private final OAuth2AuthorizationCodeService authorizationCodeService;

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException {

    OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

    String email = oAuth2User.getAttribute("email");
    String firstName = oAuth2User.getAttribute("given_name");
    String lastName = oAuth2User.getAttribute("family_name");

    // Buscar o crear usuario
    User user =
        userRepository
            .findByEmail(email)
            .orElseGet(
                () -> {
                  User newUser =
                      new User(
                          email,
                          "", // sin password — autenticó con Google
                          firstName,
                          lastName,
                          Role.PATIENT // rol por defecto para nuevos usuarios
                          );
                  return userRepository.save(newUser);
                });

    if (frontendUrl.isBlank()) {
      String token = jwtService.generateToken(user);
      writeDemoResponse(response, token);
    } else {
      redirectToFrontend(response, user);
    }
  }

  private void redirectToFrontend(HttpServletResponse response, User user) throws IOException {
    String code = authorizationCodeService.generateCode(user);
    String redirectUrl = frontendUrl + "/oauth2/callback?code=" + code;
    response.sendRedirect(redirectUrl);
  }

  private void writeDemoResponse(HttpServletResponse response, String token) throws IOException {
    String html =
        """
            <!DOCTYPE html>
            <html>
            <head><title>Login Successful</title></head>
            <body style="font-family:sans-serif;max-width:600px;margin:40px auto;padding:20px;">
              <h2> Login exitoso con Google</h2>
              <p>Copiá este token JWT para probar los endpoints protegidos en Swagger:</p>
              <textarea style="width:100%%;height:120px;font-family:monospace;padding:10px;">%s</textarea>
            </body>
            </html>
            """
            .formatted(token);

    response.setContentType("text/html;charset=UTF-8");
    response.getWriter().write(html);
    response.getWriter().flush();
  }
}
