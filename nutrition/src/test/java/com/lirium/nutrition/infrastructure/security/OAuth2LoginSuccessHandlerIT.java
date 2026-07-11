package com.lirium.nutrition.infrastructure.security;

import com.lirium.nutrition.controller.AbstractIntegrationTest;
import com.lirium.nutrition.model.entity.User;
import com.lirium.nutrition.model.enums.Role;
import com.lirium.nutrition.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.*;


@SpringBootTest
@ActiveProfiles("test")
class OAuth2LoginSuccessHandlerIT extends AbstractIntegrationTest {

    @Autowired
    private OAuth2LoginSuccessHandler handler;

    @Autowired
    private UserRepository userRepository;


    @Test
    void shouldCreateUserAndRedirectWhenOAuthUserIsNew() throws Exception {

        OAuth2User oAuth2User =
                new DefaultOAuth2User(
                        null,
                        Map.of(
                                "email", "google@test.com",
                                "given_name", "John",
                                "family_name", "Doe"
                        ),
                        "email"
                );


        Authentication authentication =
                mock(Authentication.class);

        when(authentication.getPrincipal())
                .thenReturn(oAuth2User);


        HttpServletResponse response =
                mock(HttpServletResponse.class);


        handler.onAuthenticationSuccess(
                null,
                response,
                authentication
        );


        User user =
                userRepository.findByEmail("google@test.com")
                        .orElseThrow();


        assertThat(user.getEmail())
                .isEqualTo("google@test.com");

        assertThat(user.getFirstName())
                .isEqualTo("John");

        assertThat(user.getLastName())
                .isEqualTo("Doe");

        assertThat(user.getRole())
                .isEqualTo(Role.PATIENT);


        verify(response)
                .sendRedirect(
                        startsWith(
                                "http://localhost:3000/oauth2/callback?token="
                        )
                );
    }


    @Test
    void shouldUseExistingUserWhenOAuthEmailAlreadyExists() throws Exception {

        User existing =
                new User(
                        "existing@test.com",
                        "",
                        "Existing",
                        "User",
                        Role.PATIENT
                );


        userRepository.save(existing);


        OAuth2User oAuth2User =
                new DefaultOAuth2User(
                        null,
                        Map.of(
                                "email", "existing@test.com",
                                "given_name", "Other",
                                "family_name", "Name"
                        ),
                        "email"
                );


        Authentication authentication =
                mock(Authentication.class);

        when(authentication.getPrincipal())
                .thenReturn(oAuth2User);


        HttpServletResponse response =
                mock(HttpServletResponse.class);


        handler.onAuthenticationSuccess(
                null,
                response,
                authentication
        );


        List<User> users =
                userRepository.findAll();


        assertThat(users)
                .hasSize(1);

        assertThat(users.get(0).getFirstName())
                .isEqualTo("Existing");


        verify(response)
                .sendRedirect(
                        startsWith(
                                "http://localhost:3000/oauth2/callback?token="
                        )
                );
    }
}