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

        User user = new User();
        user.setId(1L);
        user.setEmail("test@mail.com");

        User owner = new User();
        owner.setId(1L);

        PatientProfile patient = new PatientProfile(owner);

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("test@mail.com");

        when(userRepository.findByEmail("test@mail.com"))
                .thenReturn(Optional.of(user));

        when(patientRepository.findById(10L))
                .thenReturn(Optional.of(patient));

        assertTrue(patientSecurity.isOwner(10L, authentication));
    }

    @Test
    void shouldReturnFalseWhenCurrentUserDoesNotOwnPatientProfile() {

        User user = new User();
        user.setId(1L);

        User owner = new User();
        owner.setId(2L);

        PatientProfile patient = new PatientProfile(owner);

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("test@mail.com");

        when(userRepository.findByEmail("test@mail.com"))
                .thenReturn(Optional.of(user));

        when(patientRepository.findById(10L))
                .thenReturn(Optional.of(patient));

        assertFalse(patientSecurity.isOwner(10L, authentication));
    }

    @Test
    void shouldReturnFalseWhenPatientProfileDoesNotExist() {

        User user = new User();
        user.setId(1L);

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("test@mail.com");

        when(userRepository.findByEmail("test@mail.com"))
                .thenReturn(Optional.of(user));

        when(patientRepository.findById(10L))
                .thenReturn(Optional.empty());

        assertFalse(patientSecurity.isOwner(10L, authentication));
    }
}