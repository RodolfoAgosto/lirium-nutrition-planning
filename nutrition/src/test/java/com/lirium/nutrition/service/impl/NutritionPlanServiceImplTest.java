package com.lirium.nutrition.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.lirium.nutrition.dto.request.CompleteNutritionPlanRequestDTO;
import com.lirium.nutrition.dto.response.NutritionPlanDetailDTO;
import com.lirium.nutrition.dto.response.NutritionPlanSummaryDTO;
import com.lirium.nutrition.exception.ResourceNotFoundException;
import com.lirium.nutrition.model.entity.NutritionPlan;
import com.lirium.nutrition.model.entity.PatientProfile;
import com.lirium.nutrition.model.entity.Restriction;
import com.lirium.nutrition.model.entity.User;
import com.lirium.nutrition.model.enums.*;
import com.lirium.nutrition.model.valueobject.Height;
import com.lirium.nutrition.model.valueobject.Weight;
import com.lirium.nutrition.repository.NutritionPlanRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NutritionPlanServiceImplTest {

  @Mock private NutritionPlanRepository repository;

  @InjectMocks private NutritionPlanServiceImpl service;

  private final Clock clock = Clock.fixed(Instant.parse("2025-01-15T10:00:00Z"), ZoneOffset.UTC);

  @BeforeEach
  void setUp() {
    service =
        new NutritionPlanServiceImpl(
            repository,
            // demás dependencias,
            clock);
  }

  @Test
  void shouldCompleteNutritionPlan() {

    // Given
    Long planId = 1L;
    LocalDate today = LocalDate.now(clock);

    NutritionPlan plan =
        NutritionPlan.generate(
            GoalType.WEIGHT_LOSS, 2000, 120, 200, 60, generateUser().getPatientProfile());

    plan.activate(today.minusDays(1));

    CompleteNutritionPlanRequestDTO request =
        new CompleteNutritionPlanRequestDTO("Volume", "Muscle-building plan");

    when(repository.findById(planId)).thenReturn(Optional.of(plan));

    // When
    service.complete(planId, request);

    // Then
    verify(repository).findById(planId);
    assertEquals(PlanStatus.INACTIVE, plan.getStatus());
    assertEquals("Volume", plan.getName());
    assertEquals("Muscle-building plan", plan.getDescription());
  }

  @Test
  void shouldThrowWhenPlanNotFoundInComplete() {

    // Given
    Long planId = 1L;

    CompleteNutritionPlanRequestDTO request =
        new CompleteNutritionPlanRequestDTO("Volume", "Muscle-building plan");

    when(repository.findById(planId)).thenReturn(Optional.empty());

    // When + Then
    RuntimeException ex =
        assertThrows(RuntimeException.class, () -> service.complete(planId, request));

    assertTrue(ex.getMessage().contains("NutritionPlan"));
    assertTrue(ex.getMessage().contains("1"));

    verify(repository).findById(planId);

    verifyNoMoreInteractions(repository);
  }

  @Test
  void shouldActivatePlanWhenNoPreviousActivePlanExists() {

    // Given
    Long planId = 1L;
    Long patientId = 10L;

    PatientProfile patient = mock(PatientProfile.class);
    NutritionPlan newPlan = mock(NutritionPlan.class);

    when(newPlan.getPatientProfile()).thenReturn(patient);
    when(patient.getId()).thenReturn(patientId);

    when(repository.findById(planId)).thenReturn(Optional.of(newPlan));

    when(repository.findByPatientProfileIdAndStatus(patientId, PlanStatus.ACTIVE))
        .thenReturn(Optional.empty());

    // When
    NutritionPlanDetailDTO result = service.activatePlan(planId);

    // Then
    assertNotNull(result);

    verify(repository).findById(planId);

    verify(repository).findByPatientProfileIdAndStatus(patientId, PlanStatus.ACTIVE);

    verify(newPlan).activate(any(LocalDate.class));

    verify(repository).save(newPlan);

    verify(repository, times(1)).save(any());
  }

  @Test
  void shouldClosePreviousPlanAndActivateNewPlan() {

    // Given
    Long planId = 1L;
    Long patientId = 10L;

    PatientProfile patient = mock(PatientProfile.class);

    NutritionPlan previousPlan = mock(NutritionPlan.class);
    NutritionPlan newPlan = mock(NutritionPlan.class);

    when(newPlan.getPatientProfile()).thenReturn(patient);
    when(patient.getId()).thenReturn(patientId);

    when(repository.findById(planId)).thenReturn(Optional.of(newPlan));

    when(repository.findByPatientProfileIdAndStatus(patientId, PlanStatus.ACTIVE))
        .thenReturn(Optional.of(previousPlan));

    // When
    service.activatePlan(planId);

    // Then
    verify(previousPlan).close(any(LocalDate.class));

    verify(repository).save(previousPlan);

    verify(newPlan).activate(any(LocalDate.class));

    verify(repository).save(newPlan);
  }

  @Test
  void shouldThrowWhenPlanNotFoundInActivatePlan() {

    // Given
    Long planId = 1L;

    when(repository.findById(planId)).thenReturn(Optional.empty());

    // When + Then
    assertThrows(ResourceNotFoundException.class, () -> service.activatePlan(planId));

    verify(repository).findById(planId);

    verify(repository, never()).findByPatientProfileIdAndStatus(anyLong(), any());

    verify(repository, never()).save(any());
  }

  @Test
  void shouldThrowWhenNutritionPlanNotFound() {

    // Given
    Long planId = 1L;

    when(repository.findById(planId)).thenReturn(Optional.empty());

    // When + Then
    ResourceNotFoundException ex =
        assertThrows(ResourceNotFoundException.class, () -> service.findById(planId));

    assertTrue(ex.getMessage().contains("NutritionPlan"));
    assertTrue(ex.getMessage().contains(planId.toString()));

    verify(repository).findById(planId);
  }

  @Test
  void shouldReturnPlansForPatient() {

    // Given
    Long patientId = 1L;

    NutritionPlan plan1 = mock(NutritionPlan.class);
    NutritionPlan plan2 = mock(NutritionPlan.class);

    when(repository.findByPatientProfileIdOrderByStartDateDesc(patientId))
        .thenReturn(List.of(plan1, plan2));

    // When
    List<NutritionPlanSummaryDTO> result = service.findByPatient(patientId);

    // Then
    assertAll(() -> assertNotNull(result), () -> assertEquals(2, result.size()));

    verify(repository).findByPatientProfileIdOrderByStartDateDesc(patientId);
  }

  @Test
  void shouldReturnEmptyListWhenPatientHasNoPlans() {

    // Given
    Long patientId = 1L;

    when(repository.findByPatientProfileIdOrderByStartDateDesc(patientId)).thenReturn(List.of());

    // When
    List<NutritionPlanSummaryDTO> result = service.findByPatient(patientId);

    // Then
    assertAll(() -> assertNotNull(result), () -> assertTrue(result.isEmpty()));

    verify(repository).findByPatientProfileIdOrderByStartDateDesc(patientId);
  }

  @Test
  void shouldReturnActivePlan() {

    // Given
    Long patientId = 1L;

    NutritionPlan plan = mock(NutritionPlan.class);

    when(repository.findByPatientProfileIdAndStatus(patientId, PlanStatus.ACTIVE))
        .thenReturn(Optional.of(plan));

    // When
    Optional<NutritionPlan> result = service.findActivePlan(patientId);

    // Then
    assertTrue(result.isPresent());
    assertEquals(plan, result.get());

    verify(repository).findByPatientProfileIdAndStatus(patientId, PlanStatus.ACTIVE);
  }

  @Test
  void shouldReturnEmptyOptionalWhenNoActivePlanExists() {

    // Given
    Long patientId = 1L;

    when(repository.findByPatientProfileIdAndStatus(patientId, PlanStatus.ACTIVE))
        .thenReturn(Optional.empty());

    // When
    Optional<NutritionPlan> result = service.findActivePlan(patientId);

    // Then
    assertTrue(result.isEmpty());

    verify(repository).findByPatientProfileIdAndStatus(patientId, PlanStatus.ACTIVE);
  }

  @Test
  void shouldReturnTrueWhenPlanBelongsToPatient() {

    Long planId = 1L;
    Long patientId = 10L;

    User user = generateUser();
    user.setId(10L);

    PatientProfile patientProfile = user.getPatientProfile();

    NutritionPlan plan =
        NutritionPlan.generate(GoalType.WEIGHT_MAINTENANCE, 2000, 150, 200, 60, patientProfile);

    when(repository.findById(planId)).thenReturn(Optional.of(plan));

    boolean result = service.belongsToPatient(planId, patientId);

    assertTrue(result);

    verify(repository).findById(planId);
  }

  @Test
  void shouldReturnFalseWhenPlanBelongsToAnotherPatient() {

    Long planId = 1L;
    Long patientId = 10L;

    User user = generateUser();
    user.setId(99L); // distinto

    PatientProfile patientProfile = user.getPatientProfile();

    NutritionPlan plan =
        NutritionPlan.generate(GoalType.WEIGHT_MAINTENANCE, 2000, 150, 200, 60, patientProfile);

    when(repository.findById(planId)).thenReturn(Optional.of(plan));

    boolean result = service.belongsToPatient(planId, patientId);

    assertFalse(result);

    verify(repository).findById(planId);
  }

  @Test
  void shouldReturnFalseWhenPlanNotFound() {

    Long planId = 1L;
    Long patientId = 10L;

    when(repository.findById(planId)).thenReturn(Optional.empty());

    boolean result = service.belongsToPatient(planId, patientId);

    assertFalse(result);

    verify(repository).findById(planId);
  }

  private User generateUser() {

    User user = new User("john@test.com", "hash", "John", "Doe", Role.PATIENT);

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
        GoalType.WEIGHT_MAINTENANCE);

    Restriction restriction = mock(Restriction.class);

    profile.addRestriction(restriction);

    return user;
  }
}
