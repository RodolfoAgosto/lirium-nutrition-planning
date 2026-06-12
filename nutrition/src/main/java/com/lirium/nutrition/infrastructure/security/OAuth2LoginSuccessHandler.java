package com.lirium.nutrition.infrastructure.security;

import com.lirium.nutrition.model.entity.User;
import com.lirium.nutrition.model.enums.Role;
import com.lirium.nutrition.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String firstName = oAuth2User.getAttribute("given_name");
        String lastName = oAuth2User.getAttribute("family_name");

        // Buscar o crear usuario
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = new User(
                            email,
                            "",           // sin password — autenticó con Google
                            firstName,
                            lastName,
                            Role.PATIENT  // rol por defecto para nuevos usuarios
                    );
                    return userRepository.save(newUser);
                });

        // Generar JWT propio
        String token = jwtService.generateToken(user);

        // Redirigir al frontend con el token
        // El frontend lo lee de la URL y lo guarda en localStorage
        String redirectUrl = "http://localhost:3000/oauth2/callback?token=" + token;
        response.sendRedirect(redirectUrl);
    }
}