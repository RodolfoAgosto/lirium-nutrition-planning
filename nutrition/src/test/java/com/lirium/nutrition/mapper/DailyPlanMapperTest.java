package com.lirium.nutrition.mapper;

import com.lirium.nutrition.dto.request.DailyPlanCreateRequestDTO;
import com.lirium.nutrition.dto.response.DailyPlanResponseDTO;
import com.lirium.nutrition.dto.response.DailyPlanSummaryDTO;
import com.lirium.nutrition.model.entity.*;
        import com.lirium.nutrition.model.enums.GoalType;
import com.lirium.nutrition.model.enums.MealType;
import com.lirium.nutrition.model.enums.Role;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;

import static org.assertj.core.api.Assertions.assertThat;

class DailyPlanMapperTest {

    @Test
    void shouldMapEntityToResponse() {

        PatientProfile patient = createPatient();

        NutritionPlan plan = NutritionPlan.generate(
                GoalType.WEIGHT_LOSS,
                2000,
                150,
                200,
                70,
                patient
        );

        DailyPlan dailyPlan = DailyPlan.of(
                DayOfWeek.MONDAY,
                plan
        );

        PlanMeal breakfast = PlanMeal.of(
                MealType.BREAKFAST,
                dailyPlan
        );

        dailyPlan.addMeal(breakfast);

        DailyPlanResponseDTO dto =
                DailyPlanMapper.toResponse(dailyPlan);

        assertThat(dto.id()).isEqualTo(dailyPlan.getId());
        assertThat(dto.day()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(dto.nutritionPlanId()).isEqualTo(plan.getId());

        assertThat(dto.meals())
                .hasSize(1);

        assertThat(dto.meals().getFirst().type())
                .isEqualTo(MealType.BREAKFAST.toString());
    }

    @Test
    void shouldMapEntityToSummary() {

        PatientProfile patient = createPatient();

        NutritionPlan plan = NutritionPlan.generate(
                GoalType.WEIGHT_LOSS,
                2000,
                150,
                200,
                70,
                patient
        );

        DailyPlan dailyPlan = DailyPlan.of(
                DayOfWeek.TUESDAY,
                plan
        );

        DailyPlanSummaryDTO dto =
                DailyPlanMapper.toSummary(dailyPlan);

        assertThat(dto.id()).isEqualTo(dailyPlan.getId());
        assertThat(dto.day()).isEqualTo(DayOfWeek.TUESDAY);
    }

    @Test
    void shouldMapCreateRequestToEntity() {

        PatientProfile patient = createPatient();

        NutritionPlan plan = NutritionPlan.generate(
                GoalType.WEIGHT_LOSS,
                2000,
                150,
                200,
                70,
                patient
        );

        DailyPlanCreateRequestDTO dto = new DailyPlanCreateRequestDTO(
                DayOfWeek.MONDAY,
                1L,
                null
        );

        DailyPlan entity =
                DailyPlanMapper.toEntity(dto, plan);

        assertThat(entity.getDayOfWeek())
                .isEqualTo(DayOfWeek.MONDAY);

        assertThat(entity.getNutritionPlan())
                .isSameAs(plan);
    }

    private PatientProfile createPatient() {

        User user = new User(
                "patient@test.com",
                "123456",
                "Juan",
                "Perez",
                Role.PATIENT
        );

        return user.getPatientProfile();
    }
}