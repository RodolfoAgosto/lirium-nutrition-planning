package com.lirium.nutrition.repository;

import com.lirium.nutrition.infrastructure.config.JpaConfig;
import com.lirium.nutrition.model.entity.Restriction;
import com.lirium.nutrition.model.enums.RestrictionCategory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaConfig.class)
class RestrictionRepositoryIT {

    @Autowired
    private RestrictionRepository repository;

    @Autowired
    private TestEntityManager em;

    @Test
    void shouldFindRestrictionsByCodes() {

        Restriction gluten = createRestriction(
                "GLUTEN",
                "Gluten",
                RestrictionCategory.INTOLERANCES
        );

        Restriction lactose = createRestriction(
                "LACTOSE",
                "Lactose",
                RestrictionCategory.INTOLERANCES
        );

        createRestriction(
                "VEGAN",
                "Vegan",
                RestrictionCategory.DIETARY
        );

        em.flush();
        em.clear();

        Set<Restriction> result = repository.findByCodes(
                Set.of("GLUTEN", "LACTOSE")
        );

        assertThat(result)
                .extracting(Restriction::getCode)
                .containsExactlyInAnyOrder("GLUTEN", "LACTOSE");
    }

    @Test
    void shouldFindRestrictionsByCategory() {

        Restriction expected = createRestriction(
                "DIABETES",
                "Diabetes",
                RestrictionCategory.PATHOLOGICAL
        );

        createRestriction(
                "VEGAN",
                "Vegan",
                RestrictionCategory.DIETARY
        );

        em.flush();
        em.clear();

        List<Restriction> result =
                repository.findByCategory(RestrictionCategory.PATHOLOGICAL);

        assertThat(result)
                .hasSize(1);

        assertThat(result.getFirst().getId())
                .isEqualTo(expected.getId());
    }

    @Test
    void shouldFindRestrictionsByNameContainingIgnoreCase() {

        Restriction expected = createRestriction(
                "LACTOSE",
                "Lactose intolerance",
                RestrictionCategory.INTOLERANCES
        );

        createRestriction(
                "GLUTEN",
                "Gluten allergy",
                RestrictionCategory.INTOLERANCES
        );

        em.flush();
        em.clear();

        List<Restriction> result =
                repository.findByNameContainingIgnoreCase("lactose");

        assertThat(result)
                .hasSize(1);

        assertThat(result.getFirst().getId())
                .isEqualTo(expected.getId());
    }

    @Test
    void shouldReturnEmptyWhenCodeDoesNotExist() {

        createRestriction(
                "GLUTEN",
                "Gluten",
                RestrictionCategory.INTOLERANCES
        );

        em.flush();
        em.clear();

        Set<Restriction> result =
                repository.findByCodes(Set.of("UNKNOWN"));

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenCategoryHasNoRestrictions() {

        createRestriction(
                "GLUTEN",
                "Gluten",
                RestrictionCategory.INTOLERANCES
        );

        em.flush();
        em.clear();

        List<Restriction> result =
                repository.findByCategory(RestrictionCategory.DIETARY);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenNameDoesNotMatch() {

        createRestriction(
                "GLUTEN",
                "Gluten",
                RestrictionCategory.INTOLERANCES
        );

        em.flush();
        em.clear();

        List<Restriction> result =
                repository.findByNameContainingIgnoreCase("celiac");

        assertThat(result).isEmpty();
    }

    private Restriction createRestriction(
            String code,
            String name,
            RestrictionCategory category
    ) {

        Restriction restriction = Restriction.builder()
                .code(code)
                .name(name)
                .category(category)
                .description(name + " description")
                .excludedTags(new HashSet<>())
                .build();

        em.persist(restriction);

        return restriction;
    }

}