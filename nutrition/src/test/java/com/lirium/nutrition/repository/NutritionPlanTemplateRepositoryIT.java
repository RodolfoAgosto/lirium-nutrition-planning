package com.lirium.nutrition.repository;

import com.lirium.nutrition.infrastructure.config.JpaConfig;
import com.lirium.nutrition.model.entity.NutritionPlanTemplate;
import com.lirium.nutrition.model.enums.FoodTag;
import com.lirium.nutrition.model.enums.GoalType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaConfig.class)
class NutritionPlanTemplateRepositoryIT {

    @Autowired
    private NutritionPlanTemplateRepository repository;

    @Autowired
    private TestEntityManager em;

    @Test
    void shouldFindByName() {

        NutritionPlanTemplate template = createTemplate(
                "Low Carb",
                GoalType.WEIGHT_LOSS,
                Set.of(FoodTag.HONEY)
        );

        em.flush();
        em.clear();

        Optional<NutritionPlanTemplate> result =
                repository.findByName("Low Carb");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Low Carb");
    }

    @Test
    void shouldCheckExistsByName() {

        createTemplate(
                "Keto",
                GoalType.WEIGHT_LOSS,
                Set.of(FoodTag.HONEY)
        );

        em.flush();
        em.clear();

        boolean exists = repository.existsByName("Keto");

        assertThat(exists).isTrue();
    }

    @Test
    void shouldFindByTargetGoal() {

        createTemplate("Plan A", GoalType.WEIGHT_LOSS, Set.of(FoodTag.HONEY));

        NutritionPlanTemplate expected =
                createTemplate("Plan B", GoalType.MUSCLE_GAIN, Set.of(FoodTag.GLUTEN));

        em.flush();
        em.clear();

        List<NutritionPlanTemplate> result =
                repository.findByTargetGoal(GoalType.MUSCLE_GAIN);

        assertThat(result).containsExactly(expected);
    }

    @Test
    void shouldFindByExcludedTagsContains() {

        NutritionPlanTemplate template =
                createTemplate("Template 1", GoalType.WEIGHT_LOSS,
                        Set.of(FoodTag.HONEY, FoodTag.GLUTEN));

        createTemplate("Template 2", GoalType.MUSCLE_GAIN,
                Set.of(FoodTag.SOY));

        em.flush();
        em.clear();

        List<NutritionPlanTemplate> result =
                repository.findByExcludedTagsContains(FoodTag.GLUTEN);

        assertThat(result).containsExactly(template);
    }

    @Test
    void shouldDeleteByName() {

        createTemplate("To Delete", GoalType.WEIGHT_LOSS, Set.of(FoodTag.HONEY));

        NutritionPlanTemplate keep =
                createTemplate("Keep", GoalType.MUSCLE_GAIN, Set.of(FoodTag.SOY));

        em.flush();

        long deleted = repository.deleteByName("To Delete");

        em.flush();
        em.clear();

        assertThat(deleted).isEqualTo(1);

        List<NutritionPlanTemplate> remaining = repository.findAll();

        assertThat(remaining).containsExactly(keep);
    }

    // ---------------- helper ----------------

    private NutritionPlanTemplate createTemplate(
            String name,
            GoalType goal,
            Set<FoodTag> excludedTags
    ) {

        NutritionPlanTemplate template = NutritionPlanTemplate.of(
                name,
                "description",
                goal,
                30,
                40,
                30,
                excludedTags
        );

        em.persist(template);

        return template;
    }
}