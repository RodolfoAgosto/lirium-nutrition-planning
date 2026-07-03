package com.lirium.nutrition.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.lirium.nutrition.dto.request.*;
import com.lirium.nutrition.dto.response.*;
import com.lirium.nutrition.model.entity.DailyPlan;
import com.lirium.nutrition.model.entity.Food;
import com.lirium.nutrition.model.entity.PlanFoodPortion;
import com.lirium.nutrition.model.entity.PlanMeal;
import com.lirium.nutrition.model.enums.FoodCategory;
import com.lirium.nutrition.model.enums.MealType;
import com.lirium.nutrition.model.enums.MeasureUnit;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class PlanMealMapperTest {

    @Test
    void toResponse_shouldMapEntityToResponseDTO() {

        DailyPlan dailyPlan = Mockito.mock(DailyPlan.class);
        ReflectionTestUtils.setField(dailyPlan, "id", 100L);
        when(dailyPlan.getId()).thenReturn(100L);
        PlanMeal meal = PlanMeal.of(MealType.BREAKFAST, dailyPlan);
        ReflectionTestUtils.setField(meal, "id", 10L);

        Food food = Food.of(
                "Apple",
                52,
                1,
                14,
                0,
                FoodCategory.FRUIT,
                Set.of(MealType.BREAKFAST)
        );
        ReflectionTestUtils.setField(food, "id", 20L);

        PlanFoodPortion portion =
                PlanFoodPortion.of(meal, food, 150.0, MeasureUnit.GRAM);
        ReflectionTestUtils.setField(portion, "id", 30L);

        meal.addFoodPortion(portion);

        PlanMealResponseDTO dto = PlanMealMapper.toResponse(meal);

        assertEquals(10L, dto.id());
        assertEquals("BREAKFAST", dto.type());
        assertEquals(100L, dto.dailyPlanId());

        assertEquals(1, dto.foods().size());

        PlanFoodPortionSummaryDTO summary = dto.foods().getFirst();

        assertEquals(30L, summary.id());
        assertEquals(20L, summary.foodId());
        assertEquals(150.0, summary.quantity());
        assertEquals(MeasureUnit.GRAM, summary.uni());
    }

    @Test
    void toSummary_shouldMapEntityToSummaryDTO() {

        DailyPlan dailyPlan = Mockito.mock(DailyPlan.class);

        PlanMeal meal = PlanMeal.of(MealType.DINNER, dailyPlan);
        ReflectionTestUtils.setField(meal, "id", 50L);

        PlanMealSummaryDTO dto = PlanMealMapper.toSummary(meal);

        assertEquals(50L, dto.id());
        assertEquals("DINNER", dto.type());
    }

    @Test
    void toEntity_shouldCreatePlanMeal() {

        DailyPlan dailyPlan = Mockito.mock(DailyPlan.class);

        PlanMealCreateRequestDTO dto =
                new PlanMealCreateRequestDTO(
                        "BREAKFAST",
                        100L,
                        List.of()
                );

        PlanMeal entity = PlanMealMapper.toEntity(dto, dailyPlan);

        assertEquals(MealType.BREAKFAST, entity.getType());
        assertEquals(dailyPlan, entity.getDailyPlan());
        assertTrue(entity.getFoodPortions().isEmpty());
    }

    @Test
    void updateEntity_shouldNotThrowException() {

        DailyPlan dailyPlan = Mockito.mock(DailyPlan.class);

        PlanMeal meal = PlanMeal.of(MealType.BREAKFAST, dailyPlan);

        PlanMealUpdateRequestDTO dto =
                new PlanMealUpdateRequestDTO(
                        "DINNER",
                        List.of()
                );

        assertDoesNotThrow(() ->
                PlanMealMapper.updateEntity(meal, dto)
        );

        // Actualmente el mapper no modifica el tipo porque
        // entity.changeType(type) está comentado.
        assertEquals(MealType.BREAKFAST, meal.getType());
    }
}