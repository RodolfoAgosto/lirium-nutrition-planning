package com.lirium.nutrition.infrastructure.security;

import com.lirium.nutrition.model.entity.*;
import com.lirium.nutrition.model.enums.*;
import com.lirium.nutrition.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
        import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuth2LoginSuccessHandlerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private OAuth2LoginSuccessHandler handler;

    private static final String EMAIL = "test@gmail.com";
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final String TOKEN = "jwt-token-123";

    // ==================== HELPERS ====================

    private OAuth2User createOAuth2User(String email, String firstName, String lastName) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", email);
        attributes.put("given_name", firstName);
        attributes.put("family_name", lastName);

        return new DefaultOAuth2User(
                Collections.singletonList(() -> "ROLE_USER"),
                attributes,
                "email"
        );
    }

    private User createUser(String email, String firstName, String lastName) {
        return new User(email, "", firstName, lastName, Role.PATIENT);
    }

    private void mockAuthenticationPrincipal(OAuth2User oAuth2User) {
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
    }

    // ==================== TESTS ====================

    @Test
    void shouldRedirectWithTokenWhenUserExists() throws IOException {
        // Arrange
        OAuth2User oAuth2User = createOAuth2User(EMAIL, FIRST_NAME, LAST_NAME);
        User existingUser = createUser(EMAIL, FIRST_NAME, LAST_NAME);

        mockAuthenticationPrincipal(oAuth2User);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existingUser));
        when(jwtService.generateToken(existingUser)).thenReturn(TOKEN);

        // Act
        handler.onAuthenticationSuccess(request, response, authentication);

        // Assert
        String expectedRedirect = "http://localhost:3000/oauth2/callback?token=" + TOKEN;
        verify(response).sendRedirect(expectedRedirect);
        verify(userRepository, never()).save(any(User.class));
        verify(jwtService).generateToken(existingUser);
    }

    @Test
    void shouldCreateNewUser_WhenUserDoesNotExist() throws IOException {
        // Arrange
        OAuth2User oAuth2User = createOAuth2User(EMAIL, FIRST_NAME, LAST_NAME);
        User newUser = createUser(EMAIL, FIRST_NAME, LAST_NAME);

        mockAuthenticationPrincipal(oAuth2User);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(newUser);
        when(jwtService.generateToken(newUser)).thenReturn(TOKEN);

        // Act
        handler.onAuthenticationSuccess(request, response, authentication);

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals(EMAIL, savedUser.getEmail());
        assertEquals(FIRST_NAME, savedUser.getFirstName());
        assertEquals(LAST_NAME, savedUser.getLastName());
        assertEquals("", savedUser.getPassword());
        assertEquals(Role.PATIENT, savedUser.getRole());

        verify(response).sendRedirect("http://localhost:3000/oauth2/callback?token=" + TOKEN);
    }

    @Test
    void shouldSaveUserWithNullFirstNameWhenFirstNameNotProvided() throws IOException {
        // Arrange
        OAuth2User oAuth2User = createOAuth2User(EMAIL, null, LAST_NAME);
        User newUser = createUser(EMAIL, null, LAST_NAME);

        mockAuthenticationPrincipal(oAuth2User);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(newUser);
        when(jwtService.generateToken(newUser)).thenReturn(TOKEN);

        // Act
        handler.onAuthenticationSuccess(request, response, authentication);

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        assertNull(userCaptor.getValue().getFirstName());
        assertEquals(LAST_NAME, userCaptor.getValue().getLastName());
    }

    @Test
    void shouldSaveUserWithNullLastNameWhenLastNameNotProvided() throws IOException {
        // Arrange
        OAuth2User oAuth2User = createOAuth2User(EMAIL, FIRST_NAME, null);
        User newUser = createUser(EMAIL, FIRST_NAME, null);

        mockAuthenticationPrincipal(oAuth2User);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(newUser);
        when(jwtService.generateToken(newUser)).thenReturn(TOKEN);

        // Act
        handler.onAuthenticationSuccess(request, response, authentication);

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        assertEquals(FIRST_NAME, userCaptor.getValue().getFirstName());
        assertNull(userCaptor.getValue().getLastName());
    }

    @Test
    void shouldSaveUserWithNullFirstNameAndLastNameWhenNeitherProvided() throws IOException {
        // Arrange
        OAuth2User oAuth2User = createOAuth2User(EMAIL, null, null);
        User newUser = createUser(EMAIL, null, null);

        mockAuthenticationPrincipal(oAuth2User);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(newUser);
        when(jwtService.generateToken(newUser)).thenReturn(TOKEN);

        // Act
        handler.onAuthenticationSuccess(request, response, authentication);

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        assertNull(userCaptor.getValue().getFirstName());
        assertNull(userCaptor.getValue().getLastName());
    }

    @Test
    void shouldNotUpdateExistingUser_WhenUserAlreadyExists() throws IOException {
        // Arrange
        OAuth2User oAuth2User = createOAuth2User(EMAIL, "Jane", "Smith");
        User existingUser = createUser(EMAIL, "OldName", "OldLastName");

        mockAuthenticationPrincipal(oAuth2User);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existingUser));
        when(jwtService.generateToken(existingUser)).thenReturn(TOKEN);

        // Act
        handler.onAuthenticationSuccess(request, response, authentication);

        // Assert
        verify(userRepository, never()).save(any(User.class));
        assertEquals("OldName", existingUser.getFirstName());
        assertEquals("OldLastName", existingUser.getLastName());
    }

    @Test
    void shouldGenerateTokenWithCorrectUser_WhenUserExists() throws IOException {
        // Arrange
        OAuth2User oAuth2User = createOAuth2User(EMAIL, FIRST_NAME, LAST_NAME);
        User existingUser = createUser(EMAIL, FIRST_NAME, LAST_NAME);

        mockAuthenticationPrincipal(oAuth2User);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existingUser));
        when(jwtService.generateToken(existingUser)).thenReturn(TOKEN);

        // Act
        handler.onAuthenticationSuccess(request, response, authentication);

        // Assert
        verify(jwtService).generateToken(existingUser);
    }

    @Test
    void shouldGenerateTokenWithNewUser_WhenUserDoesNotExist() throws IOException {
        // Arrange
        OAuth2User oAuth2User = createOAuth2User(EMAIL, FIRST_NAME, LAST_NAME);
        User newUser = createUser(EMAIL, FIRST_NAME, LAST_NAME);

        mockAuthenticationPrincipal(oAuth2User);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(newUser);
        when(jwtService.generateToken(newUser)).thenReturn(TOKEN);

        // Act
        handler.onAuthenticationSuccess(request, response, authentication);

        // Assert
        verify(jwtService).generateToken(newUser);
    }

    @Test
    void shouldRedirectToCorrectUrl_WithTokenInQueryParam() throws IOException {
        // Arrange
        OAuth2User oAuth2User = createOAuth2User(EMAIL, FIRST_NAME, LAST_NAME);
        User existingUser = createUser(EMAIL, FIRST_NAME, LAST_NAME);

        mockAuthenticationPrincipal(oAuth2User);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existingUser));
        when(jwtService.generateToken(existingUser)).thenReturn(TOKEN);

        // Act
        handler.onAuthenticationSuccess(request, response, authentication);

        // Assert
        ArgumentCaptor<String> redirectCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).sendRedirect(redirectCaptor.capture());

        String redirectUrl = redirectCaptor.getValue();
        assertTrue(redirectUrl.startsWith("http://localhost:3000/oauth2/callback?token="));
        assertTrue(redirectUrl.contains(TOKEN));
    }

    @Test
    void shouldThrowExceptionWhenJwtServiceFails() throws IOException {
        // Arrange
        OAuth2User oAuth2User = createOAuth2User(EMAIL, FIRST_NAME, LAST_NAME);
        User existingUser = createUser(EMAIL, FIRST_NAME, LAST_NAME);

        mockAuthenticationPrincipal(oAuth2User);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existingUser));
        when(jwtService.generateToken(existingUser)).thenThrow(new RuntimeException("JWT generation error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                handler.onAuthenticationSuccess(request, response, authentication)
        );

        verify(response, never()).sendRedirect(anyString());
    }

    @Test
    void shouldThrowExceptionWhenUserSaveFails() throws IOException {
        // Arrange
        OAuth2User oAuth2User = createOAuth2User(EMAIL, FIRST_NAME, LAST_NAME);

        mockAuthenticationPrincipal(oAuth2User);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                handler.onAuthenticationSuccess(request, response, authentication)
        );

        verify(jwtService, never()).generateToken(any());
        verify(response, never()).sendRedirect(anyString());
    }

    @Test
    void shouldThrowIOExceptionWhenRedirectFails() throws IOException {
        // Arrange
        OAuth2User oAuth2User = createOAuth2User(EMAIL, FIRST_NAME, LAST_NAME);
        User existingUser = createUser(EMAIL, FIRST_NAME, LAST_NAME);

        mockAuthenticationPrincipal(oAuth2User);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existingUser));
        when(jwtService.generateToken(existingUser)).thenReturn(TOKEN);
        doThrow(new IOException("Redirect failed")).when(response).sendRedirect(anyString());

        // Act & Assert
        assertThrows(IOException.class, () ->
                handler.onAuthenticationSuccess(request, response, authentication)
        );
    }
}
