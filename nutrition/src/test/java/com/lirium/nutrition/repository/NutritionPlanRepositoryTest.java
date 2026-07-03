package com.lirium.nutrition.repository;

import com.lirium.nutrition.infrastructure.config.JpaConfig;
import com.lirium.nutrition.model.entity.*;
import com.lirium.nutrition.model.enums.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaConfig.class)
class NutritionPlanRepositoryTest {

    @Autowired
    private NutritionPlanRepository repository;

    @Autowired
    private TestEntityManager em;

    @Test
    void shouldFindByName() {

        PatientProfile patient = createPatient();

        NutritionPlan plan = createPlan(patient, "Plan A");

        em.flush();
        em.clear();

        Optional<NutritionPlan> result = repository.findByName("Plan A");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Plan A");
    }

    @Test
    void shouldFindByGoal() {

        PatientProfile patient = createPatient();

        createPlan(patient, "Plan A", GoalType.WEIGHT_LOSS);
        NutritionPlan expected = createPlan(patient, "Plan B", GoalType.MUSCLE_GAIN);

        em.flush();
        em.clear();

        List<NutritionPlan> result = repository.findByTargetGoal(GoalType.MUSCLE_GAIN);

        assertThat(result).containsExactly(expected);
    }

    @Test
    void shouldFindActivePlanByPatientAndStatus() {

        PatientProfile patient = createPatient();

        NutritionPlan draft = createPlan(patient, "Draft");
        draft.activate(LocalDate.now());

        NutritionPlan inactive = createPlan(patient, "Old");
        inactive.activate(LocalDate.now().minusDays(10));
        inactive.close(LocalDate.now().minusDays(1));

        em.flush();
        em.clear();

        Optional<NutritionPlan> result =
                repository.findByPatientProfileIdAndStatus(patient.getId(), PlanStatus.ACTIVE);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Draft");
    }

    @Test
    void shouldFindByPatientOrderByStartDateDesc() {

        PatientProfile patient = createPatient();

        NutritionPlan p1 = createPlan(patient, "Plan 1");
        p1.activate(LocalDate.now().minusDays(10));

        NutritionPlan p2 = createPlan(patient, "Plan 2");
        p2.activate(LocalDate.now().minusDays(5));

        em.flush();
        em.clear();

        List<NutritionPlan> result =
                repository.findByPatientProfileIdOrderByStartDateDesc(patient.getId());

        assertThat(result).containsExactly(p2, p1);
    }

    @Test
    void shouldDeleteByEndDateBefore() {

        PatientProfile patient = createPatient();

        NutritionPlan oldPlan = createPlan(patient, "Old");
        oldPlan.activate(LocalDate.now().minusDays(20));
        oldPlan.close(LocalDate.now().minusDays(10));

        NutritionPlan keep = createPlan(patient, "Keep");
        keep.activate(LocalDate.now());

        em.flush();

        long deleted =
                repository.deleteByEndDateBefore(LocalDate.now().minusDays(5));

        em.flush();
        em.clear();

        assertThat(deleted).isEqualTo(1);

        List<NutritionPlan> remaining = repository.findAll();

        assertThat(remaining).containsExactly(keep);
    }

    @Test
    void shouldReturnFalseWhenNoActivePlanExists() {

        PatientProfile patient = createPatient();

        em.flush();
        em.clear();

        boolean exists =
                repository.existsByPatientProfileIdAndStatus(patient.getId(), PlanStatus.ACTIVE);

        assertThat(exists).isFalse();
    }

    // ---------------- helpers ----------------

    private PatientProfile createPatient() {

        User user = new User(
                "test@mail.com",
                "pass",
                "Juan",
                "Perez",
                Role.PATIENT
        );

        em.persist(user);

        return user.getPatientProfile();
    }

    private NutritionPlan createPlan(PatientProfile patient, String name) {
        return createPlan(patient, name, GoalType.WEIGHT_LOSS);
    }

    private NutritionPlan createPlan(
            PatientProfile patient,
            String name,
            GoalType goal
    ) {

        NutritionPlan plan = NutritionPlan.generate(
                goal,
                2200,
                150,
                200,
                70,
                patient
        );

        plan.completeBasic(name, "desc");

        em.persist(plan);

        return plan;
    }
}