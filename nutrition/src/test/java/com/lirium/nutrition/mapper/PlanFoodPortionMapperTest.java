package com.lirium.nutrition.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.lirium.nutrition.dto.request.FoodPortionAddRequestDTO;
import com.lirium.nutrition.dto.request.PlanFoodPortionCreateRequestDTO;
import com.lirium.nutrition.dto.response.PlanFoodPortionResponseDTO;
import com.lirium.nutrition.dto.response.PlanFoodPortionSummaryDTO;
import com.lirium.nutrition.model.entity.*;
import com.lirium.nutrition.model.enums.*;
import com.lirium.nutrition.mapper.PlanFoodPortionMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PlanFoodPortionMapperTest {

    @Test
    void toResponse_shouldMapEntityToResponseDTO() {

        Food food = Food.of(
                "Apple",
                52,
                0,
                14,
                0,
                FoodCategory.FRUIT,
                Set.of(MealType.BREAKFAST)
        );

        ReflectionTestUtils.setField(food, "id", 2L);

        DailyPlan dailyPlan = org.mockito.Mockito.mock(DailyPlan.class);

        PlanMeal meal = PlanMeal.of(MealType.BREAKFAST, dailyPlan);
        ReflectionTestUtils.setField(meal, "id", 1L);

        PlanFoodPortion portion =
                PlanFoodPortion.of(meal, food, 150.0, MeasureUnit.GRAM);

        ReflectionTestUtils.setField(portion, "id", 10L);

        PlanFoodPortionResponseDTO dto =
                PlanFoodPortionMapper.toResponse(portion);

        assertEquals(10L, dto.id());
        assertEquals(1L, dto.mealId());
        assertEquals(2L, dto.foodId());
        assertEquals("Apple", dto.foodName());
        assertEquals(150.0, dto.quantity());
        assertEquals(MeasureUnit.GRAM, dto.unit());
    }

    @Test
    void toSummary_shouldMapEntityToSummaryDTO() {

        Food food = Food.of(
                "Apple",
                52,
                0,
                14,
                0,
                FoodCategory.FRUIT,
                Set.of(MealType.BREAKFAST)
        );

        ReflectionTestUtils.setField(food, "id", 2L);

        DailyPlan dailyPlan = org.mockito.Mockito.mock(DailyPlan.class);

        PlanMeal meal = PlanMeal.of(MealType.BREAKFAST, dailyPlan);

        PlanFoodPortion portion =
                PlanFoodPortion.of(meal, food, 150.0, MeasureUnit.GRAM);

        ReflectionTestUtils.setField(portion, "id", 10L);

        PlanFoodPortionSummaryDTO dto =
                PlanFoodPortionMapper.toSummary(portion);

        assertEquals(10L, dto.id());
        assertEquals(2L, dto.foodId());
        assertEquals(150.0, dto.quantity());
        assertEquals(MeasureUnit.GRAM, dto.uni());
    }

    @Test
    void toEntity_shouldCreateEntityFromCreateRequest() {

        Food food = Food.of(
                "Apple",
                52,
                0,
                14,
                0,
                FoodCategory.FRUIT,
                Set.of(MealType.BREAKFAST)
        );

        DailyPlan dailyPlan = org.mockito.Mockito.mock(DailyPlan.class);

        PlanMeal meal = PlanMeal.of(MealType.BREAKFAST, dailyPlan);

        PlanFoodPortionCreateRequestDTO dto =
                new PlanFoodPortionCreateRequestDTO(
                        1L,
                        2L,
                        150.0,
                        MeasureUnit.GRAM
                );

        PlanFoodPortion entity =
                PlanFoodPortionMapper.toEntity(dto, meal, food);

        assertEquals(meal, entity.getMeal());
        assertEquals(food, entity.getFood());
        assertEquals(150.0, entity.getQuantity());
        assertEquals(MeasureUnit.GRAM, entity.getUnit());
    }

    @Test
    void toEntity_shouldCreateEntityFromFoodPortionAddRequest() {

        Food food = Food.of(
                "Apple",
                52,
                0,
                14,
                0,
                FoodCategory.FRUIT,
                Set.of(MealType.BREAKFAST)
        );

        DailyPlan dailyPlan = org.mockito.Mockito.mock(DailyPlan.class);

        PlanMeal meal = PlanMeal.of(MealType.BREAKFAST, dailyPlan);

        FoodPortionAddRequestDTO dto =
                new FoodPortionAddRequestDTO(
                        2L,
                        200.0,
                        MeasureUnit.GRAM
                );

        PlanFoodPortion entity =
                PlanFoodPortionMapper.toEntity(dto, meal, food);

        assertEquals(meal, entity.getMeal());
        assertEquals(food, entity.getFood());
        assertEquals(200.0, entity.getQuantity());
        assertEquals(MeasureUnit.GRAM, entity.getUnit());
    }
}