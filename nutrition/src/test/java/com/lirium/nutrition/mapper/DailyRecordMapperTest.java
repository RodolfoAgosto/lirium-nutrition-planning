package com.lirium.nutrition.mapper;

import com.lirium.nutrition.dto.response.DailyRecordResponseDTO;
import com.lirium.nutrition.dto.response.FoodPortionRecordResponseDTO;
import com.lirium.nutrition.dto.response.MealRecordResponseDTO;
import com.lirium.nutrition.model.entity.*;
import com.lirium.nutrition.model.enums.*;
import com.lirium.nutrition.model.valueobject.Height;
import com.lirium.nutrition.model.valueobject.Weight;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DailyRecordMapperTest {

    @Test
    void shouldMapDailyRecordToResponse() {

        User user = new User(
                "user@test.com",
                "123456",
                "Juan",
                "Perez",
                Role.PATIENT
        );

        PatientProfile patient = new PatientProfile(user);
        patient.update(
                Sex.MALE,
                ActivityLevel.SEDENTARY,
                new Weight(300),
                new Height(180),
                null,
                null,
                null,
                GoalType.WEIGHT_MAINTENANCE
        );

        DailyRecord dailyRecord =
                DailyRecord.of(patient, LocalDate.of(2026, 6, 30));

        MealRecord meal = MealRecord.of(
                MealType.BREAKFAST,
                LocalDateTime.of(2026, 6, 30, 8, 0),
                dailyRecord
        );

        Food food = Food.of(
                "Rice",
                130,
                3,
                28,
                1,
                FoodCategory.CARB,
                Set.of(MealType.BREAKFAST)
        );

        meal.addFoodPortion(
                food,
                200.0,
                MeasureUnit.GRAM
        );

        dailyRecord.addMeal(meal);

        DailyRecordResponseDTO dto =
                DailyRecordMapper.toResponse(dailyRecord);

        assertThat(dto.id()).isNull(); // entidad no persistida
        assertThat(dto.date()).isEqualTo(LocalDate.of(2026, 6, 30));

        assertThat(dto.meals()).hasSize(1);

        MealRecordResponseDTO mealDto = dto.meals().getFirst();

        assertThat(mealDto.id()).isNull();
        assertThat(mealDto.type()).isEqualTo(MealType.BREAKFAST);
        assertThat(mealDto.overridden()).isFalse();
        assertThat(mealDto.notes()).isNull();
        assertThat(mealDto.eatenAt())
                .isEqualTo(LocalDateTime.of(2026, 6, 30, 8, 0));

        assertThat(mealDto.portions()).hasSize(1);

        FoodPortionRecordResponseDTO portion =
                mealDto.portions().getFirst();

        assertThat(portion.id()).isNull();
        assertThat(portion.foodName()).isEqualTo("Rice");
        assertThat(portion.quantity()).isEqualTo(200.0);
        assertThat(portion.unit()).isEqualTo(MeasureUnit.GRAM);

        // 200 g de un alimento con valores cada 100 g
        assertThat(portion.calories()).isEqualTo(260);
        assertThat(portion.protein()).isEqualTo(6);
        assertThat(portion.carbs()).isEqualTo(56);
        assertThat(portion.fat()).isEqualTo(2);
    }
}