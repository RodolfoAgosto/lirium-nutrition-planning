package com.lirium.nutrition.model.entity;

import com.lirium.nutrition.model.enums.GoalType;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NutritionPlanTest {


    private PatientProfile patient() {
        User user = new User(
                "test@test.com",
                "password",
                "Juan",
                "Perez",
                null
        );

        return new PatientProfile(user);
    }


    @Test
    void shouldGenerateNutritionPlanAsDraft() {

        NutritionPlan plan = NutritionPlan.generate(
                GoalType.WEIGHT_LOSS,
                2000,
                120,
                200,
                60,
                patient()
        );


        assertThat(plan.isDraft()).isTrue();
        assertThat(plan.isActive()).isFalse();
    }


    @Test
    void shouldCompleteBasicInformation() {

        NutritionPlan plan = NutritionPlan.generate(
                GoalType.WEIGHT_LOSS,
                2000,
                120,
                200,
                60,
                patient()
        );


        plan.completeBasic(
                "Plan definición",
                "Plan para bajar grasa"
        );


        assertThat(plan.getName())
                .isEqualTo("Plan definición");

        assertThat(plan.getDescription())
                .isEqualTo("Plan para bajar grasa");
    }


    @Test
    void shouldNotCompleteWithBlankName() {

        NutritionPlan plan = NutritionPlan.generate(
                GoalType.WEIGHT_LOSS,
                2000,
                120,
                200,
                60,
                patient()
        );


        assertThatThrownBy(() ->
                plan.completeBasic(
                        "",
                        "description"
                )
        )
                .isInstanceOf(IllegalArgumentException.class);
    }


    @Test
    void shouldActivateDraftPlan() {

        NutritionPlan plan = NutritionPlan.generate(
                GoalType.WEIGHT_LOSS,
                2000,
                120,
                200,
                60,
                patient()
        );


        LocalDate date = LocalDate.of(2026,1,1);

        plan.activate(date);


        assertThat(plan.isActive()).isTrue();
        assertThat(plan.getStartDate()).isEqualTo(date);
    }


    @Test
    void shouldNotActivateNonDraftPlan() {

        NutritionPlan plan = NutritionPlan.generate(
                GoalType.WEIGHT_LOSS,
                2000,
                120,
                200,
                60,
                patient()
        );


        plan.activate(LocalDate.now());


        assertThatThrownBy(() ->
                plan.activate(LocalDate.now())
        )
                .isInstanceOf(IllegalStateException.class);
    }


    @Test
    void shouldDeactivateActivePlan() {

        NutritionPlan plan = NutritionPlan.generate(
                GoalType.WEIGHT_LOSS,
                2000,
                120,
                200,
                60,
                patient()
        );


        plan.activate(LocalDate.now());

        plan.deactivate();


        assertThat(plan.isActive()).isFalse();
    }


    @Test
    void shouldRejectInvalidUpdateDates() {

        NutritionPlan plan = NutritionPlan.generate(
                GoalType.WEIGHT_LOSS,
                2000,
                120,
                200,
                60,
                patient()
        );


        assertThatThrownBy(() ->
                plan.update(
                        null,
                        null,
                        LocalDate.of(2026,2,1),
                        LocalDate.of(2026,1,1),
                        null,
                        null,
                        null,
                        null,
                        null
                )
        )
                .isInstanceOf(IllegalArgumentException.class);
    }


    @Test
    void shouldRejectNegativeMacros() {

        NutritionPlan plan = NutritionPlan.generate(
                GoalType.WEIGHT_LOSS,
                2000,
                120,
                200,
                60,
                patient()
        );


        assertThatThrownBy(() ->
                plan.update(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        -1,
                        null,
                        null
                )
        )
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldNotCompleteActivePlan() {

        NutritionPlan plan = createActivePlan();

        assertThatThrownBy(() ->
                plan.completeBasic(
                        "name",
                        "description"
                )
        )
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldRejectEndDateBeforeStartDate() {

        NutritionPlan plan = createPlan();

        assertThatThrownBy(() ->
                plan.update(
                        null,
                        null,
                        LocalDate.of(2026,1,10),
                        LocalDate.of(2026,1,1),
                        null,
                        null,
                        null,
                        null,
                        null
                )
        )
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectNullStartDateWhenActivating() {

        NutritionPlan plan = createPlan();

        assertThatThrownBy(() -> plan.activate(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldNotDeactivateDraftPlan() {

        NutritionPlan plan = createPlan();

        assertThatThrownBy(plan::deactivate)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldCloseActivePlan() {

        NutritionPlan plan = createActivePlan();

        LocalDate end = LocalDate.now();

        plan.close(end);

        assertThat(plan.getEndDate()).isEqualTo(end);
        assertThat(plan.isActive()).isFalse();
    }

    @Test
    void shouldNotCloseDraftPlan() {

        NutritionPlan plan = createPlan();

        assertThatThrownBy(() -> plan.close(LocalDate.now()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldRejectZeroCalories() {

        NutritionPlan plan = createPlan();

        assertThatThrownBy(() ->
                plan.update(
                        null, null, null, null,
                        null,
                        0,
                        null,
                        null,
                        null
                )
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectNegativeCarbs() {

        NutritionPlan plan = createPlan();

        assertThatThrownBy(() ->
                plan.update(
                        null, null, null, null,
                        null,
                        null,
                        null,
                        -1,
                        null
                )
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldUpdateAllFields() {

        NutritionPlan plan = createPlan();

        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 2, 1);

        plan.update(
                "Nuevo nombre",
                "Nueva descripción",
                start,
                end,
                GoalType.MUSCLE_GAIN,
                2500,
                180,
                250,
                80
        );

        assertThat(plan.getName()).isEqualTo("Nuevo nombre");
        assertThat(plan.getDescription()).isEqualTo("Nueva descripción");
        assertThat(plan.getStartDate()).isEqualTo(start);
        assertThat(plan.getEndDate()).isEqualTo(end);
        assertThat(plan.getTargetGoal()).isEqualTo(GoalType.MUSCLE_GAIN);
        assertThat(plan.getDailyCalories()).isEqualTo(2500);
        assertThat(plan.getProteinGrams()).isEqualTo(180);
        assertThat(plan.getCarbGrams()).isEqualTo(250);
        assertThat(plan.getFatGrams()).isEqualTo(80);
    }

    @Test
    void shouldRejectBlankDescription() {

        NutritionPlan plan = createPlan();

        assertThatThrownBy(() ->
                plan.completeBasic(
                        "Nombre",
                        ""
                )
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectNullName() {

        NutritionPlan plan = createPlan();

        assertThatThrownBy(() ->
                plan.completeBasic(
                        null,
                        "Descripción"
                )
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectNullDescription() {

        NutritionPlan plan = createPlan();

        assertThatThrownBy(() ->
                plan.completeBasic(
                        "Nombre",
                        null
                )
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectNegativeFatGrams() {

        NutritionPlan plan = createPlan();

        assertThatThrownBy(() ->
                plan.update(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        -1
                )
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectNullEndDateWhenClosing() {

        NutritionPlan plan = createActivePlan();

        assertThatThrownBy(() -> plan.close(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldAddDailyPlan() {

        NutritionPlan plan = createPlan();

        DailyPlan dailyPlan =
                DailyPlan.of(
                        DayOfWeek.MONDAY,
                        plan
                );

        plan.addDailyPlan(dailyPlan);

        assertThat(plan.getWeek())
                .containsExactly(dailyPlan);
    }

    @Test
    void shouldRejectNullDailyPlan() {

        NutritionPlan plan = createPlan();

        assertThatThrownBy(() ->
                plan.addDailyPlan(null)
        ).isInstanceOf(NullPointerException.class);
    }



    private NutritionPlan createActivePlan() {

        NutritionPlan plan = NutritionPlan.generate(
                GoalType.WEIGHT_LOSS,
                2000,
                120,
                200,
                60,
                patient()
        );

        plan.activate(LocalDate.now());

        return plan;
    }

    private NutritionPlan createPlan() {

        return NutritionPlan.generate(
                GoalType.WEIGHT_LOSS,
                2000,
                120,
                200,
                60,
                patient()
        );
    }
}