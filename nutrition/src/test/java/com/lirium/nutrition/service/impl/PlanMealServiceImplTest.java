package com.lirium.nutrition.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import com.lirium.nutrition.dto.request.FoodPortionAddRequestDTO;
import com.lirium.nutrition.dto.request.PlanFoodPortionUpdateQuantityRequestDTO;
import com.lirium.nutrition.dto.request.PlanMealCreateRequestDTO;
import com.lirium.nutrition.dto.response.PlanMealResponseDTO;
import com.lirium.nutrition.dto.response.PlanMealSummaryDTO;
import com.lirium.nutrition.exception.DuplicateFoodException;
import com.lirium.nutrition.exception.ResourceNotFoundException;
import com.lirium.nutrition.exception.UnprocessableEntityException;
import com.lirium.nutrition.mapper.PlanFoodPortionMapper;
import com.lirium.nutrition.mapper.PlanMealMapper;
import com.lirium.nutrition.model.entity.*;
import com.lirium.nutrition.model.enums.MeasureUnit;
import com.lirium.nutrition.repository.DailyPlanRepository;
import com.lirium.nutrition.repository.PlanFoodPortionRepository;
import com.lirium.nutrition.repository.PlanMealRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlanMealServiceImplTest {

  @Mock private PlanMealRepository repository;

  @Mock private DailyPlanRepository dailyPlanRepository;

  @Mock private FoodServiceImpl foodService;

  @Mock private PlanFoodPortionServiceImpl planFoodPortionService;

  @Mock private PlanFoodPortionRepository planFoodPortionRepository;

  @InjectMocks private PlanMealServiceImpl service;

  @Test
  void shouldThrowWhenDailyPlanNotFound() {
    // Given
    Long dailyPlanId = 1L;

    PlanMealCreateRequestDTO dto = mock(PlanMealCreateRequestDTO.class);

    given(dto.dailyPlanId()).willReturn(dailyPlanId);

    given(dailyPlanRepository.findById(dailyPlanId)).willReturn(Optional.empty());

    // When / Then
    assertThrows(ResourceNotFoundException.class, () -> service.create(dto));

    verify(repository, never()).save(any());
  }

  @Test
  void shouldCreatePlanMealSuccessfully() {
    // Given
    Long dailyPlanId = 1L;

    DailyPlan dailyPlan = mock(DailyPlan.class);
    NutritionPlan nutritionPlan = mock(NutritionPlan.class);
    PlanMeal entity = mock(PlanMeal.class);
    PlanMeal saved = mock(PlanMeal.class);

    PlanMealCreateRequestDTO dto = mock(PlanMealCreateRequestDTO.class);

    given(dto.dailyPlanId()).willReturn(dailyPlanId);

    given(dailyPlanRepository.findById(dailyPlanId)).willReturn(Optional.of(dailyPlan));

    given(dailyPlan.getNutritionPlan()).willReturn(nutritionPlan);

    try (MockedStatic<PlanMealMapper> mapper = mockStatic(PlanMealMapper.class)) {

      mapper.when(() -> PlanMealMapper.toEntity(dto, dailyPlan)).thenReturn(entity);

      mapper
          .when(() -> PlanMealMapper.toResponse(saved))
          .thenReturn(mock(PlanMealResponseDTO.class));

      given(repository.save(entity)).willReturn(saved);

      // When
      service.create(dto);

      // Then
      verify(nutritionPlan).ensureEditable();
      verify(repository).save(entity);
    }
  }

  @Test
  void shouldReturnPlanMealById() {

    // Given
    Long id = 1L;

    PlanMeal meal = mock(PlanMeal.class);
    PlanMealResponseDTO response = mock(PlanMealResponseDTO.class);

    given(repository.findById(id)).willReturn(Optional.of(meal));

    try (MockedStatic<PlanMealMapper> mapper = mockStatic(PlanMealMapper.class)) {

      mapper.when(() -> PlanMealMapper.toResponse(meal)).thenReturn(response);

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

    given(repository.findById(id)).willReturn(Optional.empty());

    // When / Then
    assertThrows(RuntimeException.class, () -> service.getById(id));
  }

  @Test
  void shouldDeletePlanMealWhenPlanIsDraft() {
    Long id = 1L;

    NutritionPlan nutritionPlan = mock(NutritionPlan.class);
    DailyPlan dailyPlan = mock(DailyPlan.class);
    PlanMeal planMeal = mock(PlanMeal.class);

    when(planMeal.getDailyPlan()).thenReturn(dailyPlan);
    when(dailyPlan.getNutritionPlan()).thenReturn(nutritionPlan);

    when(repository.findById(id)).thenReturn(Optional.of(planMeal));

    service.delete(id);

    verify(nutritionPlan).ensureEditable();
    verify(repository).delete(planMeal);
  }

  @Test
  void shouldThrowResourceNotFoundExceptionWhenPlanMealDoesNotExist() {
    // Given
    Long id = 99L;
    when(repository.findById(id)).thenReturn(Optional.empty());

    // When & Then
    assertThrows(ResourceNotFoundException.class, () -> service.delete(id));
    verify(repository, never()).delete(any());
  }

  @Test
  void shouldThrowUnprocessableEntityExceptionWhenPlanIsNotDraft() {
    // Given
    Long id = 1L;

    NutritionPlan nutritionPlan = mock(NutritionPlan.class);
    doThrow(
            new UnprocessableEntityException(
                "Nutrition plan is not editable in its current status"))
        .when(nutritionPlan)
        .ensureEditable();

    DailyPlan dailyPlan = mock(DailyPlan.class);
    PlanMeal planMeal = mock(PlanMeal.class);

    when(planMeal.getDailyPlan()).thenReturn(dailyPlan);
    when(dailyPlan.getNutritionPlan()).thenReturn(nutritionPlan);
    when(repository.findById(id)).thenReturn(Optional.of(planMeal));

    // When & Then
    assertThrows(UnprocessableEntityException.class, () -> service.delete(id));
    // Nos aseguramos de que NUNCA se llamó al delete del repositorio
    verify(repository, never()).delete(any());
  }

  @Test
  void shouldReturnMealsByPlanDay() {

    Long dayId = 1L;

    PlanMeal meal = mock(PlanMeal.class);
    PlanMealSummaryDTO summary = mock(PlanMealSummaryDTO.class);

    given(dailyPlanRepository.existsById(dayId)).willReturn(true);

    given(repository.findByDailyPlanId(dayId)).willReturn(List.of(meal));

    try (MockedStatic<PlanMealMapper> mapper = mockStatic(PlanMealMapper.class)) {

      mapper.when(() -> PlanMealMapper.toSummary(meal)).thenReturn(summary);

      List<PlanMealSummaryDTO> result = service.getByPlanDay(dayId);

      assertEquals(1, result.size());
      assertSame(summary, result.getFirst());
    }
  }

  @Test
  void shouldFailAddPortionWhenMealNotFound() {

    given(repository.findById(1L)).willReturn(Optional.empty());

    FoodPortionAddRequestDTO dto = new FoodPortionAddRequestDTO(10L, 100.0, MeasureUnit.GRAM);

    assertThrows(ResourceNotFoundException.class, () -> service.addPortion(1L, dto));
  }

  @Test
  void shouldFailAddPortionWhenFoodAlreadyExists() {

    NutritionPlan nutritionPlan = mock(NutritionPlan.class);
    DailyPlan dailyPlan = mock(DailyPlan.class);
    given(dailyPlan.getNutritionPlan()).willReturn(nutritionPlan);

    PlanMeal meal = mock(PlanMeal.class);
    given(meal.getDailyPlan()).willReturn(dailyPlan);

    given(repository.findById(1L)).willReturn(Optional.of(meal));

    given(planFoodPortionRepository.existsByMeal_IdAndFood_Id(1L, 10L)).willReturn(true);

    FoodPortionAddRequestDTO dto = new FoodPortionAddRequestDTO(10L, 100.0, MeasureUnit.GRAM);

    assertThrows(DuplicateFoodException.class, () -> service.addPortion(1L, dto));
  }

  @Test
  void shouldAddPortionSuccessfully() {

    NutritionPlan nutritionPlan = mock(NutritionPlan.class);
    DailyPlan dailyPlan = mock(DailyPlan.class);
    given(dailyPlan.getNutritionPlan()).willReturn(nutritionPlan);

    PlanMeal meal = mock(PlanMeal.class);
    given(meal.getDailyPlan()).willReturn(dailyPlan);

    Food food = mock(Food.class);
    PlanFoodPortion newPortion = mock(PlanFoodPortion.class);

    given(repository.findById(1L)).willReturn(Optional.of(meal));

    given(planFoodPortionRepository.existsByMeal_IdAndFood_Id(1L, 10L)).willReturn(false);

    given(foodService.findEntityById(10L)).willReturn(food);

    FoodPortionAddRequestDTO dto = new FoodPortionAddRequestDTO(10L, 100.0, MeasureUnit.GRAM);

    PlanMealResponseDTO response = mock(PlanMealResponseDTO.class);

    try (MockedStatic<PlanFoodPortionMapper> portionMapper =
            mockStatic(PlanFoodPortionMapper.class);
        MockedStatic<PlanMealMapper> mealMapper = mockStatic(PlanMealMapper.class)) {

      portionMapper
          .when(() -> PlanFoodPortionMapper.toEntity(dto, meal, food))
          .thenReturn(newPortion);

      mealMapper.when(() -> PlanMealMapper.toResponse(meal)).thenReturn(response);

      PlanMealResponseDTO result = service.addPortion(1L, dto);

      verify(nutritionPlan).ensureEditable(); // Verifica la regla de dominio
      verify(meal).addFoodPortion(newPortion);
      assertSame(response, result);
    }
  }

  @Test
  @DisplayName("Debe eliminar la porción exitosamente")
  void shouldRemovePortionSuccessfully() {

    PlanMeal meal = mock(PlanMeal.class);
    DailyPlan dailyPlan = mock(DailyPlan.class);
    NutritionPlan nutritionPlan = mock(NutritionPlan.class);
    PlanFoodPortion portion = mock(PlanFoodPortion.class);

    given(meal.getDailyPlan()).willReturn(dailyPlan);
    given(dailyPlan.getNutritionPlan()).willReturn(nutritionPlan);

    given(portion.getMeal()).willReturn(meal);
    given(meal.getId()).willReturn(1L);

    given(repository.findById(1L)).willReturn(Optional.of(meal));
    given(planFoodPortionService.findEntityById(2L)).willReturn(portion);

    PlanMealResponseDTO response = mock(PlanMealResponseDTO.class);

    try (MockedStatic<PlanMealMapper> mapper = mockStatic(PlanMealMapper.class)) {

      mapper.when(() -> PlanMealMapper.toResponse(meal)).thenReturn(response);

      PlanMealResponseDTO result = service.removePortion(1L, 2L);

      verify(meal).removeFoodPortion(portion);
      assertSame(response, result);
    }
  }

  @Test
  void shouldFailUpdateQuantityWhenMealNotFound() {

    given(repository.findById(1L)).willReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> service.updateQuantity(1L, 1L, new PlanFoodPortionUpdateQuantityRequestDTO(100.0)));
  }

  @Test
  void shouldUpdateQuantity() {

    PlanMeal meal = mock(PlanMeal.class);
    DailyPlan dailyPlan = mock(DailyPlan.class);
    NutritionPlan nutritionPlan = mock(NutritionPlan.class);
    PlanFoodPortion portion = mock(PlanFoodPortion.class);

    given(meal.getDailyPlan()).willReturn(dailyPlan);
    given(dailyPlan.getNutritionPlan()).willReturn(nutritionPlan);
    given(portion.getMeal()).willReturn(meal);
    given(meal.getId()).willReturn(1L);

    given(repository.findById(1L)).willReturn(Optional.of(meal));
    given(planFoodPortionRepository.findById(1L)).willReturn(Optional.of(portion));

    PlanMealResponseDTO response = mock(PlanMealResponseDTO.class);

    try (MockedStatic<PlanMealMapper> mapper = mockStatic(PlanMealMapper.class)) {

      mapper.when(() -> PlanMealMapper.toResponse(meal)).thenReturn(response);

      PlanMealResponseDTO result =
          service.updateQuantity(1L, 1L, new PlanFoodPortionUpdateQuantityRequestDTO(200.0));

      verify(portion).changeQuantity(200.0);
      verify(planFoodPortionRepository).save(portion);
      assertNotNull(result);
    }
  }

  @Test
  void shouldFailUpdateQuantityWhenPortionDoesNotBelongToMeal() {

    PlanMeal meal = mock(PlanMeal.class);
    PlanMeal otherMeal = mock(PlanMeal.class);
    PlanFoodPortion portion = mock(PlanFoodPortion.class);

    // Si tu servicio hace portion.getMeal().getId(), solo necesitas mockear otherMeal.getId()
    given(otherMeal.getId()).willReturn(99L);
    given(portion.getMeal()).willReturn(otherMeal);

    given(repository.findById(1L)).willReturn(Optional.of(meal));
    given(planFoodPortionRepository.findById(1L)).willReturn(Optional.of(portion));

    assertThrows(
        IllegalArgumentException.class,
        () ->
            service.updateQuantity(
                1L, // mealId = 1
                1L, // portionId = 1
                new PlanFoodPortionUpdateQuantityRequestDTO(150.0)));
  }
}
