package com.lirium.nutrition.service.impl;

import com.lirium.nutrition.dto.request.PatientUpdateRequestDTO;
import com.lirium.nutrition.dto.request.RestrictionUpdateDTO;
import com.lirium.nutrition.dto.response.PatientDetailsDTO;
import com.lirium.nutrition.dto.response.PatientProfileResponseDTO;
import com.lirium.nutrition.dto.response.PatientSummaryDTO;
import com.lirium.nutrition.dto.response.RestrictionSummaryDTO;
import com.lirium.nutrition.exception.ResourceNotFoundException;
import com.lirium.nutrition.mapper.PatientProfileMapper;
import com.lirium.nutrition.mapper.RestrictionMapper;
import com.lirium.nutrition.model.entity.PatientProfile;
import com.lirium.nutrition.model.entity.Restriction;
import com.lirium.nutrition.model.entity.User;
import com.lirium.nutrition.model.enums.*;
import com.lirium.nutrition.model.valueobject.Height;
import com.lirium.nutrition.model.valueobject.Weight;
import com.lirium.nutrition.repository.PatientProfileRepository;
import com.lirium.nutrition.repository.RestrictionRepository;
import com.lirium.nutrition.service.PatientProfileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceImplTest {

    @Mock
    private PatientProfileService patientProfileService;

    @Mock
    private PatientProfileRepository patientProfileRepository;

    @Mock
    private RestrictionRepository restrictionRepository;

    @Mock
    private RestrictionMapper restrictionMapper;

    @InjectMocks
    private PatientServiceImpl patientService;

    @Test
    void shouldSearchPatients() {

        // Given
        PatientSummaryDTO dto = mock(PatientSummaryDTO.class);

        when(patientProfileRepository.searchPatients(
                "John",
                "Doe",
                "john@test.com",
                "12345678"))
                .thenReturn(List.of(dto));

        // When
        List<PatientSummaryDTO> result =
                patientService.searchPatients(
                        "John",
                        "Doe",
                        "john@test.com",
                        "12345678");

        // Then
        assertEquals(1, result.size());

        verify(patientProfileRepository)
                .searchPatients(
                        "John",
                        "Doe",
                        "john@test.com",
                        "12345678");
    }

    @Test
    void shouldThrowWhenPatientNotFound() {

        // Given
        Long id = 21L;

        when(patientProfileRepository.findById(id))
                .thenReturn(Optional.empty());

        // When + Then
        ResourceNotFoundException ex =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> patientService.getPatientDetail(id)
                );

        assertTrue(ex.getMessage().contains("Patient not found"));

        verify(patientProfileRepository).findById(id);
    }

    @Test
    void shouldReturnPatientDetails() {

        // Given
        User user = generateUser();

        Restriction restriction =
                user.getPatientProfile()
                        .getRestrictions()
                        .iterator()
                        .next();

        RestrictionSummaryDTO restrictionDto =
                mock(RestrictionSummaryDTO.class);

        when(patientProfileRepository.findById(1L))
                .thenReturn(Optional.of(user.getPatientProfile()));

        when(restrictionMapper.toSummaryDTO(restriction))
                .thenReturn(restrictionDto);

        // When
        PatientDetailsDTO result =
                patientService.getPatientDetail(1L);

        // Then
        assertAll(
                () -> assertEquals("John", result.firstName()),
                () -> assertEquals("Doe", result.lastName()),
                () -> assertEquals("john@test.com", result.email()),
                () -> assertEquals(Sex.MALE, result.sex()),
                () -> assertEquals(ActivityLevel.MODERATE, result.activityLevel()),
                () -> assertEquals(GoalType.WEIGHT_MAINTENANCE, result.goal())
        );

        verify(patientProfileRepository).findById(1L);
        verify(restrictionMapper).toSummaryDTO(restriction);
    }

    @Test
    void shouldThrowWhenPatientUpdateRequestIsNull() {

        // When + Then
        NullPointerException ex =
                assertThrows(
                        NullPointerException.class,
                        () -> patientService.updatePatient(1L, null)
                );

        assertEquals(
                "PatientUpdateRequestDTO must not be null",
                ex.getMessage()
        );

        verifyNoInteractions(patientProfileService);
    }

    @Test
    void shouldResolveRestrictionsWhenNull() {

        // Given
        PatientProfile profile =
                generateUser().getPatientProfile();

        when(patientProfileService.findByUserId(1L))
                .thenReturn(profile);

        PatientUpdateRequestDTO request =
                new PatientUpdateRequestDTO(
                        "Johny",
                        "Doey",
                        "johny@test.com",
                        "12345677",
                        Sex.MALE,
                        true,
                        LocalDate.of(1990,1,1),
                        180,
                        80000,
                        ActivityLevel.MODERATE,
                        GoalType.WEIGHT_MAINTENANCE,
                        "notes",
                        null,
                        List.of()
                );

        // When
        patientService.updatePatient(1L, request);

        // Then
        verifyNoInteractions(restrictionRepository);
    }

    @Test
    void shouldResolveRestrictionsWhenEmpty() {

        // Given
        PatientProfile profile =
                generateUser().getPatientProfile();

        when(patientProfileService.findByUserId(1L))
                .thenReturn(profile);

        PatientUpdateRequestDTO request =
                new PatientUpdateRequestDTO(
                        "Johny",
                        "Doey",
                        "johny@test.com",
                        "12345677",
                        Sex.MALE,
                        true,
                        LocalDate.of(1990,1,1),
                        180,
                        80000,
                        ActivityLevel.MODERATE,
                        GoalType.WEIGHT_MAINTENANCE,
                        "notes",
                        Set.of(),
                        List.of()
                );

        // When
        patientService.updatePatient(1L, request);

        // Then
        verify(restrictionRepository, never())
                .findByCodes(any());
    }

    @Test
    void shouldResolveRestrictionsWhenValidSet() {

        // Given
        PatientProfile profile =
                generateUser().getPatientProfile();

        Restriction gluten = mock(Restriction.class);

        when(patientProfileService.findByUserId(1L))
                .thenReturn(profile);

        when(restrictionRepository.findByCodes(any()))
                .thenReturn(Set.of(gluten));

        when(restrictionMapper.toDTOSet(any()))
                .thenReturn(Set.of());

        // When
        patientService.updatePatient(
                1L,
                generateUpdateRequest()
        );

        // Then
        verify(restrictionRepository)
                .findByCodes(any());
    }

    @Test
    void shouldThrowWhenRestrictionCodeNotFound() {

        // Given
        User user = generateUser();

        Restriction gluten = mock(Restriction.class);

        when(gluten.getCode())
                .thenReturn("GLUTEN");

        when(patientProfileService.findByUserId(1L))
                .thenReturn(user.getPatientProfile());

        when(restrictionRepository.findByCodes(any()))
                .thenReturn(Set.of(gluten));

        PatientUpdateRequestDTO request =
                new PatientUpdateRequestDTO(
                        "John",
                        "Doe",
                        "john@test.com",
                        "12345678",
                        Sex.MALE,
                        true,
                        LocalDate.of(1990,1,1),
                        180,
                        80000,
                        ActivityLevel.MODERATE,
                        GoalType.WEIGHT_MAINTENANCE,
                        "notes",
                        Set.of(
                                new RestrictionUpdateDTO("GLUTEN"),
                                new RestrictionUpdateDTO("LACTOSE")
                        ),
                        List.of()
                );

        // When + Then
        ResourceNotFoundException ex =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> patientService.updatePatient(1L, request)
                );

        assertTrue(
                ex.getMessage().contains("Restrictions not found")
        );
    }

    @Test
    void shouldUpdatePatientSuccessfully() {

        // Given
        User user = generateUser();

        PatientProfile profile =
                user.getPatientProfile();

        Restriction gluten = mock(Restriction.class);

        RestrictionSummaryDTO dto =
                mock(RestrictionSummaryDTO.class);

        when(patientProfileService.findByUserId(1L))
                .thenReturn(profile);

        when(restrictionRepository.findByCodes(any()))
                .thenReturn(Set.of(gluten));

        when(restrictionMapper.toDTOSet(any()))
                .thenReturn(Set.of(dto));

        // When
        PatientDetailsDTO result =
                patientService.updatePatient(
                        1L,
                        generateUpdateRequest()
                );

        // Then
        assertAll(
                () -> assertEquals("Johny", result.firstName()),
                () -> assertEquals("Doey", result.lastName()),
                () -> assertEquals("johny@test.com", result.email()),
                () -> assertEquals("12345677", result.dni())
        );

        assertAll(
                () -> assertEquals("Johny", user.getFirstName()),
                () -> assertEquals("Doey", user.getLastName()),
                () -> assertEquals("johny@test.com", user.getEmail()),
                () -> assertEquals("12345677", user.getDni()),
                () -> assertFalse(user.getEmailValidated())
        );

        verify(patientProfileService).findByUserId(1L);
        verify(restrictionRepository).findByCodes(any());
        verify(restrictionMapper).toDTOSet(any());
    }

    private User generateUser(){

        User user = new User(
                "john@test.com",
                "hash",
                "John",
                "Doe",
                Role.PATIENT
        );

        user.setDni("12345678");

        PatientProfile profile = user.getPatientProfile();

        profile.update(
                Sex.MALE,
                ActivityLevel.MODERATE,
                Weight.of(80000),
                Height.of(180),
                "notes",
                Set.of(),
                List.of(),
                GoalType.WEIGHT_MAINTENANCE
        );

        Restriction restriction = mock(Restriction.class);

        profile.addRestriction(restriction);

        return user;
    }

    private PatientUpdateRequestDTO generateUpdateRequest(){
        return new PatientUpdateRequestDTO(
                    "Johny",
                    "Doey",
                    "johny@test.com",
                    "12345677",
                    Sex.MALE,
                    true,
                    LocalDate.of(1990, 1, 1),
                    180,
                    80000,
                    ActivityLevel.MODERATE,
                    GoalType.WEIGHT_MAINTENANCE,
                    "updated notes",
                    Set.of(new RestrictionUpdateDTO("GLUTEN")),
                    List.of()
            );
    }

}