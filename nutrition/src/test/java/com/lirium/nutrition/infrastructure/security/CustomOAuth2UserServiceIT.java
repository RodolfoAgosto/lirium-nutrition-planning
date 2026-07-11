package com.lirium.nutrition.infrastructure.security;

import com.lirium.nutrition.controller.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class CustomOAuth2UserServiceIT extends AbstractIntegrationTest {


    @Autowired
    private CustomOAuth2UserService service;


    @Test
    void shouldRejectOAuthUserWithoutEmail() {

        OAuth2User user = new OAuth2User() {

            @Override
            public <A> A getAttribute(String name) {
                if ("email_verified".equals(name)) {
                    return (A) Boolean.TRUE;
                }
                return null;
            }

            @Override
            public Map<String, Object> getAttributes() {
                return Map.of(
                        "email_verified", true
                );
            }

            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return List.of();
            }

            @Override
            public String getName() {
                return "test";
            }
        };


        assertThatThrownBy(() ->
                service.validateEmail(user)
        )
                .isInstanceOf(OAuth2AuthenticationException.class);
    }



    @Test
    void shouldRejectOAuthUserWithUnverifiedEmail() {

        OAuth2User user =
                new DefaultOAuth2User(
                        null,
                        Map.of(
                                "email", "test@test.com",
                                "email_verified", false
                        ),
                        "email"
                );


        assertThatThrownBy(() ->
                service.validateEmail(user)
        )
                .isInstanceOf(OAuth2AuthenticationException.class);
    }


    @Test
    void shouldAcceptOAuthUserWithVerifiedEmail() {

        OAuth2User user =
                new DefaultOAuth2User(
                        null,
                        Map.of(
                                "email", "test@test.com",
                                "email_verified", true
                        ),
                        "email"
                );


        service.validateEmail(user);
    }

}