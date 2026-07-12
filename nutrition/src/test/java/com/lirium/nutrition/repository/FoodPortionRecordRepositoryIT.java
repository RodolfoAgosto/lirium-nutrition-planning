package com.lirium.nutrition.repository;

import com.lirium.nutrition.model.entity.*;
import com.lirium.nutrition.model.enums.FoodCategory;
import com.lirium.nutrition.model.enums.MealType;
import com.lirium.nutrition.model.enums.MeasureUnit;
import com.lirium.nutrition.model.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@EnableJpaAuditing
class FoodPortionRecordRepositoryIT {

    @Autowired
    private FoodPortionRecordRepository repository;

    @Autowired
    private TestEntityManager em;

    @Test
    void shouldFindByMeal() {

        User user = new User("a@test.com", "123", "Juan", "Perez", Role.PATIENT);
        em.persist(user);

        PatientProfile patient = user.getPatientProfile();

        DailyRecord record = DailyRecord.of(patient, LocalDate.now());
        em.persist(record);

        MealRecord meal = MealRecord.of(MealType.BREAKFAST, LocalDateTime.now(), record);
        em.persist(meal);

        Food food = Food.of(
                "Rice",
                130,
                2,
                28,
                0,
                FoodCategory.CARB,
                Set.of(MealType.LUNCH)
        );
        em.persist(food);

        FoodPortionRecord portion = FoodPortionRecord.of(
                meal,
                food,
                100.0,
                MeasureUnit.GRAM
        );

        em.persist(portion);
        em.flush();

        List<FoodPortionRecord> result =
                repository.findByMeal(meal);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMeal().getId())
                .isEqualTo(meal.getId());
    }
}