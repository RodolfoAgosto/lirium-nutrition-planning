package com.lirium.nutrition.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.JwtException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private FilterChain filterChain;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private UserDetails userDetails;

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldContinueFilterChainWhenAuthorizationHeaderIsMissing()
            throws Exception {

        when(request.getHeader("Authorization"))
                .thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);

        verifyNoInteractions(jwtService);
    }

    @Test
    void shouldContinueFilterChainWhenAuthorizationHeaderDoesNotStartWithBearer()
            throws Exception {

        when(request.getHeader("Authorization"))
                .thenReturn("Basic 123");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);

        verifyNoInteractions(jwtService);
    }

    @Test
    void shouldContinueFilterChainWhenTokenIsInvalid()
            throws Exception {

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer invalid-token");

        when(jwtService.extractUsername("invalid-token"))
                .thenThrow(new IllegalArgumentException("invalid"));

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);

        verifyNoInteractions(userDetailsService);
    }

    @Test
    void shouldAuthenticateUserWhenTokenIsValid()
            throws Exception {

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer token");

        when(jwtService.extractUsername("token"))
                .thenReturn("john@test.com");

        when(userDetailsService.loadUserByUsername("john@test.com"))
                .thenReturn(userDetails);

        when(jwtService.isTokenValid("token", userDetails))
                .thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        assertNotNull(authentication);
        assertEquals(userDetails, authentication.getPrincipal());

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotAuthenticateWhenTokenValidationFails()
            throws Exception {

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer token");

        when(jwtService.extractUsername("token"))
                .thenReturn("john@test.com");

        when(userDetailsService.loadUserByUsername("john@test.com"))
                .thenReturn(userDetails);

        when(jwtService.isTokenValid("token", userDetails))
                .thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(
                SecurityContextHolder.getContext().getAuthentication()
        );

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldSkipAuthenticationWhenSecurityContextAlreadyContainsAuthentication()
            throws Exception {

        Authentication existingAuth =
                mock(Authentication.class);

        SecurityContextHolder.getContext()
                .setAuthentication(existingAuth);

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer token");

        when(jwtService.extractUsername("token"))
                .thenReturn("john@test.com");

        filter.doFilterInternal(request, response, filterChain);

        verifyNoInteractions(userDetailsService);

        verify(filterChain).doFilter(request, response);
    }
}