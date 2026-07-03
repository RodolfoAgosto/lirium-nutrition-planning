package com.lirium.nutrition.infrastructure.security;

import com.lirium.nutrition.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(request);
        validateEmail(oAuth2User);
        return oAuth2User;
    }

    void validateEmail(OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");
        Boolean emailVerified = oAuth2User.getAttribute("email_verified");

        if (email == null || email.isEmpty()) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("email_missing"),
                    "Email address not provided"
            );
        }

        if (emailVerified == null || !emailVerified) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("email_not_verified"),
                    "Email address not provided"
            );
        }
    }
}