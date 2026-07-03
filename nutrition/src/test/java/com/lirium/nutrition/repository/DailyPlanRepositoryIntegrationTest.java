package com.lirium.nutrition.repository;

import com.lirium.nutrition.model.entity.*;
import com.lirium.nutrition.model.enums.*;
import com.lirium.nutrition.model.valueobject.Height;
import com.lirium.nutrition.model.valueobject.Weight;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@DataJpaTest
@EnableJpaAuditing
class DailyPlanRepositoryIntegrationTest {

    @Autowired
    private DailyPlanRepository repository;

    @Autowired
    private TestEntityManager em;

    @Test
    void shouldFindDailyPlansByNutritionPlan() {

        NutritionPlan plan = persistNutritionPlan();

        DailyPlan monday = persistDailyPlan(plan, DayOfWeek.MONDAY);
        DailyPlan tuesday = persistDailyPlan(plan, DayOfWeek.TUESDAY);

        List<DailyPlan> result = repository.findByNutritionPlan(plan);

        assertThat(result)
                .hasSize(2)
                .containsExactlyInAnyOrder(monday, tuesday);
    }

    @Test
    void shouldFindDailyPlanByNutritionPlanAndDayOfWeek() {

        NutritionPlan plan = persistNutritionPlan();

        DailyPlan monday = persistDailyPlan(plan, DayOfWeek.MONDAY);

        Optional<DailyPlan> result =
                repository.findByNutritionPlanAndDayOfWeek(
                        plan,
                        DayOfWeek.MONDAY
                );

        assertThat(result)
                .isPresent()
                .contains(monday);
    }

    @Test
    void shouldDeleteDailyPlansByNutritionPlan() {

        NutritionPlan plan = persistNutritionPlan();

        persistDailyPlan(plan, DayOfWeek.MONDAY);
        persistDailyPlan(plan, DayOfWeek.TUESDAY);

        repository.deleteByNutritionPlan(plan);
        em.flush();

        List<DailyPlan> result = repository.findByNutritionPlan(plan);

        assertThat(result).isEmpty();
    }

    private NutritionPlan persistNutritionPlan() {

        User user = generateUser();

        em.persist(user);
        em.flush();

        PatientProfile profile = user.getPatientProfile();

        NutritionPlan plan = NutritionPlan.generate(
                GoalType.WEIGHT_MAINTENANCE,
                2000,
                150,
                250,
                60,
                profile
        );

        em.persist(plan);
        em.flush();

        return plan;
    }

    private DailyPlan persistDailyPlan(
            NutritionPlan plan,
            DayOfWeek dayOfWeek
    ) {

        DailyPlan dailyPlan = DailyPlan.of(dayOfWeek, plan);

        em.persist(dailyPlan);
        em.flush();

        return dailyPlan;
    }

    private User generateUser(){

        User user = new User(
                "john@test.com",
                "hash",
                "John",
                "Doe",
                Role.PATIENT
        );

        user.setDni("12345678");

        PatientProfile profile = user.getPatientProfile();

        profile.update(
                Sex.MALE,
                ActivityLevel.MODERATE,
                Weight.of(80000),
                Height.of(180),
                "notes",
                Set.of(),
                List.of(),
                GoalType.WEIGHT_MAINTENANCE
        );

        Restriction restriction = createRestriction();

        profile.addRestriction(restriction);

        return user;
    }

    private Restriction createRestriction() {
        Restriction r = new Restriction();
        r.setCode("NO_SUGAR");
        r.setName("No Sugar");
        r.setCategory(RestrictionCategory.DIETARY);
        r.setDescription("No sugar allowed");
        r.setExcludedTags(Set.of());
        return em.persist(r);
    }

}
