package com.lirium.nutrition.service.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import com.lirium.nutrition.dto.response.NutritionPlanDetailDTO;
import com.lirium.nutrition.exception.NutritionPlanTemplateNotFoundException;
import com.lirium.nutrition.exception.PatientProfileNotFoundException;
import com.lirium.nutrition.exception.PlanConflictException;
import com.lirium.nutrition.exception.UnprocessableEntityException;
import com.lirium.nutrition.model.entity.NutritionPlan;
import com.lirium.nutrition.model.entity.NutritionPlanTemplate;
import com.lirium.nutrition.model.entity.PatientProfile;
import com.lirium.nutrition.model.entity.User;
import com.lirium.nutrition.model.enums.ActivityLevel;
import com.lirium.nutrition.model.enums.FoodTag;
import com.lirium.nutrition.model.enums.GoalType;
import com.lirium.nutrition.model.enums.PlanStatus;
import com.lirium.nutrition.model.enums.Sex;
import com.lirium.nutrition.model.valueobject.Calories;
import com.lirium.nutrition.model.valueobject.Height;
import com.lirium.nutrition.model.valueobject.MacroDistribution;
import com.lirium.nutrition.model.valueobject.Weight;
import com.lirium.nutrition.repository.NutritionPlanRepository;
import com.lirium.nutrition.repository.NutritionPlanTemplateRepository;
import com.lirium.nutrition.repository.PatientProfileRepository;
import com.lirium.nutrition.service.CalorieCalculator;
import com.lirium.nutrition.service.MacroDistributor;
import com.lirium.nutrition.service.NutritionPlanAssembler;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NutritionPlanGeneratorImplTest {

  @Mock private CalorieCalculator calorieCalculator;

  @Mock private MacroDistributor macroDistributor;

  @Mock private PatientProfileRepository repository;

  @Mock private NutritionPlanAssembler nutritionPlanAssembler;

  @Mock private NutritionPlanRepository nutritionPlanRepository;

  @Mock private NutritionPlanTemplateRepository templateRepository;

  @InjectMocks private NutritionPlanGeneratorImpl nutritionPlanGenerator;

  @Test
  void shouldThrowWhenPatientNotFound() {

    // Given
    Long patientId = 1L;

    when(repository.findById(patientId)).thenReturn(Optional.empty());

    // When - Then
    assertThrows(
        PatientProfileNotFoundException.class, () -> nutritionPlanGenerator.generate(patientId));

    verify(repository).findById(patientId);

    verifyNoInteractions(
        calorieCalculator,
        macroDistributor,
        nutritionPlanAssembler,
        nutritionPlanRepository,
        templateRepository);
  }

  @Test
  void shouldThrowWhenDraftPlanAlreadyExists() {

    // Given
    Long patientId = 1L;

    PatientProfile patient = mock(PatientProfile.class);

    when(repository.findById(patientId)).thenReturn(Optional.of(patient));

    when(nutritionPlanRepository.existsByPatientProfileIdAndStatus(patientId, PlanStatus.DRAFT))
        .thenReturn(true);

    // When - Then
    assertThrows(PlanConflictException.class, () -> nutritionPlanGenerator.generate(patientId));

    verify(repository).findById(patientId);

    verify(nutritionPlanRepository).existsByPatientProfileIdAndStatus(patientId, PlanStatus.DRAFT);

    verifyNoInteractions(
        calorieCalculator, macroDistributor, nutritionPlanAssembler, templateRepository);

    verify(nutritionPlanRepository, never()).save(any());
  }

  @Test
  void shouldRejectProfileWithoutBirthDate() {

    // Given
    Long patientId = 1L;
    PatientProfile patient = completePatient(patientId, 70000);
    patient.getUser().setBirthDate(null);

    when(repository.findById(patientId)).thenReturn(Optional.of(patient));
    when(nutritionPlanRepository.existsByPatientProfileIdAndStatus(patientId, PlanStatus.DRAFT))
        .thenReturn(false);

    // When - Then
    UnprocessableEntityException ex =
        assertThrows(
            UnprocessableEntityException.class, () -> nutritionPlanGenerator.generate(patientId));

    assertTrue(ex.getMessage().contains("birth date"));
    verifyNoInteractions(calorieCalculator, macroDistributor, nutritionPlanAssembler);
    verify(nutritionPlanRepository, never()).save(any());
  }

  @Test
  void shouldRejectProfileWithoutSex() {

    // Given
    Long patientId = 1L;
    User user = new User();
    user.setId(patientId);
    user.setBirthDate(LocalDate.of(1990, 1, 1));

    PatientProfile patient = new PatientProfile(user);
    patient.updateNutritionProfile(
        Height.of(175), Weight.of(70000), ActivityLevel.MODERATE, GoalType.WEIGHT_MAINTENANCE);

    when(repository.findById(patientId)).thenReturn(Optional.of(patient));
    when(nutritionPlanRepository.existsByPatientProfileIdAndStatus(patientId, PlanStatus.DRAFT))
        .thenReturn(false);

    // When - Then
    UnprocessableEntityException ex =
        assertThrows(
            UnprocessableEntityException.class, () -> nutritionPlanGenerator.generate(patientId));

    assertTrue(ex.getMessage().contains("sex"));
    verifyNoInteractions(calorieCalculator, macroDistributor, nutritionPlanAssembler);
  }

  @Test
  void shouldRejectIncompleteProfileForTemplateGeneration() {

    // Given
    Long patientId = 1L;
    Long templateId = 10L;
    PatientProfile patient = completePatient(patientId, 70000);
    patient.getUser().setBirthDate(null);

    given(repository.findById(patientId)).willReturn(Optional.of(patient));
    given(nutritionPlanRepository.existsByPatientProfileIdAndStatus(patientId, PlanStatus.DRAFT))
        .willReturn(false);
    given(templateRepository.findById(templateId))
        .willReturn(Optional.of(mock(NutritionPlanTemplate.class)));

    // When - Then
    assertThrows(
        UnprocessableEntityException.class,
        () -> nutritionPlanGenerator.generateFromTemplate(patientId, templateId));

    verifyNoInteractions(calorieCalculator, macroDistributor, nutritionPlanAssembler);
    verify(nutritionPlanRepository, never()).save(any());
  }

  @Test
  void shouldGeneratePlanSuccessfully() {

    // Given
    Long patientId = 1L;

    PatientProfile patient = completePatient(patientId, 70000);

    Calories calories = mock(Calories.class);
    MacroDistribution macros = mock(MacroDistribution.class);
    NutritionPlan plan = mock(NutritionPlan.class);

    when(repository.findById(patientId)).thenReturn(Optional.of(patient));

    when(nutritionPlanRepository.existsByPatientProfileIdAndStatus(patientId, PlanStatus.DRAFT))
        .thenReturn(false);

    when(calorieCalculator.calculate(patient)).thenReturn(calories);

    when(macroDistributor.distribute(patient, calories)).thenReturn(macros);

    when(nutritionPlanAssembler.assemble(patient, calories, macros)).thenReturn(plan);

    // When
    nutritionPlanGenerator.generate(patientId);

    // Then
    verify(repository).findById(patientId);

    verify(nutritionPlanRepository).existsByPatientProfileIdAndStatus(patientId, PlanStatus.DRAFT);

    verify(calorieCalculator).calculate(patient);

    verify(macroDistributor).distribute(patient, calories);

    verify(nutritionPlanAssembler).assemble(patient, calories, macros);

    verify(nutritionPlanRepository).save(plan);
  }

  @Test
  void shouldThrowWhenPatientNotFoundForTemplateGeneration() {

    // Given
    Long patientId = 1L;
    Long templateId = 10L;

    when(repository.findById(patientId)).thenReturn(Optional.empty());

    // When - Then
    assertThrows(
        PatientProfileNotFoundException.class,
        () -> nutritionPlanGenerator.generateFromTemplate(patientId, templateId));

    verify(repository).findById(patientId);

    verifyNoInteractions(
        calorieCalculator,
        macroDistributor,
        nutritionPlanAssembler,
        nutritionPlanRepository,
        templateRepository);
  }

  @Test
  void shouldThrowWhenDraftPlanAlreadyExistsForTemplateGeneration() {

    // Given
    Long patientId = 1L;
    Long templateId = 10L;

    PatientProfile patient = mock(PatientProfile.class);

    when(repository.findById(patientId)).thenReturn(Optional.of(patient));

    when(nutritionPlanRepository.existsByPatientProfileIdAndStatus(patientId, PlanStatus.DRAFT))
        .thenReturn(true);

    // When - Then
    assertThrows(
        PlanConflictException.class,
        () -> nutritionPlanGenerator.generateFromTemplate(patientId, templateId));

    verify(repository).findById(patientId);

    verify(nutritionPlanRepository).existsByPatientProfileIdAndStatus(patientId, PlanStatus.DRAFT);

    verify(nutritionPlanRepository, never())
        .existsByPatientProfileIdAndStatus(patientId, PlanStatus.ACTIVE);

    verifyNoInteractions(
        templateRepository, calorieCalculator, macroDistributor, nutritionPlanAssembler);

    verify(nutritionPlanRepository, never()).save(any());
  }

  @Test
  void shouldThrowWhenTemplateNotFound() {
    // Given
    Long patientId = 1L;
    Long templateId = 10L;

    PatientProfile patient = mock(PatientProfile.class);

    given(repository.findById(patientId)).willReturn(Optional.of(patient));

    given(nutritionPlanRepository.existsByPatientProfileIdAndStatus(patientId, PlanStatus.DRAFT))
        .willReturn(false);

    given(templateRepository.findById(templateId)).willReturn(Optional.empty());

    // When / Then
    assertThrows(
        NutritionPlanTemplateNotFoundException.class,
        () -> nutritionPlanGenerator.generateFromTemplate(patientId, templateId));

    verify(templateRepository).findById(templateId);

    verify(nutritionPlanRepository, never()).save(any());

    verifyNoInteractions(calorieCalculator);
    verifyNoInteractions(macroDistributor);
    verifyNoInteractions(nutritionPlanAssembler);
  }

  @Test
  void shouldGeneratePlanFromTemplateSuccessfully() {
    // Given
    Long patientId = 1L;
    Long templateId = 10L;

    PatientProfile patient = completePatient(patientId, 70000);

    NutritionPlanTemplate template = mock(NutritionPlanTemplate.class);

    Calories calories = new Calories(2000);

    MacroDistribution macros = new MacroDistribution(150, 250, 67);

    Set<FoodTag> excludedTags = Set.of(FoodTag.SOY);

    NutritionPlan plan = mock(NutritionPlan.class);

    given(repository.findById(patientId)).willReturn(Optional.of(patient));

    given(nutritionPlanRepository.existsByPatientProfileIdAndStatus(patientId, PlanStatus.DRAFT))
        .willReturn(false);

    given(templateRepository.findById(templateId)).willReturn(Optional.of(template));

    given(calorieCalculator.calculate(patient)).willReturn(calories);

    given(macroDistributor.distributeFromTemplate(calories, template)).willReturn(macros);

    given(template.getExcludedTags()).willReturn(excludedTags);

    given(template.getName()).willReturn("Keto template");

    given(nutritionPlanAssembler.assemble(patient, calories, macros, excludedTags))
        .willReturn(plan);

    // When
    NutritionPlanDetailDTO result =
        nutritionPlanGenerator.generateFromTemplate(patientId, templateId);

    // Then
    assertNotNull(result);

    verify(repository).findById(patientId);

    verify(templateRepository).findById(templateId);

    verify(calorieCalculator).calculate(patient);

    verify(macroDistributor).distributeFromTemplate(calories, template);

    verify(nutritionPlanAssembler).assemble(patient, calories, macros, excludedTags);

    verify(plan).rename("Keto template");

    verify(nutritionPlanRepository).save(plan);

    // An ACTIVE plan does not block generation: the new draft replaces it on activation
    verify(nutritionPlanRepository, never())
        .existsByPatientProfileIdAndStatus(patientId, PlanStatus.ACTIVE);
  }

  @Test
  void shouldGeneratePlanAndPersistCorrectly() {

    Long patientId = 1L;

    PatientProfile patient = completePatient(patientId, 75000);

    Calories calories = new Calories(2000);
    MacroDistribution macros = new MacroDistribution(150, 250, 67);
    NutritionPlan plan = mock(NutritionPlan.class);

    when(repository.findById(patientId)).thenReturn(Optional.of(patient));
    when(nutritionPlanRepository.existsByPatientProfileIdAndStatus(patientId, PlanStatus.DRAFT))
        .thenReturn(false);

    when(calorieCalculator.calculate(patient)).thenReturn(calories);
    when(macroDistributor.distribute(patient, calories)).thenReturn(macros);
    when(nutritionPlanAssembler.assemble(patient, calories, macros)).thenReturn(plan);

    nutritionPlanGenerator.generate(patientId);

    InOrder inOrder =
        inOrder(
            repository,
            calorieCalculator,
            macroDistributor,
            nutritionPlanAssembler,
            nutritionPlanRepository);

    inOrder.verify(repository).findById(patientId);
    inOrder
        .verify(nutritionPlanRepository)
        .existsByPatientProfileIdAndStatus(patientId, PlanStatus.DRAFT);
    inOrder.verify(calorieCalculator).calculate(patient);
    inOrder.verify(macroDistributor).distribute(patient, calories);
    inOrder.verify(nutritionPlanAssembler).assemble(patient, calories, macros);
    inOrder.verify(nutritionPlanRepository).save(plan);
  }

  /** A patient with every field the energy calculation needs. */
  private PatientProfile completePatient(Long patientId, int grams) {
    User user = new User();
    user.setId(patientId);
    user.setBirthDate(LocalDate.of(1990, 1, 1));

    PatientProfile patient = new PatientProfile(user);
    patient.update(Sex.FEMALE, null, null, null, null, null, null, null);
    patient.updateNutritionProfile(
        Height.of(175), Weight.of(grams), ActivityLevel.MODERATE, GoalType.WEIGHT_MAINTENANCE);
    return patient;
  }
}
