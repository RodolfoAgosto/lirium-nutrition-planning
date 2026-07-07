package com.lirium.nutrition.infrastructure.security;

import com.lirium.nutrition.model.entity.PatientProfile;
import com.lirium.nutrition.model.entity.User;
import com.lirium.nutrition.repository.PatientProfileRepository;
import com.lirium.nutrition.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientSecurityTest {

    @InjectMocks
    private PatientSecurity patientSecurity;

    @Mock
    private PatientProfileRepository patientRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetails userDetails;

    @Test
    void shouldReturnTrueWhenCurrentUserOwnsPatientProfile() {
        User principal = new User();
        principal.setId(10L);
        when(authentication.getPrincipal()).thenReturn(principal);
        assertTrue(patientSecurity.isOwner(10L, authentication));
    }

    @Test
    void shouldReturnFalseWhenCurrentUserDoesNotOwnPatientProfile() {

        User principal = new User();
        principal.setId(1L);
        when(authentication.getPrincipal()).thenReturn(principal);
        assertFalse(patientSecurity.isOwner(10L, authentication));

    }

}