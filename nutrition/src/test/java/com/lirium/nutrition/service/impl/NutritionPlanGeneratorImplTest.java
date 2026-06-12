package com.lirium.nutrition.service.impl;

import com.lirium.nutrition.dto.response.NutritionPlanDetailDTO;
import com.lirium.nutrition.exception.ResourceNotFoundException;
import com.lirium.nutrition.model.entity.NutritionPlan;
import com.lirium.nutrition.model.entity.NutritionPlanTemplate;
import com.lirium.nutrition.model.entity.PatientProfile;
import com.lirium.nutrition.model.enums.FoodTag;
import com.lirium.nutrition.model.enums.PlanStatus;
import com.lirium.nutrition.model.valueobject.Calories;
import com.lirium.nutrition.model.valueobject.MacroDistribution;
import com.lirium.nutrition.repository.NutritionPlanRepository;
import com.lirium.nutrition.repository.NutritionPlanTemplateRepository;
import com.lirium.nutrition.repository.PatientProfileRepository;
import com.lirium.nutrition.service.CalorieCalculator;
import com.lirium.nutrition.service.MacroDistributor;
import com.lirium.nutrition.service.NutritionPlanAssembler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NutritionPlanGeneratorImplTest {

    @Mock
    private CalorieCalculator calorieCalculator;

    @Mock
    private MacroDistributor macroDistributor;

    @Mock
    private PatientProfileRepository repository;

    @Mock
    private NutritionPlanAssembler nutritionPlanAssembler;

    @Mock
    private NutritionPlanRepository nutritionPlanRepository;

    @Mock
    private NutritionPlanTemplateRepository templateRepository;

    @InjectMocks
    private NutritionPlanGeneratorImpl nutritionPlanGenerator;

    @Test
    void shouldThrowWhenPatientNotFound() {

        // Given
        Long patientId = 1L;

        when(repository.findById(patientId))
                .thenReturn(Optional.empty());

        // When - Then
        assertThrows(
                ResourceNotFoundException.class,
                () -> nutritionPlanGenerator.generate(patientId)
        );

        verify(repository).findById(patientId);

        verifyNoInteractions(
                calorieCalculator,
                macroDistributor,
                nutritionPlanAssembler,
                nutritionPlanRepository,
                templateRepository
        );
    }

    @Test
    void shouldThrowWhenDraftPlanAlreadyExists() {

        // Given
        Long patientId = 1L;

        PatientProfile patient =  mock(PatientProfile.class);

        when(repository.findById(patientId))
                .thenReturn(Optional.of(patient));

        when(nutritionPlanRepository.existsByPatientProfileIdAndStatus(
                patientId,
                PlanStatus.DRAFT))
                .thenReturn(true);

        // When - Then
        assertThrows(
                IllegalStateException.class,
                () -> nutritionPlanGenerator.generate(patientId)
        );

        verify(repository).findById(patientId);

        verify(nutritionPlanRepository)
                .existsByPatientProfileIdAndStatus(
                        patientId,
                        PlanStatus.DRAFT);

        verifyNoInteractions(
                calorieCalculator,
                macroDistributor,
                nutritionPlanAssembler,
                templateRepository
        );

        verify(nutritionPlanRepository, never()).save(any());
    }

    @Test
    void shouldGeneratePlanSuccessfully() {

        // Given
        Long patientId = 1L;

        PatientProfile patient = mock(PatientProfile.class);
        Calories calories = mock(Calories.class);
        MacroDistribution macros = mock(MacroDistribution.class);
        NutritionPlan plan = mock(NutritionPlan.class);

        when(repository.findById(patientId))
                .thenReturn(Optional.of(patient));

        when(nutritionPlanRepository.existsByPatientProfileIdAndStatus(
                patientId,
                PlanStatus.DRAFT))
                .thenReturn(false);

        when(calorieCalculator.calculate(patient))
                .thenReturn(calories);

        when(macroDistributor.distribute(patient, calories))
                .thenReturn(macros);

        when(nutritionPlanAssembler.assemble(patient, calories, macros))
                .thenReturn(plan);

        // When
        nutritionPlanGenerator.generate(patientId);

        // Then
        verify(repository).findById(patientId);

        verify(nutritionPlanRepository)
                .existsByPatientProfileIdAndStatus(
                        patientId,
                        PlanStatus.DRAFT);

        verify(calorieCalculator)
                .calculate(patient);

        verify(macroDistributor)
                .distribute(patient, calories);

        verify(nutritionPlanAssembler)
                .assemble(patient, calories, macros);

        verify(nutritionPlanRepository)
                .save(plan);
    }

    @Test
    void shouldThrowWhenPatientNotFoundForTemplateGeneration() {

        // Given
        Long patientId = 1L;
        Long templateId = 10L;

        when(repository.findById(patientId))
                .thenReturn(Optional.empty());

        // When - Then
        assertThrows(
                ResourceNotFoundException.class,
                () -> nutritionPlanGenerator.generateFromTemplate(
                        patientId,
                        templateId)
        );

        verify(repository).findById(patientId);

        verifyNoInteractions(
                calorieCalculator,
                macroDistributor,
                nutritionPlanAssembler,
                nutritionPlanRepository,
                templateRepository
        );
    }

    @Test
    void shouldThrowWhenDraftPlanAlreadyExistsForTemplateGeneration() {

        // Given
        Long patientId = 1L;
        Long templateId = 10L;

        PatientProfile patient = mock(PatientProfile.class);

        when(repository.findById(patientId))
                .thenReturn(Optional.of(patient));

        when(nutritionPlanRepository.existsByPatientProfileIdAndStatus(
                patientId,
                PlanStatus.DRAFT))
                .thenReturn(true);

        // When - Then
        assertThrows(
                IllegalStateException.class,
                () -> nutritionPlanGenerator.generateFromTemplate(
                        patientId,
                        templateId)
        );

        verify(repository).findById(patientId);

        verify(nutritionPlanRepository)
                .existsByPatientProfileIdAndStatus(
                        patientId,
                        PlanStatus.DRAFT);

        verify(nutritionPlanRepository, never())
                .existsByPatientProfileIdAndStatus(
                        patientId,
                        PlanStatus.ACTIVE);

        verifyNoInteractions(
                templateRepository,
                calorieCalculator,
                macroDistributor,
                nutritionPlanAssembler
        );

        verify(nutritionPlanRepository, never())
                .save(any());
    }

    @Test
    void shouldThrowWhenActivePlanAlreadyExistsForTemplateGeneration() {

        // Given
        Long patientId = 1L;
        Long templateId = 10L;

        PatientProfile patient = mock(PatientProfile.class);

        when(repository.findById(patientId))
                .thenReturn(Optional.of(patient));

        when(nutritionPlanRepository.existsByPatientProfileIdAndStatus(
                patientId,
                PlanStatus.DRAFT))
                .thenReturn(false);

        when(nutritionPlanRepository.existsByPatientProfileIdAndStatus(
                patientId,
                PlanStatus.ACTIVE))
                .thenReturn(true);

        // When - Then
        assertThrows(
                IllegalStateException.class,
                () -> nutritionPlanGenerator.generateFromTemplate(
                        patientId,
                        templateId)
        );

        verify(repository).findById(patientId);

        verify(nutritionPlanRepository)
                .existsByPatientProfileIdAndStatus(
                        patientId,
                        PlanStatus.DRAFT);

        verify(nutritionPlanRepository)
                .existsByPatientProfileIdAndStatus(
                        patientId,
                        PlanStatus.ACTIVE);

        verifyNoInteractions(
                templateRepository,
                calorieCalculator,
                macroDistributor,
                nutritionPlanAssembler
        );

        verify(nutritionPlanRepository, never())
                .save(any());
    }

    @Test
    void shouldThrowWhenTemplateNotFound() {
        // Given
        Long patientId = 1L;
        Long templateId = 10L;

        PatientProfile patient = mock(PatientProfile.class);

        given(repository.findById(patientId))
                .willReturn(Optional.of(patient));

        given(nutritionPlanRepository.existsByPatientProfileIdAndStatus(
                patientId,
                PlanStatus.DRAFT))
                .willReturn(false);

        given(nutritionPlanRepository.existsByPatientProfileIdAndStatus(
                patientId,
                PlanStatus.ACTIVE))
                .willReturn(false);

        given(templateRepository.findById(templateId))
                .willReturn(Optional.empty());

        // When / Then
        assertThrows(
                ResourceNotFoundException.class,
                () -> nutritionPlanGenerator.generateFromTemplate(patientId, templateId)
        );

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

        PatientProfile patient = mock(PatientProfile.class);
        NutritionPlanTemplate template = mock(NutritionPlanTemplate.class);

        Calories calories = new Calories(2000);

        MacroDistribution macros = new MacroDistribution(
                150,
                250,
                67
        );

        Set<FoodTag> excludedTags = Set.of(FoodTag.SOY);

        NutritionPlan plan = mock(NutritionPlan.class);

        given(repository.findById(patientId))
                .willReturn(Optional.of(patient));

        given(nutritionPlanRepository.existsByPatientProfileIdAndStatus(
                patientId,
                PlanStatus.DRAFT))
                .willReturn(false);

        given(nutritionPlanRepository.existsByPatientProfileIdAndStatus(
                patientId,
                PlanStatus.ACTIVE))
                .willReturn(false);

        given(templateRepository.findById(templateId))
                .willReturn(Optional.of(template));

        given(calorieCalculator.calculate(patient))
                .willReturn(calories);

        given(macroDistributor.distributeFromTemplate(calories, template))
                .willReturn(macros);

        given(template.getExcludedTags())
                .willReturn(excludedTags);

        given(nutritionPlanAssembler.assemble(
                patient,
                calories,
                macros,
                excludedTags))
                .willReturn(plan);

        // When
        NutritionPlanDetailDTO result =
                nutritionPlanGenerator.generateFromTemplate(patientId, templateId);

        // Then
        assertNotNull(result);

        verify(repository).findById(patientId);

        verify(templateRepository).findById(templateId);

        verify(calorieCalculator).calculate(patient);

        verify(macroDistributor)
                .distributeFromTemplate(calories, template);

        verify(nutritionPlanAssembler)
                .assemble(patient, calories, macros, excludedTags);

        verify(nutritionPlanRepository).save(plan);
    }

    @Test
    void shouldThrowWhenDraftOrActiveExistsInTemplateFlow() {

        Long patientId = 1L;
        Long templateId = 10L;

        PatientProfile patient = mock(PatientProfile.class);

        when(repository.findById(patientId)).thenReturn(Optional.of(patient));

        when(nutritionPlanRepository.existsByPatientProfileIdAndStatus(patientId, PlanStatus.DRAFT))
                .thenReturn(false);

        when(nutritionPlanRepository.existsByPatientProfileIdAndStatus(patientId, PlanStatus.ACTIVE))
                .thenReturn(true);

        assertThrows(IllegalStateException.class,
                () -> nutritionPlanGenerator.generateFromTemplate(patientId, templateId));

        verify(nutritionPlanRepository).existsByPatientProfileIdAndStatus(patientId, PlanStatus.DRAFT);
        verify(nutritionPlanRepository).existsByPatientProfileIdAndStatus(patientId, PlanStatus.ACTIVE);
    }

    @Test
    void shouldGeneratePlanAndPersistCorrectly() {

        Long patientId = 1L;

        PatientProfile patient = mock(PatientProfile.class);
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

        InOrder inOrder = inOrder(repository, calorieCalculator, macroDistributor,
                nutritionPlanAssembler, nutritionPlanRepository);

        inOrder.verify(repository).findById(patientId);
        inOrder.verify(nutritionPlanRepository).existsByPatientProfileIdAndStatus(patientId, PlanStatus.DRAFT);
        inOrder.verify(calorieCalculator).calculate(patient);
        inOrder.verify(macroDistributor).distribute(patient, calories);
        inOrder.verify(nutritionPlanAssembler).assemble(patient, calories, macros);
        inOrder.verify(nutritionPlanRepository).save(plan);
    }

}