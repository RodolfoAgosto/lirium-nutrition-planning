package com.lirium.nutrition.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.lirium.nutrition.dto.request.FoodPortionRecordCreateRequestDTO;
import com.lirium.nutrition.dto.response.FoodPortionRecordResponseDTO;
import com.lirium.nutrition.dto.response.FoodPortionRecordSummaryDTO;
import com.lirium.nutrition.mapper.FoodPortionRecordMapper;
import com.lirium.nutrition.model.entity.*;
import com.lirium.nutrition.model.enums.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class FoodPortionRecordMapperTest {

    @Test
    void shouldMapEntityToResponse() {

        Food food = Food.of(
                "Rice",
                350,
                7,
                78,
                1,
                FoodCategory.CARB,
                Set.of(MealType.LUNCH)
        );

        User user = new User(
                "test@test.com",
                "123456",
                "Juan",
                "Perez",
                Role.PATIENT
        );

        PatientProfile patient = new PatientProfile(user);

        DailyRecord dailyRecord = DailyRecord.of(
                patient,
                LocalDate.now()
        );

        MealRecord meal = MealRecord.of(
                MealType.LUNCH,
                LocalDateTime.now(),
                dailyRecord
        );

        FoodPortionRecord portion = FoodPortionRecord.of(
                meal,
                food,
                100.0,
                MeasureUnit.GRAM
        );

        FoodPortionRecordResponseDTO dto =
                FoodPortionRecordMapper.toResponse(portion);

        assertThat(dto.id()).isNull();
        assertThat(dto.foodName()).isEqualTo("Rice");
        assertThat(dto.quantity()).isEqualTo(100.0);
        assertThat(dto.unit()).isEqualTo(MeasureUnit.GRAM);
        assertThat(dto.calories()).isEqualTo(350);
        assertThat(dto.protein()).isEqualTo(7);
        assertThat(dto.carbs()).isEqualTo(78);
        assertThat(dto.fat()).isEqualTo(1);
    }

    @Test
    void shouldMapEntityToSummary() {

        Food food = Food.of(
                "Rice",
                350,
                7,
                78,
                1,
                FoodCategory.CARB,
                Set.of(MealType.LUNCH)
        );

        User user = new User(
                "test@test.com",
                "123456",
                "Juan",
                "Perez",
                Role.PATIENT
        );

        PatientProfile patient = new PatientProfile(user);

        DailyRecord dailyRecord = DailyRecord.of(
                patient,
                LocalDate.now()
        );

        MealRecord meal = MealRecord.of(
                MealType.LUNCH,
                LocalDateTime.now(),
                dailyRecord
        );

        FoodPortionRecord portion = FoodPortionRecord.of(
                meal,
                food,
                150.0,
                MeasureUnit.GRAM
        );

        FoodPortionRecordSummaryDTO dto =
                FoodPortionRecordMapper.toSummary(portion);

        assertThat(dto.id()).isNull();
        assertThat(dto.foodId()).isNull(); // Food no está persistido
        assertThat(dto.quantity()).isEqualTo(150.0);
        assertThat(dto.unit()).isEqualTo(MeasureUnit.GRAM);
    }

    @Test
    void shouldMapCreateDtoToEntity() {

        Food food = Food.of(
                "Rice",
                350,
                7,
                78,
                1,
                FoodCategory.CARB,
                Set.of(MealType.LUNCH)
        );

        User user = new User(
                "test@test.com",
                "123456",
                "Juan",
                "Perez",
                Role.PATIENT
        );

        PatientProfile patient = new PatientProfile(user);

        DailyRecord dailyRecord = DailyRecord.of(
                patient,
                LocalDate.now()
        );

        MealRecord meal = MealRecord.of(
                MealType.LUNCH,
                LocalDateTime.now(),
                dailyRecord
        );

        FoodPortionRecordCreateRequestDTO dto =
                new FoodPortionRecordCreateRequestDTO(
                        1L,
                        2L,
                        80.0,
                        MeasureUnit.GRAM
                );

        FoodPortionRecord entity =
                FoodPortionRecordMapper.toEntity(dto, meal, food);

        assertThat(entity.getFood()).isSameAs(food);
        assertThat(entity.getMeal()).isSameAs(meal);
        assertThat(entity.getQuantity()).isEqualTo(80.0);
        assertThat(entity.getUnit()).isEqualTo(MeasureUnit.GRAM);
    }
}