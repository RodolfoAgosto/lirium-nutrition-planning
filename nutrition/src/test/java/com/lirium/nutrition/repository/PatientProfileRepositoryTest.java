package com.lirium.nutrition.repository;

import com.lirium.nutrition.infrastructure.config.JpaConfig;
import com.lirium.nutrition.model.entity.*;
import com.lirium.nutrition.model.enums.GoalType;
import com.lirium.nutrition.model.enums.Role;
import com.lirium.nutrition.model.enums.Sex;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaConfig.class)
class PatientProfileRepositoryTest {

    @Autowired
    private PatientProfileRepository repository;

    @Autowired
    private TestEntityManager em;

    // ---------------- TESTS ----------------

    @Test
    void shouldFindByUser() {

        User user = createUser();
        PatientProfile profile = user.getPatientProfile();

        em.flush();
        em.clear();

        Optional<PatientProfile> result =
                repository.findByUser(user);

        assertThat(result).isPresent();
        assertThat(result.get().getUser().getEmail())
                .isEqualTo(user.getEmail());
    }

    @Test
    void shouldFindBySex() {

        PatientProfile p1 = createPatient(Sex.MALE);
        PatientProfile p2 = createPatient(Sex.FEMALE);

        em.flush();
        em.clear();

        List<PatientProfile> result =
                repository.findBySex(Sex.FEMALE);

        assertThat(result).containsExactly(p2);
    }

    @Test
    void shouldFindByPrimaryGoal() {

        PatientProfile p1 = createPatientWithGoal(GoalType.WEIGHT_LOSS);
        PatientProfile p2 = createPatientWithGoal(GoalType.MUSCLE_GAIN);

        em.flush();
        em.clear();

        List<PatientProfile> result =
                repository.findByPrimaryGoal(GoalType.MUSCLE_GAIN);

        assertThat(result).containsExactly(p2);
    }

    @Test
    void shouldSearchPatients() {

        createPatientNamed("Juan", "Perez", "111");
        createPatientNamed("Maria", "Gomez", "222");

        em.flush();
        em.clear();

        var result =
                repository.searchPatients(
                        "Juan",
                        null,
                        null,
                        null
                );

        assertThat(result).hasSize(1);
        assertThat(result.get(0).firstName()).isEqualTo("Juan");
    }

    @Test
    void shouldFindByUserIdFetchUser() {

        User user = createUser();

        em.flush();
        em.clear();

        Optional<PatientProfile> result =
                repository.findByUserIdFetchUser(user.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getUser().getEmail())
                .isEqualTo(user.getEmail());
    }

    @Test
    void shouldFindByUserId() {

        User user = createUser();

        em.flush();
        em.clear();

        Optional<PatientProfile> result =
                repository.findByUserId(user.getId());

        assertThat(result).isPresent();
    }

    // ---------------- HELPERS ----------------

    private User createUser() {

        String email = "user-" + UUID.randomUUID() + "@test.com";

        User user = new User(
                email,
                "pass",
                "Juan",
                "Perez",
                Role.PATIENT
        );

        em.persist(user);
        return user;
    }

    private PatientProfile createPatient(Sex sex) {

        User user = createUser();
        PatientProfile profile = user.getPatientProfile();

        setField(profile, "sex", sex);

        em.persist(user);
        return profile;
    }

    private PatientProfile createPatientWithGoal(GoalType goal) {

        User user = createUser();
        PatientProfile profile = user.getPatientProfile();

        setField(profile, "primaryGoal", goal);

        em.persist(user);
        return profile;
    }

    private PatientProfile createPatientNamed(
            String first,
            String last,
            String dni
    ) {

        User user = createUser();

        setField(user, "firstName", first);
        setField(user, "lastName", last);
        setField(user, "dni", dni);

        em.persist(user);
        return user.getPatientProfile();
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

    private PatientProfile createPatientWithGoal(String email) {

        User user = new User(
                email + UUID.randomUUID(),
                "123456",
                "Juan",
                "Perez",
                Role.PATIENT
        );

        em.persist(user);
        em.flush();

        return user.getPatientProfile();
    }

}