package com.lirium.nutrition.service.impl;

import com.lirium.nutrition.dto.request.PlanFoodPortionCreateRequestDTO;
import com.lirium.nutrition.dto.request.PlanFoodPortionUpdateFoodRequestDTO;
import com.lirium.nutrition.dto.response.PlanFoodPortionResponseDTO;
import com.lirium.nutrition.exception.ResourceNotFoundException;
import com.lirium.nutrition.mapper.PlanFoodPortionMapper;
import com.lirium.nutrition.model.entity.*;
import com.lirium.nutrition.model.enums.FoodCategory;
import com.lirium.nutrition.model.enums.MeasureUnit;
import com.lirium.nutrition.model.enums.PlanStatus;
import com.lirium.nutrition.repository.FoodRepository;
import com.lirium.nutrition.repository.PlanFoodPortionRepository;
import com.lirium.nutrition.repository.PlanMealRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class PlanFoodPortionServiceImplTest {

    @Mock
    private PlanFoodPortionRepository repository;

    @Mock
    private PlanMealRepository planMealRepository;

    @Mock
    private FoodRepository foodRepository;

    @InjectMocks
    private PlanFoodPortionServiceImpl service;

    @Test
    void shouldReturnPortionsByPlanMeal() {

        // Given
        PlanFoodPortion portion1 = mock(PlanFoodPortion.class);
        PlanFoodPortion portion2 = mock(PlanFoodPortion.class);

        PlanMeal meal = mock(PlanMeal.class);
        Food food = mock(Food.class);

        when(repository.findByMealId(1L))
                .thenReturn(List.of(portion1, portion2));

        when(portion1.getId()).thenReturn(1L);
        when(portion2.getId()).thenReturn(2L);

        when(portion1.getMeal()).thenReturn(meal);
        when(portion2.getMeal()).thenReturn(meal);

        when(portion1.getFood()).thenReturn(food);
        when(portion2.getFood()).thenReturn(food);

        when(meal.getId()).thenReturn(1L);

        when(food.getId()).thenReturn(10L);
        when(food.getName()).thenReturn("Rice");

        when(portion1.getQuantity()).thenReturn(100.0);
        when(portion1.getUnit()).thenReturn(MeasureUnit.GRAM);

        when(portion2.getQuantity()).thenReturn(200.0);
        when(portion2.getUnit()).thenReturn(MeasureUnit.GRAM);

        // When
        var result = service.getByPlanMeal(1L);

        // Then
        assertEquals(2, result.size());
        verify(repository).findByMealId(1L);
    }


    @Test
    void shouldThrowWhenPortionNotFound() {

        // Given
        when(repository.findById(1L))
                .thenReturn(Optional.empty());

        // When + Then
        assertThrows(RuntimeException.class,
                () -> service.getById(1L));
    }

    @Test
    void shouldCreatePortion() {

        // Given
        PlanMeal meal = mock(PlanMeal.class);
        Food food = mock(Food.class);
        PlanFoodPortion portion = mock(PlanFoodPortion.class);

        when(meal.getId()).thenReturn(1L);

        when(food.getId()).thenReturn(2L);
        when(food.getName()).thenReturn("Rice");

        when(planMealRepository.findById(1L))
                .thenReturn(Optional.of(meal));

        when(foodRepository.findById(2L))
                .thenReturn(Optional.of(food));

        when(portion.getId()).thenReturn(100L);
        when(portion.getMeal()).thenReturn(meal);
        when(portion.getFood()).thenReturn(food);
        when(portion.getQuantity()).thenReturn(100.0);
        when(portion.getUnit()).thenReturn(MeasureUnit.GRAM);

        when(repository.save(any(PlanFoodPortion.class)))
                .thenReturn(portion);

        var dto = createPlanFoodPortionCreateRequestDTO();

        // When
        var result = service.create(dto);

        // Then
        assertNotNull(result);
        assertEquals(100L, result.id());
        assertEquals(1L, result.mealId());
        assertEquals(2L, result.foodId());
        assertEquals("Rice", result.foodName());
        assertEquals(100.0, result.quantity());
        assertEquals(MeasureUnit.GRAM, result.unit());

        verify(planMealRepository).findById(1L);
        verify(foodRepository).findById(2L);
        verify(repository).save(any(PlanFoodPortion.class));
    }

    @Test
    void shouldFailWhenMealNotFound() {

        when(planMealRepository.findById(1L))
                .thenReturn(Optional.empty());

        var dto = createPlanFoodPortionCreateRequestDTO();

        assertThrows(ResourceNotFoundException.class,
                () -> service.create(dto));
    }

    @Test
    void shouldFailWhenFoodNotFound() {

        PlanMeal meal = mock(PlanMeal.class);

        when(planMealRepository.findById(1L))
                .thenReturn(Optional.of(meal));

        when(foodRepository.findById(2L))
                .thenReturn(Optional.empty());

        var dto = createPlanFoodPortionCreateRequestDTO();

        assertThrows(ResourceNotFoundException.class,
                () -> service.create(dto));
    }

    @Test
    void shouldDeletePortion() {

        service.delete(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void shouldFailUpdateWhenNotFound() {

        when(repository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.update(1L,
                        new PlanFoodPortionUpdateFoodRequestDTO(null, null)));
    }

    @Test
    void shouldFailUpdateWhenPlanNotDraft() {

        // Given
        PlanFoodPortion portion = mockPortionWithStatus(PlanStatus.ACTIVE);

        when(repository.findById(1L))
                .thenReturn(Optional.of(portion));

        // When / Then
        assertThrows(IllegalStateException.class,
                () -> service.update(
                        1L,
                        new PlanFoodPortionUpdateFoodRequestDTO(null, null)
                ));

        verify(repository).findById(1L);
        verify(repository, never()).save(any());
        verifyNoInteractions(foodRepository);
    }

    @Test
    void shouldReturnEntityById() {

        PlanFoodPortion portion = mock(PlanFoodPortion.class);

        when(repository.findById(1L))
                .thenReturn(Optional.of(portion));

        PlanFoodPortion result = service.findEntityById(1L);

        assertSame(portion, result);
        verify(repository).findById(1L);
    }

    @Test
    void shouldThrowWhenEntityNotFound() {

        when(repository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.findEntityById(1L));

        verify(repository).findById(1L);
    }

    @Test
    void shouldFailWhenCategoryMismatch() {

        PlanFoodPortion portion = mock(PlanFoodPortion.class);
        Food oldFood = mock(Food.class);
        Food newFood = mock(Food.class);

        when(oldFood.getId()).thenReturn(2L);

        PlanMeal meal = mock(PlanMeal.class);
        DailyPlan dailyPlan = mock(DailyPlan.class);
        NutritionPlan nutritionPlan = mock(NutritionPlan.class);

        when(portion.getId()).thenReturn(1L);

        when(portion.getMeal()).thenReturn(meal);
        when(meal.getDailyPlan()).thenReturn(dailyPlan);
        when(dailyPlan.getNutritionPlan()).thenReturn(nutritionPlan);
        when(nutritionPlan.getStatus()).thenReturn(PlanStatus.DRAFT);

        when(portion.getFood()).thenReturn(oldFood);
        when(foodRepository.findById(2L))
                .thenReturn(Optional.of(newFood));

        when(oldFood.getCategory()).thenReturn(FoodCategory.PROTEIN);
        when(newFood.getCategory()).thenReturn(FoodCategory.CARB);

        when(repository.findById(1L))
                .thenReturn(Optional.of(portion));

        when(foodRepository.findById(2L))
                .thenReturn(Optional.of(newFood));

        PlanFoodPortionUpdateFoodRequestDTO dto =
                new PlanFoodPortionUpdateFoodRequestDTO(2L, null);

        assertThrows(IllegalArgumentException.class,
                () -> service.update(1L, dto));

        verify(repository).findById(1L);
        verify(foodRepository).findById(2L);
    }

    @Test
    void shouldUpdateQuantitySuccessfully() {

        // given
        PlanFoodPortion portion = mock(PlanFoodPortion.class);

        Food food = mock(Food.class);
        when(food.getId()).thenReturn(2L);
        when(food.getCategory()).thenReturn(FoodCategory.CARB);

        NutritionPlan nutritionPlan = mock(NutritionPlan.class);
        when(nutritionPlan.getStatus()).thenReturn(PlanStatus.DRAFT);

        DailyPlan dailyPlan = mock(DailyPlan.class);
        when(dailyPlan.getNutritionPlan()).thenReturn(nutritionPlan);

        PlanMeal meal = mock(PlanMeal.class);
        when(meal.getDailyPlan()).thenReturn(dailyPlan);

        when(portion.getMeal()).thenReturn(meal);
        when(portion.getFood()).thenReturn(food);

        when(repository.findById(1L)).thenReturn(Optional.of(portion));

        // 🔥 ESTE ERA EL QUE TE FALTABA
        when(foodRepository.findById(2L)).thenReturn(Optional.of(food));

        PlanFoodPortionUpdateFoodRequestDTO dto =
                new PlanFoodPortionUpdateFoodRequestDTO(null, 250.0);

        when(repository.save(portion)).thenReturn(portion);

        try (MockedStatic<PlanFoodPortionMapper> mapper =
                     mockStatic(PlanFoodPortionMapper.class)) {

            mapper.when(() -> PlanFoodPortionMapper.toResponse(portion))
                    .thenReturn(mock(PlanFoodPortionResponseDTO.class));

            // when
            var result = service.update(1L, dto);

            // then
            verify(portion).changeQuantity(250.0);
            verify(repository).save(portion);
        }
    }

    @Test
    void shouldUpdateOnlyQuantityWhenPortionHasNoId() {

        // Given
        PlanFoodPortion portion = mock(PlanFoodPortion.class);

        NutritionPlan nutritionPlan = mock(NutritionPlan.class);
        when(nutritionPlan.getStatus()).thenReturn(PlanStatus.DRAFT);

        DailyPlan dailyPlan = mock(DailyPlan.class);
        when(dailyPlan.getNutritionPlan()).thenReturn(nutritionPlan);

        PlanMeal meal = mock(PlanMeal.class);
        when(meal.getDailyPlan()).thenReturn(dailyPlan);

        Food food = mock(Food.class);
        when(food.getId()).thenReturn(10L);

        when(portion.getMeal()).thenReturn(meal);
        when(portion.getFood()).thenReturn(food);
        when(portion.getId()).thenReturn(null);

        when(repository.findById(1L))
                .thenReturn(Optional.of(portion));

        when(repository.save(portion))
                .thenReturn(portion);

        try (MockedStatic<PlanFoodPortionMapper> mapper =
                     mockStatic(PlanFoodPortionMapper.class)) {

            PlanFoodPortionResponseDTO response =
                    mock(PlanFoodPortionResponseDTO.class);

            mapper.when(() -> PlanFoodPortionMapper.toResponse(portion))
                    .thenReturn(response);

            // When
            PlanFoodPortionResponseDTO result = service.update(
                    1L,
                    new PlanFoodPortionUpdateFoodRequestDTO(null, 250.0)
            );

            // Then
            assertThat(result).isSameAs(response);

            verify(foodRepository, never()).findById(anyLong());
            verify(portion, never()).changeFood(any(Food.class));
            verify(portion).changeQuantity(250.0);
            verify(repository).save(portion);
        }
    }

    @Test
    void shouldNotChangeQuantityWhenQuantityIsNull() {

        // Given
        PlanFoodPortion portion = mock(PlanFoodPortion.class);

        NutritionPlan nutritionPlan = mock(NutritionPlan.class);
        when(nutritionPlan.getStatus()).thenReturn(PlanStatus.DRAFT);

        DailyPlan dailyPlan = mock(DailyPlan.class);
        when(dailyPlan.getNutritionPlan()).thenReturn(nutritionPlan);

        PlanMeal meal = mock(PlanMeal.class);
        when(meal.getDailyPlan()).thenReturn(dailyPlan);

        Food food = mock(Food.class);
        when(food.getId()).thenReturn(10L);

        when(portion.getMeal()).thenReturn(meal);
        when(portion.getFood()).thenReturn(food);
        when(portion.getId()).thenReturn(null);

        when(repository.findById(1L))
                .thenReturn(Optional.of(portion));

        when(repository.save(portion))
                .thenReturn(portion);

        try (MockedStatic<PlanFoodPortionMapper> mapper =
                     mockStatic(PlanFoodPortionMapper.class)) {

            PlanFoodPortionResponseDTO response = mock(PlanFoodPortionResponseDTO.class);

            mapper.when(() -> PlanFoodPortionMapper.toResponse(portion))
                    .thenReturn(response);

            // When
            PlanFoodPortionResponseDTO result = service.update(
                    1L,
                    new PlanFoodPortionUpdateFoodRequestDTO(null, null)
            );

            // Then
            assertThat(result).isSameAs(response);

            verify(foodRepository, never()).findById(anyLong());
            verify(portion, never()).changeFood(any(Food.class));
            verify(portion, never()).changeQuantity(anyDouble());
            verify(repository).save(portion);
        }
    }

    @Test
    void shouldFailUpdateWhenFoodNotFound() {

        // Given
        PlanFoodPortion portion = mock(PlanFoodPortion.class);

        NutritionPlan nutritionPlan = mock(NutritionPlan.class);
        when(nutritionPlan.getStatus()).thenReturn(PlanStatus.DRAFT);

        DailyPlan dailyPlan = mock(DailyPlan.class);
        when(dailyPlan.getNutritionPlan()).thenReturn(nutritionPlan);

        PlanMeal meal = mock(PlanMeal.class);
        when(meal.getDailyPlan()).thenReturn(dailyPlan);

        Food currentFood = mock(Food.class);
        when(currentFood.getId()).thenReturn(2L);

        when(portion.getMeal()).thenReturn(meal);
        when(portion.getFood()).thenReturn(currentFood);
        when(portion.getId()).thenReturn(1L);

        when(repository.findById(1L))
                .thenReturn(Optional.of(portion));

        when(foodRepository.findById(2L))
                .thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() ->
                service.update(
                        1L,
                        new PlanFoodPortionUpdateFoodRequestDTO(null, null)
                )
        ).isInstanceOf(ResourceNotFoundException.class);

        verify(repository, never()).save(any());
        verify(portion, never()).changeFood(any());
        verify(portion, never()).changeQuantity(anyDouble());
    }


    private PlanFoodPortion mockPortion(PlanStatus status, FoodCategory category) {

        NutritionPlan nutritionPlan = mock(NutritionPlan.class);
        when(nutritionPlan.getStatus()).thenReturn(status);

        DailyPlan dailyPlan = mock(DailyPlan.class);
        when(dailyPlan.getNutritionPlan()).thenReturn(nutritionPlan);

        PlanMeal meal = mock(PlanMeal.class);
        when(meal.getDailyPlan()).thenReturn(dailyPlan);

        Food food = mock(Food.class);
        when(food.getCategory()).thenReturn(category);

        PlanFoodPortion portion = mock(PlanFoodPortion.class);
        when(portion.getMeal()).thenReturn(meal);
        when(portion.getFood()).thenReturn(food);

        return portion;
    }

    private PlanFoodPortion mockPortionWithStatus(PlanStatus status) {

        NutritionPlan nutritionPlan = mock(NutritionPlan.class);
        when(nutritionPlan.getStatus()).thenReturn(status);

        DailyPlan dailyPlan = mock(DailyPlan.class);
        when(dailyPlan.getNutritionPlan()).thenReturn(nutritionPlan);

        PlanMeal meal = mock(PlanMeal.class);
        when(meal.getDailyPlan()).thenReturn(dailyPlan);

        Food food = mock(Food.class);

        PlanFoodPortion portion = mock(PlanFoodPortion.class);
        when(portion.getMeal()).thenReturn(meal);

        return portion;
    }


    private PlanFoodPortion mockPortionWithStatusAndFood(PlanStatus status) {

        NutritionPlan nutritionPlan = mock(NutritionPlan.class);
        when(nutritionPlan.getStatus()).thenReturn(status);

        DailyPlan dailyPlan = mock(DailyPlan.class);
        when(dailyPlan.getNutritionPlan()).thenReturn(nutritionPlan);

        PlanMeal meal = mock(PlanMeal.class);
        when(meal.getDailyPlan()).thenReturn(dailyPlan);

        Food food = mock(Food.class);

        PlanFoodPortion portion = mock(PlanFoodPortion.class);
        when(portion.getMeal()).thenReturn(meal);
        when(portion.getFood()).thenReturn(food);

        return portion;
    }

    private PlanFoodPortionCreateRequestDTO createPlanFoodPortionCreateRequestDTO(){
      return new PlanFoodPortionCreateRequestDTO(1L, 2L, 2D, MeasureUnit.MILLILITER);
   }

}