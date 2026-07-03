package com.lirium.nutrition.infrastructure.security;


import com.lirium.nutrition.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomOAuth2UserService service;

    @Test
    void validateEmail_WithValidEmail_DoesNotThrow() {
        // Arrange
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", "test@gmail.com");
        attributes.put("email_verified", true);

        OAuth2User oAuth2User = new DefaultOAuth2User(
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                "email"
        );

        // Act & Assert
        assertDoesNotThrow(() -> service.validateEmail(oAuth2User));
    }

    @Test
    void validateEmail_WithUnverifiedEmail_ThrowsException() {
        // Arrange
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", "test@gmail.com");
        attributes.put("email_verified", false);

        OAuth2User oAuth2User = new DefaultOAuth2User(
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                "email"
        );

        // Act & Assert
        OAuth2AuthenticationException exception = assertThrows(
                OAuth2AuthenticationException.class,
                () -> service.validateEmail(oAuth2User)
        );

        assertEquals("Email address not provided", exception.getMessage());
    }

    @Test
    void validateEmail_WithNullEmail_ThrowsException() {
        // Arrange
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", null);
        attributes.put("email_verified", true);

        OAuth2User oAuth2User = new DefaultOAuth2User(
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                "email"
        );

        // Act & Assert
        OAuth2AuthenticationException exception = assertThrows(
                OAuth2AuthenticationException.class,
                () -> service.validateEmail(oAuth2User)
        );

        assertEquals("Email address not provided", exception.getMessage());
    }

    @Test
    void validateEmail_WithEmptyEmail_ThrowsException() {
        // Arrange
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", "");
        attributes.put("email_verified", true);

        OAuth2User oAuth2User = new DefaultOAuth2User(
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                "email"
        );

        // Act & Assert
        OAuth2AuthenticationException exception = assertThrows(
                OAuth2AuthenticationException.class,
                () -> service.validateEmail(oAuth2User)
        );

        assertEquals("Email address not provided", exception.getMessage());
    }
}