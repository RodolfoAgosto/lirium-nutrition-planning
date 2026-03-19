package com.lirium.nutrition.infrastructure.config;

import com.lirium.nutrition.model.entity.Restriction;
import com.lirium.nutrition.model.enums.RestrictionCategory;
import com.lirium.nutrition.repository.RestrictionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Order(2)
public class RestrictionDataLoader implements CommandLineRunner {

    private final RestrictionRepository restrictionRepository;

    @Override
    public void run(String... args) {

        if (restrictionRepository.count() > 0) return;

        restrictionRepository.save(Restriction.builder()
                .code("GLUTEN_FREE").name("Gluten Free")
                .category(RestrictionCategory.INTOLERANCES)
                .description("Avoid gluten containing foods").build());

        restrictionRepository.save(Restriction.builder()
                .code("LACTOSE_FREE").name("Lactose Free")
                .category(RestrictionCategory.INTOLERANCES)
                .description("Avoid lactose and dairy products").build());

        restrictionRepository.save(Restriction.builder()
                .code("LOW_SODIUM").name("Low Sodium")
                .category(RestrictionCategory.PATHOLOGICAL)
                .description("Reduce sodium intake").build());

        restrictionRepository.save(Restriction.builder()
                .code("VEGAN").name("Vegan")
                .category(RestrictionCategory.DIETARY)
                .description("Avoid all animal products").build());

        restrictionRepository.save(Restriction.builder()
                .code("VEGETARIAN").name("Vegetarian")
                .category(RestrictionCategory.DIETARY)
                .description("Avoid meat and fish").build());
    }
}