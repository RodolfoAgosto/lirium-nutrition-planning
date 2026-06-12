package com.lirium.nutrition.service.impl;

import com.lirium.nutrition.dto.request.NutritionPlanTemplateCreateRequestDTO;
import com.lirium.nutrition.dto.request.NutritionPlanTemplateUpdateRequestDTO;
import com.lirium.nutrition.dto.response.NutritionPlanTemplateResponseDTO;
import com.lirium.nutrition.dto.response.NutritionPlanTemplateSummaryDTO;
import com.lirium.nutrition.exception.DuplicateTemplateException;
import com.lirium.nutrition.exception.ResourceNotFoundException;
import com.lirium.nutrition.model.entity.NutritionPlanTemplate;
import com.lirium.nutrition.model.enums.GoalType;
import com.lirium.nutrition.repository.NutritionPlanTemplateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NutritionPlanTemplateServiceImplTest {

    @Mock
    private  NutritionPlanTemplateRepository repository;

    @InjectMocks
    private NutritionPlanTemplateServiceImpl service;

    @Test
    void shouldCreateTemplate() {

        // Given
        NutritionPlanTemplateCreateRequestDTO request =
            new NutritionPlanTemplateCreateRequestDTO(
                    "Weight Loss",
                    "Template for weight loss",
                    GoalType.WEIGHT_LOSS,
                    30,
                    40,
                    30,
                    Set.of()
            );

        NutritionPlanTemplate template =
            NutritionPlanTemplate.of(
                    "Weight Loss",
                    "Template for weight loss",
                    GoalType.WEIGHT_LOSS,
                    30,
                    40,
                    30,
                    Set.of()
            );

        when(repository.existsByName("Weight Loss"))
            .thenReturn(false);

        when(repository.save(any(NutritionPlanTemplate.class)))
            .thenReturn(template);

        // When
        NutritionPlanTemplateResponseDTO result =
            service.create(request);

        // Then
        assertAll(
            () -> assertEquals("Weight Loss", result.name()),
            () -> assertEquals("Template for weight loss", result.description()),
            () -> assertEquals(GoalType.WEIGHT_LOSS, result.targetGoal()),
            () -> assertEquals(30, result.proteinPercentage()),
            () -> assertEquals(40, result.carbPercentage()),
            () -> assertEquals(30, result.fatPercentage())
        );

        verify(repository).existsByName("Weight Loss");
        verify(repository).save(any(NutritionPlanTemplate.class));

    }

    @Test
    void shouldThrowWhenCreatingDuplicateTemplate() {

        // Given
        NutritionPlanTemplateCreateRequestDTO request =
                new NutritionPlanTemplateCreateRequestDTO(
                        "Weight Loss",
                        "Template for weight loss",
                        GoalType.WEIGHT_LOSS,
                        30,
                        40,
                        30,
                        Set.of()
                );

        when(repository.existsByName("Weight Loss"))
                .thenReturn(true);

        // When + Then
        DuplicateTemplateException ex =
                assertThrows(
                        DuplicateTemplateException.class,
                        () -> service.create(request)
                );

        assertTrue(
                ex.getMessage().contains("Template already exists")
        );

        verify(repository)
                .existsByName("Weight Loss");

        verify(repository, never())
                .save(any());

    }

    @Test
    void shouldUpdateTemplateWithMacros() {

        // Given
        NutritionPlanTemplate template =
                NutritionPlanTemplate.of(
                        "Weight Loss",
                        "Template",
                        GoalType.WEIGHT_LOSS,
                        30,
                        40,
                        30,
                        Set.of()
                );

        NutritionPlanTemplateUpdateRequestDTO request =
                new NutritionPlanTemplateUpdateRequestDTO(
                        "Updated Template",
                        "Updated Description",
                        GoalType.WEIGHT_MAINTENANCE,
                        25,
                        50,
                        25,
                        Set.of()
                );

        when(repository.findById(1L))
                .thenReturn(Optional.of(template));

        // When
        NutritionPlanTemplateResponseDTO result =
                service.update(1L, request);

        // Then
        assertAll(
                () -> assertEquals("Updated Template", result.name()),
                () -> assertEquals("Updated Description", result.description()),
                () -> assertEquals(GoalType.WEIGHT_MAINTENANCE, result.targetGoal()),
                () -> assertEquals(25, result.proteinPercentage()),
                () -> assertEquals(50, result.carbPercentage()),
                () -> assertEquals(25, result.fatPercentage())
        );

        verify(repository).findById(1L);
        verify(repository, never()).save(any());
    }

    @Test
    void shouldUpdateTemplateWithoutMacros() {

        // Given
        NutritionPlanTemplate template =
            NutritionPlanTemplate.of(
                    "Weight Loss",
                    "Original Description",
                    GoalType.WEIGHT_LOSS,
                    30,
                    40,
                    30,
                    Set.of()
            );

        NutritionPlanTemplateUpdateRequestDTO request =
            new NutritionPlanTemplateUpdateRequestDTO(
                    "Updated Template",
                    "Updated Description",
                    GoalType.WEIGHT_MAINTENANCE,
                    null,
                    null,
                    null,
                    Set.of()
            );

        when(repository.findById(1L))
            .thenReturn(Optional.of(template));

        // When
        NutritionPlanTemplateResponseDTO result =
            service.update(1L, request);

        // Then
        assertAll(
            () -> assertEquals("Updated Template", result.name()),
            () -> assertEquals("Updated Description", result.description()),
            () -> assertEquals(GoalType.WEIGHT_MAINTENANCE, result.targetGoal()),

            // Deben conservarse los macros originales
            () -> assertEquals(30, result.proteinPercentage()),
            () -> assertEquals(40, result.carbPercentage()),
            () -> assertEquals(30, result.fatPercentage())
         );

        verify(repository).findById(1L);

    }

    @Test
    void shouldThrowWhenOnlySomeMacroPercentagesAreProvided() {

        // Given
        NutritionPlanTemplate template =
                NutritionPlanTemplate.of(
                        "Weight Loss",
                        "Template",
                        GoalType.WEIGHT_LOSS,
                        30,
                        40,
                        30,
                        Set.of()
                );

        NutritionPlanTemplateUpdateRequestDTO request =
                new NutritionPlanTemplateUpdateRequestDTO(
                        "Updated Template",
                        "Updated Description",
                        GoalType.WEIGHT_MAINTENANCE,
                        25,      // protein
                        null,    // carb
                        null,    // fat
                        Set.of()
                );

        when(repository.findById(1L))
                .thenReturn(Optional.of(template));

        // When + Then
        IllegalArgumentException ex =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.update(1L, request)
                );

        assertEquals(
                "All macro percentages must be provided together",
                ex.getMessage()
        );

        verify(repository).findById(1L);
    }

    @Test
    void shouldThrowWhenUpdatingDuplicateName() {

        // Given
        NutritionPlanTemplate template =
                NutritionPlanTemplate.of(
                        "Weight Loss",
                        "Template",
                        GoalType.WEIGHT_LOSS,
                        30,
                        40,
                        30,
                        Set.of()
                );

        NutritionPlanTemplateUpdateRequestDTO request =
                new NutritionPlanTemplateUpdateRequestDTO(
                        "Keto",
                        "Updated Description",
                        GoalType.WEIGHT_MAINTENANCE,
                        null,
                        null,
                        null,
                        Set.of()
                );

        when(repository.findById(1L))
                .thenReturn(Optional.of(template));

        when(repository.existsByName("Keto"))
                .thenReturn(true);

        // When + Then
        assertThrows(
                DuplicateTemplateException.class,
                () -> service.update(1L, request)
        );

        verify(repository).findById(1L);
        verify(repository).existsByName("Keto");
    }

    @Test
    void shouldDeleteTemplate() {

        // Given
        NutritionPlanTemplate template =
                NutritionPlanTemplate.of(
                        "Weight Loss",
                        "Template",
                        GoalType.WEIGHT_LOSS,
                        30,
                        40,
                        30,
                        Set.of()
                );

        when(repository.findById(1L))
                .thenReturn(Optional.of(template));

        // When
        service.delete(1L);

        // Then
        verify(repository).findById(1L);
        verify(repository).delete(template);

    }

    @Test
    void shouldThrowWhenDeletingMissingTemplate() {

        // Given
        when(repository.findById(1L))
                .thenReturn(Optional.empty());

        // When + Then
        assertThrows(
                ResourceNotFoundException.class,
                () -> service.delete(1L)
        );

        verify(repository).findById(1L);
        verify(repository, never()).delete(any());
    }

    @Test
    void shouldReturnTemplateById() {

        // Given
        NutritionPlanTemplate template =
                NutritionPlanTemplate.of(
                        "Weight Loss",
                        "Template for weight loss",
                        GoalType.WEIGHT_LOSS,
                        30,
                        40,
                        30,
                        Set.of()
                );

        when(repository.findById(1L))
                .thenReturn(Optional.of(template));

        // When
        NutritionPlanTemplateResponseDTO result =
                service.getById(1L);

        // Then
        assertAll(
                () -> assertEquals("Weight Loss", result.name()),
                () -> assertEquals("Template for weight loss", result.description()),
                () -> assertEquals(GoalType.WEIGHT_LOSS, result.targetGoal()),
                () -> assertEquals(30, result.proteinPercentage()),
                () -> assertEquals(40, result.carbPercentage()),
                () -> assertEquals(30, result.fatPercentage())
        );

        verify(repository).findById(1L);
    }

    @Test
    void shouldThrowWhenTemplateNotFound() {

        // Given
        when(repository.findById(1L))
                .thenReturn(Optional.empty());

        // When + Then
        assertThrows(
                ResourceNotFoundException.class,
                () -> service.getById(1L)
        );

        verify(repository).findById(1L);
    }

    @Test
    void shouldReturnAllTemplates() {

        // Given
        NutritionPlanTemplate template1 =
                NutritionPlanTemplate.of(
                        "Weight Loss",
                        "Template 1",
                        GoalType.WEIGHT_LOSS,
                        30,
                        40,
                        30,
                        Set.of()
                );

        NutritionPlanTemplate template2 =
                NutritionPlanTemplate.of(
                        "Muscle Gain",
                        "Template 2",
                        GoalType.MUSCLE_GAIN,
                        25,
                        50,
                        25,
                        Set.of()
                );

        when(repository.findAll())
                .thenReturn(List.of(template1, template2));

        // When
        List<NutritionPlanTemplateSummaryDTO> result =
                service.getAll();

        // Then
        assertEquals(2, result.size());

        assertAll(
                () -> assertEquals("Weight Loss", result.get(0).name()),
                () -> assertEquals("Muscle Gain", result.get(1).name())
        );

        verify(repository).findAll();
    }

}