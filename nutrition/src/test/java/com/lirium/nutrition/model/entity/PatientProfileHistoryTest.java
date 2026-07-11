package com.lirium.nutrition.model.entity;

import com.lirium.nutrition.model.enums.*;
import com.lirium.nutrition.model.valueobject.Height;
import com.lirium.nutrition.model.valueobject.Weight;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PatientProfileHistoryTest {


    private PatientProfile createProfile() {

        User user = new User(
                "patient@test.com",
                "password",
                "Juan",
                "Perez",
                Role.PATIENT
        );

        PatientProfile profile =
                new PatientProfile(user);

        profile.update(
                Sex.MALE,
                ActivityLevel.VERY_ACTIVE,
                Weight.of(80000),
                Height.of(180),
                "No issues",
                null,
                null,
                GoalType.WEIGHT_LOSS
        );

        return profile;
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
    void shouldCreateHistoryFromPatientProfile() {

        PatientProfile profile =
                createProfile();


        PatientProfileHistory history =
                new PatientProfileHistory(profile);


        assertThat(history.getPatientProfile())
                .isEqualTo(profile);

        assertThat(history.getWeight())
                .isEqualTo(Weight.of(80000));

        assertThat(history.getHeight())
                .isEqualTo(Height.of(180));

        assertThat(history.getMedicalNotes())
                .isEqualTo("No issues");

        assertThat(history.getPrimaryGoal())
                .isEqualTo(GoalType.WEIGHT_LOSS);

        assertThat(history.getVisitDate())
                .isEqualTo(java.time.LocalDate.now());
    }


    @Test
    void shouldCopyRestrictionsFromProfile() {

        PatientProfile profile =
                createProfile();

        Restriction restriction =
                createRestriction();

        profile.addRestriction(restriction);


        PatientProfileHistory history =
                new PatientProfileHistory(profile);


        assertThat(history.getRestrictions())
                .containsExactly(restriction);
    }

    @Test
    void shouldNotShareRestrictionCollection() {

        PatientProfile profile = createProfile();

        Restriction restriction = createRestriction();

        profile.addRestriction(restriction);

        PatientProfileHistory history =
                new PatientProfileHistory(profile);


        profile.update(
                null,
                null,
                null,
                null,
                null,
                new HashSet<>(),
                null,
                null
        );


        assertThat(profile.getRestrictions())
                .doesNotContain(restriction);

        assertThat(history.getRestrictions())
                .contains(restriction);
    }


    @Test
    void shouldRejectNullProfile() {

        assertThatThrownBy(() ->
                new PatientProfileHistory(null)
        )
                .isInstanceOf(NullPointerException.class);
    }

}