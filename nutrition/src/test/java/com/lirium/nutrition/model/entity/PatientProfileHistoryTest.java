package com.lirium.nutrition.model.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.lirium.nutrition.model.enums.*;
import com.lirium.nutrition.model.valueobject.Height;
import com.lirium.nutrition.model.valueobject.Weight;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashSet;
import org.junit.jupiter.api.Test;

class PatientProfileHistoryTest {

  private PatientProfile createProfile() {

    User user = new User("patient@test.com", "password", "Juan", "Perez", Role.PATIENT);

    PatientProfile profile = new PatientProfile(user);

    profile.update(
        Sex.MALE,
        ActivityLevel.VERY_ACTIVE,
        Weight.of(80000),
        Height.of(180),
        "No issues",
        null,
        null,
        GoalType.WEIGHT_LOSS);

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

    PatientProfile profile = createProfile();

    PatientProfileHistory history = new PatientProfileHistory(profile);

    assertThat(history.getPatientProfile()).isEqualTo(profile);

    assertThat(history.getWeight()).isEqualTo(Weight.of(80000));

    assertThat(history.getHeight()).isEqualTo(Height.of(180));

    assertThat(history.getMedicalNotes()).isEqualTo("No issues");

    assertThat(history.getPrimaryGoal()).isEqualTo(GoalType.WEIGHT_LOSS);

    assertThat(history.getVisitDate())
        .isEqualTo(LocalDate.now(ZoneId.of("America/Argentina/Buenos_Aires")));
  }

  @Test
  void shouldCopyRestrictionsFromProfile() {

    PatientProfile profile = createProfile();

    Restriction restriction = createRestriction();

    profile.addRestriction(restriction);

    PatientProfileHistory history = new PatientProfileHistory(profile);

    assertThat(history.getRestrictions()).containsExactly(restriction);
  }

  @Test
  void shouldNotShareRestrictionCollection() {

    PatientProfile profile = createProfile();

    Restriction restriction = createRestriction();

    profile.addRestriction(restriction);

    PatientProfileHistory history = new PatientProfileHistory(profile);

    profile.update(null, null, null, null, null, new HashSet<>(), null, null);

    assertThat(profile.getRestrictions()).doesNotContain(restriction);

    assertThat(history.getRestrictions()).contains(restriction);
  }

  @Test
  void shouldRejectNullProfile() {

    assertThatThrownBy(() -> new PatientProfileHistory(null))
        .isInstanceOf(NullPointerException.class);
  }
}
