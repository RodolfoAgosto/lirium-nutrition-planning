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
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaConfig.class)
class MealRecordRepositoryIT {

    @Autowired
    private MealRecordRepository repository;

    @Autowired
    private TestEntityManager em;

    @Test
    void shouldFindMealsByType() {

        PatientProfile patient = createPatient("patient1@test.com");

        DailyRecord daily = createDailyRecord(
                patient,
                LocalDate.now()
        );

        MealRecord breakfast = createMeal(
                daily,
                MealType.BREAKFAST,
                LocalDateTime.now().minusHours(5)
        );

        createMeal(
                daily,
                MealType.LUNCH,
                LocalDateTime.now().minusHours(1)
        );

        em.flush();
        em.clear();

        List<MealRecord> result =
                repository.findByType(MealType.BREAKFAST);

        assertThat(result)
                .containsExactly(breakfast);
    }

    @Test
    void shouldFindMealsBetweenDates() {

        PatientProfile patient = createPatient("patient2@test.com");

        DailyRecord daily = createDailyRecord(
                patient,
                LocalDate.of(2026, 6, 1)
        );

        createMeal(
                daily,
                MealType.BREAKFAST,
                LocalDateTime.of(2026, 6, 1, 8, 0)
        );

        MealRecord expected = createMeal(
                daily,
                MealType.LUNCH,
                LocalDateTime.of(2026, 6, 2, 13, 0)
        );

        createMeal(
                daily,
                MealType.DINNER,
                LocalDateTime.of(2026, 6, 5, 20, 0)
        );

        em.flush();
        em.clear();

        List<MealRecord> result =
                repository.findByEatenAtBetween(
                        LocalDateTime.of(2026, 6, 2, 0, 0),
                        LocalDateTime.of(2026, 6, 3, 0, 0)
                );

        assertThat(result)
                .containsExactly(expected);
    }

    @Test
    void shouldFindMealsByTypeAndBetweenDates() {

        PatientProfile patient = createPatient("patient3@test.com");

        DailyRecord daily = createDailyRecord(
                patient,
                LocalDate.now()
        );

        MealRecord expected = createMeal(
                daily,
                MealType.LUNCH,
                LocalDateTime.of(2026, 6, 2, 13, 0)
        );

        createMeal(
                daily,
                MealType.BREAKFAST,
                LocalDateTime.of(2026, 6, 2, 8, 0)
        );

        createMeal(
                daily,
                MealType.LUNCH,
                LocalDateTime.of(2026, 6, 10, 13, 0)
        );

        em.flush();
        em.clear();

        List<MealRecord> result =
                repository.findByTypeAndEatenAtBetween(
                        MealType.LUNCH,
                        LocalDateTime.of(2026, 6, 1, 0, 0),
                        LocalDateTime.of(2026, 6, 3, 0, 0)
                );

        assertThat(result)
                .containsExactly(expected);
    }

    @Test
    void shouldDeleteMealsBeforeDate() {

        PatientProfile patient = createPatient("patient4@test.com");

        DailyRecord daily = createDailyRecord(
                patient,
                LocalDate.now()
        );

        createMeal(
                daily,
                MealType.BREAKFAST,
                LocalDateTime.of(2026, 6, 1, 8, 0)
        );

        createMeal(
                daily,
                MealType.LUNCH,
                LocalDateTime.of(2026, 6, 10, 13, 0)
        );

        em.flush();
        em.clear();

        long deleted = repository.deleteByEatenAtBefore(
                LocalDateTime.of(2026, 6, 5, 0, 0)
        );

        em.flush();
        em.clear();

        assertThat(deleted).isEqualTo(1);

        assertThat(repository.findByType(MealType.BREAKFAST))
                .isEmpty();

        assertThat(repository.findByType(MealType.LUNCH))
                .hasSize(1);

        assertThat(repository.count())
                .isEqualTo(1);
    }

    @Test
    void shouldReturnEmptyListWhenNoMealsExist() {

        List<MealRecord> result =
                repository.findByType(MealType.BREAKFAST);

        assertThat(result).isEmpty();
    }

    private PatientProfile createPatient(String email) {

        User user = new User(
                email,
                "123456",
                "Juan",
                "Perez",
                Role.PATIENT
        );

        em.persist(user);

        return user.getPatientProfile();
    }

    private DailyRecord createDailyRecord(
            PatientProfile patient,
            LocalDate date
    ) {

        DailyRecord record = DailyRecord.of(patient, date);

        em.persist(record);

        return record;
    }

    private MealRecord createMeal(
            DailyRecord dailyRecord,
            MealType type,
            LocalDateTime eatenAt
    ) {

        MealRecord meal =
                MealRecord.of(type, eatenAt, dailyRecord);

        dailyRecord.addMeal(meal);

        em.persist(meal);

        return meal;
    }
}