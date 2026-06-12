package com.lirium.nutrition.service.impl;

import com.lirium.nutrition.dto.response.PatientProfileResponseDTO;
import com.lirium.nutrition.exception.ResourceNotFoundException;
import com.lirium.nutrition.mapper.PatientProfileMapper;
import com.lirium.nutrition.model.entity.PatientProfile;
import com.lirium.nutrition.model.entity.User;
import com.lirium.nutrition.model.enums.ActivityLevel;
import com.lirium.nutrition.model.enums.GoalType;
import com.lirium.nutrition.model.enums.Role;
import com.lirium.nutrition.model.enums.Sex;
import com.lirium.nutrition.model.valueobject.Height;
import com.lirium.nutrition.model.valueobject.Weight;
import com.lirium.nutrition.repository.PatientProfileRepository;
import com.lirium.nutrition.service.PatientProfileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientProfileServiceImplTest {

    @Mock
    private PatientProfileRepository patientProfileRepository;

    @Mock
    private PatientProfileMapper patientProfileMapper;

    @InjectMocks
    private PatientProfileServiceImpl patientProfileService;

    @Test
    void shouldThrowWhenPatientProfileNotFound() {

        // Given
        when(patientProfileRepository.findByUserIdFetchUser(1L))
                .thenReturn(Optional.empty());

        // When + Then
        ResourceNotFoundException ex =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> patientProfileService.findByUserId(1L)
                );

        assertTrue(
                ex.getMessage().contains("Patient profile not found")
        );

        verify(patientProfileRepository)
                .findByUserIdFetchUser(1L);

        verifyNoInteractions(patientProfileMapper);
    }

    @Test
    void shouldReturnPatientProfileWhenFound() {

        // Given
        PatientProfile patientProfile = mock(PatientProfile.class);

        when(patientProfileRepository.findByUserIdFetchUser(1L))
                .thenReturn(Optional.of(patientProfile));

        // When
        PatientProfile result =
                patientProfileService.findByUserId(1L);

        // Then
        assertSame(patientProfile, result);

        verify(patientProfileRepository)
                .findByUserIdFetchUser(1L);

        verifyNoInteractions(patientProfileMapper);
    }

    @Test
    void shouldUpdatePatientProfileSuccessfully() {

        // Given
        User user = new User(
                "test@test.com",
                "password",
                "Rodolfo",
                "Agosto",
                Role.PATIENT
        );

        PatientProfile patientProfile = new PatientProfile(user);

        patientProfile.update(
                Sex.MALE,
                ActivityLevel.SEDENTARY,
                Weight.of(80000),
                Height.of(180),
                "notes",
                Set.of(),
                List.of(),
                GoalType.MUSCLE_GAIN
        );

        when(patientProfileRepository.save(patientProfile))
                .thenReturn(patientProfile);

        // When
        PatientProfileResponseDTO result =
                patientProfileService.update(patientProfile);

        // Then
        assertNotNull(result);

        verify(patientProfileRepository)
                .save(patientProfile);
    }


}