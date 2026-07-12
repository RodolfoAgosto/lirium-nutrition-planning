package com.lirium.nutrition.repository;

import com.lirium.nutrition.infrastructure.config.JpaConfig;
import com.lirium.nutrition.model.entity.*;
        import com.lirium.nutrition.model.enums.GoalType;
import com.lirium.nutrition.model.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaConfig.class)
class PatientProfileHistoryRepositoryIT {

    @Autowired
    private PatientProfileHistoryRepository repository;

    @Autowired
    private TestEntityManager em;

    @Test
    void shouldFindByPatientProfileOrderByVisitDateDesc() {

        PatientProfile patient = createPatient();

        PatientProfileHistory h1 = createHistory(patient, LocalDate.of(2026, 1, 1));
        PatientProfileHistory h2 = createHistory(patient, LocalDate.of(2026, 1, 5));

        em.flush();
        em.clear();

        List<PatientProfileHistory> result =
                repository.findByPatientProfileOrderByVisitDateDesc(patient);

        assertThat(result).containsExactly(h2, h1);
    }

    @Test
    void shouldFindByPatientProfileAndVisitDateBetween() {

        PatientProfile patient = createPatient();

        createHistory(patient, LocalDate.of(2026, 1, 1));
        PatientProfileHistory h2 = createHistory(patient, LocalDate.of(2026, 1, 5));
        createHistory(patient, LocalDate.of(2026, 1, 10));

        em.flush();
        em.clear();

        List<PatientProfileHistory> result =
                repository.findByPatientProfileAndVisitDateBetween(
                        patient,
                        LocalDate.of(2026, 1, 2),
                        LocalDate.of(2026, 1, 8)
                );

        assertThat(result).containsExactly(h2);
    }

    @Test
    void shouldFindByPatientProfileAndPrimaryGoal() {

        PatientProfile patient = createPatient();

        PatientProfileHistory h1 = createHistoryWithGoal(patient, GoalType.WEIGHT_LOSS);
        PatientProfileHistory h2 = createHistoryWithGoal(patient, GoalType.MUSCLE_GAIN);

        em.flush();
        em.clear();

        List<PatientProfileHistory> result =
                repository.findByPatientProfileAndPrimaryGoal(
                        patient,
                        GoalType.MUSCLE_GAIN
                );

        assertThat(result).containsExactly(h2);
    }

    // ---------------- helpers ----------------

    private PatientProfile createPatient() {

        User user = new User(
                "patient@test.com",
                "pass",
                "Juan",
                "Perez",
                Role.PATIENT
        );

        em.persist(user);

        return user.getPatientProfile();
    }

    private PatientProfileHistory createHistory(
            PatientProfile patient,
            LocalDate visitDate
    ) {

        PatientProfileHistory history = new PatientProfileHistory(patient);

        // override visit date (entidad no tiene setter → se setea por reflexión o constructor alterno)
        setField(history, "visitDate", visitDate);

        em.persist(history);
        return history;
    }

    private PatientProfileHistory createHistoryWithGoal(
            PatientProfile patient,
            GoalType goal
    ) {

        PatientProfileHistory history = new PatientProfileHistory(patient);
        setField(history, "primaryGoal", goal);

        em.persist(history);
        return history;
    }

    private void setField(Object target, String field, Object value) {
        try {
            var f = target.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}