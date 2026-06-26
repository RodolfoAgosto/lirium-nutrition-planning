package com.lirium.nutrition.service.impl;

import com.lirium.nutrition.dto.request.FoodPortionAddRequestDTO;
import com.lirium.nutrition.dto.request.PlanFoodPortionUpdateFoodRequestDTO;
import com.lirium.nutrition.dto.request.PlanMealCreateRequestDTO;
import com.lirium.nutrition.dto.response.PlanMealResponseDTO;
import com.lirium.nutrition.dto.response.PlanMealSummaryDTO;
import com.lirium.nutrition.exception.DuplicateFoodException;
import com.lirium.nutrition.exception.ResourceNotFoundException;
import com.lirium.nutrition.mapper.PlanFoodPortionMapper;
import com.lirium.nutrition.mapper.PlanMealMapper;
import com.lirium.nutrition.model.entity.DailyPlan;
import com.lirium.nutrition.model.entity.Food;
import com.lirium.nutrition.model.entity.PlanFoodPortion;
import com.lirium.nutrition.model.entity.PlanMeal;
import com.lirium.nutrition.model.enums.MeasureUnit;
import com.lirium.nutrition.repository.DailyPlanRepository;
import com.lirium.nutrition.repository.PlanMealRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
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

    @Mock
    private FoodServiceImpl foodService;

    @Mock
    private PlanFoodPortionServiceImpl planFoodPortionService;

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

    @Test
    void shouldReturnMealsByPlanDay() {

        Long dayId = 1L;

        PlanMeal meal = mock(PlanMeal.class);
        PlanMealSummaryDTO summary = mock(PlanMealSummaryDTO.class);

        given(repository.findByDailyPlanId(dayId))
                .willReturn(List.of(meal));

        try (MockedStatic<PlanMealMapper> mapper = mockStatic(PlanMealMapper.class)) {

            mapper.when(() -> PlanMealMapper.toSummary(meal))
                    .thenReturn(summary);

            List<PlanMealSummaryDTO> result = service.getByPlanDay(dayId);

            assertEquals(1, result.size());
            assertSame(summary, result.getFirst());
        }
    }

    @Test
    void shouldFailAddPortionWhenMealNotFound() {

        given(repository.findById(1L))
                .willReturn(Optional.empty());

        FoodPortionAddRequestDTO dto =
                new FoodPortionAddRequestDTO(10L, 100.0, MeasureUnit.GRAM);

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.addPortion(1L, dto)
        );
    }

    @Test
    void shouldFailAddPortionWhenFoodAlreadyExists() {

        PlanMeal meal = mock(PlanMeal.class);

        Food food = mock(Food.class);
        given(food.getId()).willReturn(10L);

        PlanFoodPortion portion = mock(PlanFoodPortion.class);
        given(portion.getFood()).willReturn(food);

        given(meal.getFoodPortions())
                .willReturn(List.of(portion));

        given(repository.findById(1L))
                .willReturn(Optional.of(meal));

        FoodPortionAddRequestDTO dto =
                new FoodPortionAddRequestDTO(10L, 100.0, MeasureUnit.GRAM);

        assertThrows(
                DuplicateFoodException.class,
                () -> service.addPortion(1L, dto)
        );
    }

    @Test
    void shouldAddPortionSuccessfully() {

        PlanMeal meal = mock(PlanMeal.class);
        Food food = mock(Food.class);
        PlanFoodPortion newPortion = mock(PlanFoodPortion.class);

        given(meal.getFoodPortions())
                .willReturn(new ArrayList<>());

        given(repository.findById(1L))
                .willReturn(Optional.of(meal));

        given(foodService.findEntityById(10L))
                .willReturn(food);

        FoodPortionAddRequestDTO dto =
                new FoodPortionAddRequestDTO(10L, 100.0, MeasureUnit.GRAM);

        PlanMealResponseDTO response = mock(PlanMealResponseDTO.class);

        try (
                MockedStatic<PlanFoodPortionMapper> portionMapper =
                        mockStatic(PlanFoodPortionMapper.class);

                MockedStatic<PlanMealMapper> mealMapper =
                        mockStatic(PlanMealMapper.class)
        ) {

            portionMapper.when(
                    () -> PlanFoodPortionMapper.toEntity(dto, meal, food)
            ).thenReturn(newPortion);

            mealMapper.when(
                    () -> PlanMealMapper.toResponse(meal)
            ).thenReturn(response);

            PlanMealResponseDTO result =
                    service.addPortion(1L, dto);

            verify(meal).addFoodPortion(newPortion);
            assertSame(response, result);
        }
    }

    @Test
    void shouldRemovePortionSuccessfully() {

        PlanMeal meal = mock(PlanMeal.class);

        PlanFoodPortion portion = mock(PlanFoodPortion.class);

        given(repository.findById(1L))
                .willReturn(Optional.of(meal));

        given(planFoodPortionService.findEntityById(2L))
                .willReturn(portion);

        PlanMealResponseDTO response = mock(PlanMealResponseDTO.class);

        try (MockedStatic<PlanMealMapper> mapper = mockStatic(PlanMealMapper.class)) {

            mapper.when(() -> PlanMealMapper.toResponse(meal))
                    .thenReturn(response);

            PlanMealResponseDTO result =
                    service.removePortion(1L, 2L);

            verify(meal).removeFoodPortion(portion);
            assertSame(response, result);
        }
    }

    @Test
    void shouldFailUpdateWhenMealNotFound() {

        given(repository.findById(1L))
                .willReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.updatePortion(
                        1L,
                        1L,
                        new PlanFoodPortionUpdateFoodRequestDTO(null, 100.0)
                )
        );
    }

    @Test
    void shouldUpdateQuantity() {

        PlanMeal meal = mock(PlanMeal.class);

        PlanFoodPortion portion = mock(PlanFoodPortion.class);

        given(portion.getId()).willReturn(1L);

        given(meal.getFoodPortions())
                .willReturn(List.of(portion));

        given(repository.findById(1L))
                .willReturn(Optional.of(meal));

        PlanMealResponseDTO response = mock(PlanMealResponseDTO.class);

        try (MockedStatic<PlanMealMapper> mapper = mockStatic(PlanMealMapper.class)) {

            mapper.when(() -> PlanMealMapper.toResponse(meal))
                    .thenReturn(response);

            service.updatePortion(
                    1L,
                    1L,
                    new PlanFoodPortionUpdateFoodRequestDTO(null, 200.0)
            );

            verify(portion).changeQuantity(200.0);
        }
    }

    @Test
    void shouldUpdateFood() {

        PlanMeal meal = mock(PlanMeal.class);

        PlanFoodPortion portion = mock(PlanFoodPortion.class);

        Food oldFood = mock(Food.class);
        given(oldFood.getId()).willReturn(1L);

        given(portion.getId()).willReturn(1L);
        given(portion.getFood()).willReturn(oldFood);

        given(meal.getFoodPortions())
                .willReturn(List.of(portion));

        given(repository.findById(1L))
                .willReturn(Optional.of(meal));

        Food newFood = mock(Food.class);

        given(foodService.findEntityById(2L))
                .willReturn(newFood);

        PlanMealResponseDTO response = mock(PlanMealResponseDTO.class);

        try (MockedStatic<PlanMealMapper> mapper = mockStatic(PlanMealMapper.class)) {

            mapper.when(() -> PlanMealMapper.toResponse(meal))
                    .thenReturn(response);

            service.updatePortion(
                    1L,
                    1L,
                    new PlanFoodPortionUpdateFoodRequestDTO(2L, null)
            );

            verify(portion).changeFood(newFood);
        }
    }

}