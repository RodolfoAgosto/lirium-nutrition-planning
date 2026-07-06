package com.lirium.nutrition.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lirium.nutrition.infrastructure.security.JwtService;
import com.lirium.nutrition.infrastructure.security.OAuth2LoginSuccessHandler;
import com.lirium.nutrition.model.entity.PatientProfile;
import com.lirium.nutrition.model.entity.User;
import com.lirium.nutrition.model.enums.Role;
import com.lirium.nutrition.repository.FoodRepository;
import com.lirium.nutrition.repository.PatientProfileRepository;
import com.lirium.nutrition.repository.RefreshTokenRepository;
import com.lirium.nutrition.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class AbstractIntegrationTest {

    @MockBean
    ClientRegistrationRepository clientRegistrationRepository;

    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    MockMvc mockMvc;
    @Autowired
    JwtService jwtService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    PatientProfileRepository patientRepository;
    @Autowired
    FoodRepository foodRepository;
    @Autowired
    RefreshTokenRepository refreshTokenRepository;
    @Autowired
    OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

}