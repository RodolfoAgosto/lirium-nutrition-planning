package com.lirium.nutrition.infrastructure.security;

import com.lirium.nutrition.model.entity.User;
import com.lirium.nutrition.model.enums.Role;
import com.lirium.nutrition.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

  private final UserRepository userRepository;
  private final JwtService jwtService;

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

    // Generar JWT propio
    String token = jwtService.generateToken(user);

    // Redirigir al frontend con el token
    // El frontend lo lee de la URL y lo guarda en localStorage
    // String redirectUrl = "http://localhost:3000/oauth2/callback?token=" + token;
    // response.sendRedirect(redirectUrl);
    String html =
        """
              <html><body style="font-family:sans-serif;max-width:600px;margin:40px auto">
                <h2>✅ Login exitoso con Google</h2>
                <p>Copiá este token y pegalo en el botón <b>Authorize</b> de Swagger:</p>
                <textarea style="width:100%;height:100px">__TOKEN__</textarea>
              </body></html>
              """
            .replace("__TOKEN__", token);

    response.setContentType("text/html");
    response.getWriter().write(html);
  }
}
