package com.lirium.nutrition.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigIT {

    @Autowired
    private SecurityFilterChain securityFilterChain;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationProvider authenticationProvider;

    @Autowired
    private AuthenticationManager authenticationManager;

    @MockBean
    private ClientRegistrationRepository clientRegistrationRepository;

    @MockBean
    private OAuth2AuthorizedClientService authorizedClientService;

    @Test
    void shouldLoadSecurityBeans() {

        assertThat(securityFilterChain).isNotNull();

        assertThat(passwordEncoder)
                .isNotNull();

        assertThat(authenticationProvider)
                .isInstanceOf(DaoAuthenticationProvider.class);

        assertThat(authenticationManager)
                .isNotNull();
    }

    @Test
    void shouldEncodePasswords() {

        String encoded =
                passwordEncoder.encode("password");

        assertThat(passwordEncoder.matches("password", encoded))
                .isTrue();
    }

    @Test
    void shouldUseBCryptPasswordEncoder() {

        DaoAuthenticationProvider provider =
                (DaoAuthenticationProvider) authenticationProvider;

        assertThat(provider).isNotNull();
    }

}