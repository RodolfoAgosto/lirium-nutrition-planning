package com.lirium.nutrition.repository;

import com.lirium.nutrition.infrastructure.config.JpaConfig;
import com.lirium.nutrition.model.entity.DailyRecord;
import com.lirium.nutrition.model.entity.MealRecord;
import com.lirium.nutrition.model.entity.PatientProfile;
import com.lirium.nutrition.model.entity.User;
import com.lirium.nutrition.model.enums.MealType;
import com.lirium.nutrition.model.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaConfig.class)
class DailyRecordRepositoryTest {

    @Autowired
    private DailyRecordRepository repository;

    @Autowired
    private TestEntityManager em;

    private User createPatientUser(String email) {
        User user = new User(
                email,
                "123",
                "Juan",
                "Perez",
                Role.PATIENT
        );
        em.persist(user);
        return user;
    }

    private PatientProfile createPatient(User user) {
        PatientProfile profile = user.getPatientProfile();
        em.persist(profile);
        return profile;
    }

    private DailyRecord createRecord(PatientProfile patient, LocalDate date) {
        DailyRecord record = DailyRecord.of(patient, date);
        em.persist(record);
        return record;
    }

    private MealRecord createMeal(DailyRecord record, LocalDateTime time) {
        // simplificado: sin PlanMeal
        MealRecord meal = MealRecord.of(MealType.BREAKFAST, time, record);
        record.addMeal(meal);
        em.persist(meal);
        return meal;
    }

    @Test
    void shouldFindByPatientIdAndDate() {

        User user = createPatientUser("a@test.com");
        PatientProfile patient = createPatient(user);

        DailyRecord record = createRecord(patient, LocalDate.of(2026, 6, 30));

        em.flush();

        Optional<DailyRecord> result =
                repository.findByPatient_IdAndDate(
                        patient.getId(),
                        LocalDate.of(2026, 6, 30)
                );

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(record.getId());
    }

    @Test
    void shouldReturnRecordsOrderedByDateDesc() {

        User user = createPatientUser("b@test.com");
        PatientProfile patient = createPatient(user);

        createRecord(patient, LocalDate.of(2026, 6, 28));
        createRecord(patient, LocalDate.of(2026, 6, 29));
        createRecord(patient, LocalDate.of(2026, 6, 30));

        em.flush();

        List<DailyRecord> result =
                repository.findByPatient_IdOrderByDateDesc(patient.getId());

        assertThat(result).hasSize(3);

        assertThat(result.get(0).getDate()).isEqualTo(LocalDate.of(2026, 6, 30));
        assertThat(result.get(1).getDate()).isEqualTo(LocalDate.of(2026, 6, 29));
        assertThat(result.get(2).getDate()).isEqualTo(LocalDate.of(2026, 6, 28));
    }

    @Test
    void shouldFindBetweenDates() {

        User user = createPatientUser("c@test.com");
        PatientProfile patient = createPatient(user);

        createRecord(patient, LocalDate.of(2026, 6, 20));
        createRecord(patient, LocalDate.of(2026, 6, 21));
        createRecord(patient, LocalDate.of(2026, 6, 22));
        createRecord(patient, LocalDate.of(2026, 6, 23));
        createRecord(patient, LocalDate.of(2026, 6, 24));

        em.flush();

        List<DailyRecord> result =
                repository.findByPatient_IdAndDateBetween(
                        patient.getId(),
                        LocalDate.of(2026, 6, 21),
                        LocalDate.of(2026, 6, 23)
                );

        assertThat(result).hasSize(3);
    }

    @Test
    void shouldFindWithMealsFetched() {

        User user = createPatientUser("d@test.com");
        PatientProfile patient = createPatient(user);

        DailyRecord record = createRecord(patient, LocalDate.of(2026, 6, 30));

        createMeal(record, LocalDateTime.now().minusHours(2));
        createMeal(record, LocalDateTime.now().minusHours(1));

        em.flush();
        em.clear();

        List<DailyRecord> result =
                repository.findByPatient_IdAndDateBetweenWithMeals(
                        patient.getId(),
                        LocalDate.of(2026, 6, 29),
                        LocalDate.of(2026, 6, 30)
                );

        assertThat(result).hasSize(1);

        DailyRecord loaded = result.get(0);

        // importante: meals deben estar inicializados (FETCH JOIN)
        assertThat(loaded.getMeals()).hasSize(2);
    }

    @Test
    void shouldFindByMealId() {

        User user = createPatientUser("e@test.com");
        PatientProfile patient = createPatient(user);

        DailyRecord record = createRecord(patient, LocalDate.of(2026, 6, 30));

        MealRecord meal = createMeal(record, LocalDateTime.now().minusHours(1));

        em.flush();

        Optional<DailyRecord> result =
                repository.findByMealRecordId(meal.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(record.getId());
    }
}