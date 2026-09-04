package com.lirium.nutrition.infrastructure.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.lirium.nutrition.model.entity.User;
import com.lirium.nutrition.model.enums.Role;
import com.lirium.nutrition.repository.UserRepository;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class OAuth2LoginSuccessHandlerTest {

  @Mock private UserRepository userRepository;
  @Mock private JwtService jwtService;
  @Mock private OAuth2AuthorizationCodeService authorizationCodeService;
  @Mock private Authentication authentication;

  @InjectMocks private OAuth2LoginSuccessHandler handler;

  private MockHttpServletRequest request;
  private MockHttpServletResponse response;

  private static final String EMAIL = "test@gmail.com";
  private static final String FIRST_NAME = "John";
  private static final String LAST_NAME = "Doe";
  private static final String TOKEN = "jwt-token-123";
  private static final String AUTH_CODE = "auth-code-xyz";

  @BeforeEach
  void setUp() {
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
    ReflectionTestUtils.setField(handler, "frontendUrl", "");
  }

  // ==================== HELPERS ====================
  private OAuth2User createOAuth2User(String email, String firstName, String lastName) {
    Map<String, Object> attributes = new HashMap<>();
    attributes.put("email", email);
    attributes.put("given_name", firstName);
    attributes.put("family_name", lastName);

    return new DefaultOAuth2User(Collections.singletonList(() -> "ROLE_USER"), attributes, "email");
  }

  private User createUser(String email, String firstName, String lastName) {
    return new User(email, "", firstName, lastName, Role.PATIENT);
  }

  private void mockAuthenticationPrincipal(OAuth2User oAuth2User) {
    when(authentication.getPrincipal()).thenReturn(oAuth2User);
  }

  // ==================== TESTS: MODO DEMO / SWAGGER (frontendUrl Vacío) ====================
  @Test
  void shouldReturnHtmlWithTokenWhenUserExists() throws IOException {
    // Arrange
    OAuth2User oAuth2User = createOAuth2User(EMAIL, FIRST_NAME, LAST_NAME);
    User existingUser = createUser(EMAIL, FIRST_NAME, LAST_NAME);

    mockAuthenticationPrincipal(oAuth2User);
    when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existingUser));

    // Usamos any() para evitar fallos por referencias en el equals
    when(jwtService.generateToken(any(User.class))).thenReturn(TOKEN);

    // Act
    handler.onAuthenticationSuccess(request, response, authentication);

    // Assert
    assertTrue(response.getContentType().contains("text/html"));
    assertTrue(response.getCharacterEncoding().equalsIgnoreCase("UTF-8"));

    String html = response.getContentAsString();

    // Imprime el HTML en consola si falla para ver exactamente qué escribió el handler
    assertTrue(
        html.contains(TOKEN),
        "El HTML devuelto fue: [" + html + "], se esperaba que contuviera: [" + TOKEN + "]");

    verify(userRepository, never()).save(any(User.class));
    verify(jwtService).generateToken(existingUser);
  }

  @Test
  void shouldCreateNewUser_WhenUserDoesNotExist() throws IOException {
    OAuth2User oAuth2User = createOAuth2User(EMAIL, FIRST_NAME, LAST_NAME);
    User newUser = createUser(EMAIL, FIRST_NAME, LAST_NAME);

    mockAuthenticationPrincipal(oAuth2User);
    when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
    when(userRepository.save(any(User.class))).thenReturn(newUser);
    when(jwtService.generateToken(newUser)).thenReturn(TOKEN);

    handler.onAuthenticationSuccess(request, response, authentication);

    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(userCaptor.capture());

    User savedUser = userCaptor.getValue();

    assertEquals(EMAIL, savedUser.getEmail());
    assertEquals(FIRST_NAME, savedUser.getFirstName());
    assertEquals(LAST_NAME, savedUser.getLastName());
    assertEquals("", savedUser.getPassword());
    assertEquals(Role.PATIENT, savedUser.getRole());

    assertTrue(response.getContentType().contains("text/html"));
    assertTrue(response.getContentAsString().contains(TOKEN));
  }

  @Test
  void shouldSaveUserWithNullFirstNameWhenFirstNameNotProvided() throws IOException {
    OAuth2User oAuth2User = createOAuth2User(EMAIL, null, LAST_NAME);
    User newUser = createUser(EMAIL, null, LAST_NAME);

    mockAuthenticationPrincipal(oAuth2User);
    when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
    when(userRepository.save(any(User.class))).thenReturn(newUser);
    when(jwtService.generateToken(newUser)).thenReturn(TOKEN);

    handler.onAuthenticationSuccess(request, response, authentication);

    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(userCaptor.capture());

    assertNull(userCaptor.getValue().getFirstName());
    assertEquals(LAST_NAME, userCaptor.getValue().getLastName());

    assertTrue(response.getContentType().contains("text/html"));
    assertTrue(response.getContentAsString().contains(TOKEN));
  }

  // ==================== TESTS: REDIRECCIÓN FRONTEND (frontendUrl Configurado) ====================

  @Test
  void shouldRedirectToFrontendWithCode_WhenFrontendUrlIsConfigured() throws IOException {
    String frontendUrl = "http://localhost:3000";
    ReflectionTestUtils.setField(handler, "frontendUrl", frontendUrl);

    OAuth2User oAuth2User = createOAuth2User(EMAIL, FIRST_NAME, LAST_NAME);
    User existingUser = createUser(EMAIL, FIRST_NAME, LAST_NAME);

    mockAuthenticationPrincipal(oAuth2User);
    when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existingUser));
    when(authorizationCodeService.generateCode(existingUser)).thenReturn(AUTH_CODE);

    handler.onAuthenticationSuccess(request, response, authentication);

    String expectedRedirectUrl = frontendUrl + "/oauth2/callback?code=" + AUTH_CODE;
    assertEquals(expectedRedirectUrl, response.getRedirectedUrl());
    verify(jwtService, never()).generateToken(any());
  }

  // ==================== TESTS: EXCEPCIONES ====================

  @Test
  void shouldThrowExceptionWhenJwtServiceFails() throws IOException {
    OAuth2User oAuth2User = createOAuth2User(EMAIL, FIRST_NAME, LAST_NAME);
    User existingUser = createUser(EMAIL, FIRST_NAME, LAST_NAME);

    mockAuthenticationPrincipal(oAuth2User);
    when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existingUser));
    when(jwtService.generateToken(existingUser))
        .thenThrow(new RuntimeException("JWT generation error"));

    assertThrows(
        RuntimeException.class,
        () -> handler.onAuthenticationSuccess(request, response, authentication));
  }

  @Test
  void shouldThrowExceptionWhenUserSaveFails() throws IOException {
    OAuth2User oAuth2User = createOAuth2User(EMAIL, FIRST_NAME, LAST_NAME);

    mockAuthenticationPrincipal(oAuth2User);
    when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
    when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("Database error"));

    assertThrows(
        RuntimeException.class,
        () -> handler.onAuthenticationSuccess(request, response, authentication));

    verify(jwtService, never()).generateToken(any());
  }
}
