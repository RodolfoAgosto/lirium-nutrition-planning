package com.lirium.nutrition.service.impl;

import com.lirium.nutrition.dto.request.PlanMealCreateRequestDTO;
import com.lirium.nutrition.dto.response.PlanMealResponseDTO;
import com.lirium.nutrition.exception.ResourceNotFoundException;
import com.lirium.nutrition.mapper.PlanMealMapper;
import com.lirium.nutrition.model.entity.DailyPlan;
import com.lirium.nutrition.model.entity.PlanMeal;
import com.lirium.nutrition.repository.DailyPlanRepository;
import com.lirium.nutrition.repository.PlanMealRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlanMealServiceImplTest {

    @Mock
    private PlanMealRepository repository;

    @Mock
    private DailyPlanRepository dailyPlanRepository;

    @InjectMocks
    private PlanMealServiceImpl service;

    @Test
    void shouldThrowWhenDailyPlanNotFound() {
        // Given
        Long dailyPlanId = 1L;

        PlanMealCreateRequestDTO dto = mock(PlanMealCreateRequestDTO.class);

        given(dto.dailyPlanId()).willReturn(dailyPlanId);

        given(dailyPlanRepository.findById(dailyPlanId))
                .willReturn(Optional.empty());

        // When / Then
        assertThrows(
                ResourceNotFoundException.class,
                () -> service.create(dto)
        );

        verify(repository, never()).save(any());
    }

    @Test
    void shouldCreatePlanMealSuccessfully() {
        // Given
        Long dailyPlanId = 1L;

        DailyPlan dailyPlan = mock(DailyPlan.class);
        PlanMeal entity = mock(PlanMeal.class);
        PlanMeal saved = mock(PlanMeal.class);

        PlanMealCreateRequestDTO dto = mock(PlanMealCreateRequestDTO.class);

        given(dto.dailyPlanId()).willReturn(dailyPlanId);

        given(dailyPlanRepository.findById(dailyPlanId))
                .willReturn(Optional.of(dailyPlan));

        try (MockedStatic<PlanMealMapper> mapper = mockStatic(PlanMealMapper.class)) {

            mapper.when(() -> PlanMealMapper.toEntity(dto, dailyPlan))
                    .thenReturn(entity);

            mapper.when(() -> PlanMealMapper.toResponse(saved))
                    .thenReturn(mock(PlanMealResponseDTO.class));

            given(repository.save(entity))
                    .willReturn(saved);

            // When
            service.create(dto);

            // Then
            verify(repository).save(entity);
        }
    }

    @Test
    void shouldReturnPlanMealById() {

        // Given
        Long id = 1L;

        PlanMeal meal = mock(PlanMeal.class);
        PlanMealResponseDTO response = mock(PlanMealResponseDTO.class);

        given(repository.findById(id))
                .willReturn(Optional.of(meal));

        try (MockedStatic<PlanMealMapper> mapper = mockStatic(PlanMealMapper.class)) {

            mapper.when(() -> PlanMealMapper.toResponse(meal))
                    .thenReturn(response);

            // When
            PlanMealResponseDTO result = service.getById(id);

            // Then
            assertSame(response, result);
        }
    }

    @Test
    void shouldThrowWhenPlanMealNotFound() {

        // Given
        Long id = 1L;

        given(repository.findById(id))
                .willReturn(Optional.empty());

        // When / Then
        assertThrows(
                RuntimeException.class,
                () -> service.getById(id)
        );
    }

    @Test
    void shouldDeletePlanMeal() {

        // Given
        Long id = 1L;

        // When
        service.delete(id);

        // Then
        verify(repository).deleteById(id);
    }

}