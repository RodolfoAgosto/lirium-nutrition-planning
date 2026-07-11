package com.lirium.nutrition.model.entity;

import com.lirium.nutrition.model.enums.*;
import com.lirium.nutrition.model.valueobject.Height;
import com.lirium.nutrition.model.valueobject.Weight;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PatientProfileTest {


    private User createUser() {

        return new User(
                "patient@test.com",
                "password",
                "Juan",
                "Perez",
                Role.PATIENT
        );
    }


    private Restriction createRestriction() {

        return Restriction.builder()
                .code("GLUTEN")
                .name("Gluten")
                .category(RestrictionCategory.INTOLERANCES)
                .description("Avoid gluten")
                .build();
    }


    @Test
    void shouldCreateProfileWithUser() {

        PatientProfile profile =
                new PatientProfile(createUser());


        assertThat(profile.getUser())
                .isNotNull();
    }


    @Test
    void shouldRejectNullUser() {

        assertThatThrownBy(() ->
                new PatientProfile(null)
        )
                .isInstanceOf(NullPointerException.class);
    }


    @Test
    void shouldAddRestriction() {

        PatientProfile profile =
                new PatientProfile(createUser());

        Restriction restriction =
                createRestriction();


        profile.addRestriction(restriction);


        assertThat(profile.getRestrictions())
                .contains(restriction);
    }


    @Test
    void shouldUpdatePatientInformation() {

        PatientProfile profile =
                new PatientProfile(createUser());


        profile.update(
                Sex.MALE,
                ActivityLevel.VERY_ACTIVE,
                Weight.of(80000),
                Height.of(180),
                "No medical issues",
                null,
                null,
                GoalType.WEIGHT_LOSS
        );


        assertThat(profile.getSex())
                .isEqualTo(Sex.MALE);

        assertThat(profile.getActivityLevel())
                .isEqualTo(ActivityLevel.VERY_ACTIVE);

        assertThat(profile.getWeight())
                .isEqualTo(Weight.of(80000));

        assertThat(profile.getHeight())
                .isEqualTo(Height.of(180));

        assertThat(profile.getPrimaryGoal())
                .isEqualTo(GoalType.WEIGHT_LOSS);
    }


    @Test
    void shouldReplaceRestrictionsWhenUpdating() {

        PatientProfile profile =
                new PatientProfile(createUser());


        Restriction restriction =
                createRestriction();


        profile.update(
                null,
                null,
                null,
                null,
                null,
                Set.of(restriction),
                null,
                null
        );


        assertThat(profile.getRestrictions())
                .containsExactly(restriction);
    }


    @Test
    void shouldUpdateNutritionProfile() {

        PatientProfile profile =
                new PatientProfile(createUser());


        profile.updateNutritionProfile(
                Height.of(175),
                Weight.of(75000),
                ActivityLevel.MODERATE,
                GoalType.MUSCLE_GAIN
        );


        assertThat(profile.getHeight())
                .isEqualTo(Height.of(175));

        assertThat(profile.getWeight())
                .isEqualTo(Weight.of(75000));

        assertThat(profile.getActivityLevel())
                .isEqualTo(ActivityLevel.MODERATE);

        assertThat(profile.getPrimaryGoal())
                .isEqualTo(GoalType.MUSCLE_GAIN);
    }
}