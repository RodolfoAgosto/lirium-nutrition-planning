package com.lirium.nutrition.mapper;

import com.lirium.nutrition.dto.request.FoodPortionCreateDTO;
import com.lirium.nutrition.dto.request.MealRecordCreateRequestDTO;
import com.lirium.nutrition.dto.response.*;
import com.lirium.nutrition.model.entity.*;
import com.lirium.nutrition.model.enums.FoodCategory;
import com.lirium.nutrition.model.enums.MealType;
import com.lirium.nutrition.model.enums.MeasureUnit;
import com.lirium.nutrition.model.enums.Role;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class MealRecordMapperTest {

    @Test
    void shouldMapEntityToResponse() {

        User user = new User(
                "user@test.com",
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
                LocalDateTime.now().minusHours(1),
                dailyRecord
        );

        Food rice = Food.of(
                "Rice",
                360,
                7,
                80,
                1,
                FoodCategory.CARB,
                Set.of(MealType.LUNCH)
        );

        meal.addFoodPortion(
                rice,
                100.0,
                MeasureUnit.GRAM
        );

        MealRecordResponseDTO dto = MealRecordMapper.toResponse(meal);

        assertThat(dto.type()).isEqualTo(MealType.LUNCH);
        assertThat(dto.overridden()).isFalse();
        assertThat(dto.notes()).isNull();
        assertThat(dto.eatenAt()).isEqualTo(meal.getEatenAt());

        assertThat(dto.portions()).hasSize(1);

        FoodPortionRecordResponseDTO portion = dto.portions().getFirst();

        assertThat(portion.foodName()).isEqualTo("Rice");
        assertThat(portion.quantity()).isEqualTo(100.0);
        assertThat(portion.unit()).isEqualTo(MeasureUnit.GRAM);
        assertThat(portion.calories()).isEqualTo(360);
        assertThat(portion.protein()).isEqualTo(7);
        assertThat(portion.carbs()).isEqualTo(80);
        assertThat(portion.fat()).isEqualTo(1);
    }

    @Test
    void shouldMapEntityToSummary() {

        User user = new User(
                "user@test.com",
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
                MealType.BREAKFAST,
                LocalDateTime.now().minusHours(2),
                dailyRecord
        );

        MealRecordSummaryDTO dto = MealRecordMapper.toSummary(meal);

        assertThat(dto.type()).isEqualTo(MealType.BREAKFAST);
        assertThat(dto.eatenAt()).isEqualTo(meal.getEatenAt());
        assertThat(dto.overridden()).isFalse();
    }

    @Test
    void shouldMapCreateDtoToEntity() {

        Food rice = Food.of(
                "Rice",
                360,
                7,
                80,
                1,
                FoodCategory.CARB,
                Set.of(MealType.LUNCH)
        );

        MealRecordCreateRequestDTO dto =
                new MealRecordCreateRequestDTO(
                        "LUNCH",
                        LocalDateTime.now().minusHours(1),
                        "Good meal",
                        List.of(
                                new FoodPortionCreateDTO(
                                        1L,
                                        150.0,
                                        MeasureUnit.GRAM
                                )
                        )
                );

        User user = new User(
                "user@test.com",
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

        MealRecord meal = MealRecordMapper.toEntity(
                dto,
                List.of(rice),
                dailyRecord
        );

        assertThat(meal.getType()).isEqualTo(MealType.LUNCH);
        assertThat(meal.getNotes()).isEqualTo("Good meal");
        assertThat(meal.isOverridden()).isFalse();

        assertThat(meal.getFoodPortions()).hasSize(1);

        FoodPortionRecord portion = meal.getFoodPortions().getFirst();

        assertThat(portion.getFood()).isEqualTo(rice);
        assertThat(portion.getQuantity()).isEqualTo(150.0);
        assertThat(portion.getUnit()).isEqualTo(MeasureUnit.GRAM);
    }
}