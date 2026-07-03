package com.lirium.nutrition.repository;

import com.lirium.nutrition.infrastructure.config.JpaConfig;
import com.lirium.nutrition.model.entity.Food;
import com.lirium.nutrition.model.enums.FoodCategory;
import com.lirium.nutrition.model.enums.FoodTag;
import com.lirium.nutrition.model.enums.MealType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(JpaConfig.class)
class FoodRepositoryTest {

    @Autowired
    private FoodRepository repository;

    @Autowired
    private TestEntityManager em;

    @Test
    void shouldExistsByName() {

        createFood(
                "Banana",
                Set.of(MealType.BREAKFAST),
                Set.of()
        );

        em.flush();

        assertThat(repository.existsByName("Banana")).isTrue();
        assertThat(repository.existsByName("Pizza")).isFalse();
    }

    @Test
    void shouldFindFoodsByTag() {

        Food bread = createFood(
                "Bread",
                Set.of(MealType.BREAKFAST),
                Set.of(FoodTag.GLUTEN)
        );

        createFood(
                "Milk",
                Set.of(MealType.BREAKFAST),
                Set.of(FoodTag.LACTOSE)
        );

        em.flush();

        Set<Food> result =
                repository.findByFoodTagsIn(
                        Set.of(FoodTag.GLUTEN)
                );

        assertThat(result)
                .containsExactly(bread);
    }

    @Test
    void shouldFindSuitableFoods() {

        Food rice = createFood(
                "Rice",
                Set.of(MealType.LUNCH),
                Set.of()
        );

        createFood(
                "Bread",
                Set.of(MealType.LUNCH),
                Set.of(FoodTag.GLUTEN)
        );

        em.flush();

        List<Food> result =
                repository.findSuitableFoods(
                        MealType.LUNCH,
                        Set.of(FoodTag.GLUTEN)
                );

        assertThat(result)
                .containsExactly(rice);
    }

    private Food createFood(
            String name,
            Set<MealType> suitableFor,
            Set<FoodTag> tags
    ) {

        Food food = Food.of(
                name,
                100,
                10,
                20,
                5,
                FoodCategory.VEGETABLE,
                suitableFor
        );

        if (tags != null) {
            tags.forEach(food::addTag);
        }

        em.persist(food);

        return food;
    }

}