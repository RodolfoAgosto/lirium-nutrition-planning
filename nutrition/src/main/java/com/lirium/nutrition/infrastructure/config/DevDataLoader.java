package com.lirium.nutrition.infrastructure.config;

        import com.lirium.nutrition.model.entity.*;
        import com.lirium.nutrition.model.enums.*;
        import com.lirium.nutrition.repository.*;
        import lombok.RequiredArgsConstructor;
        import org.springframework.boot.CommandLineRunner;
        import org.springframework.context.annotation.Profile;
        import org.springframework.stereotype.Component;

        import java.time.LocalDate;
        import java.util.List;
        import java.util.Set;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevDataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RestrictionRepository restrictionRepository;
    private final FoodRepository foodRepository;

    @Override
    public void run(String... args) {

        if(userRepository.count() > 0){
            return;
        }

        // ********************** USERS **************************

        User u1 = new User(
                "ana@test.com",
                "1234",
                "Ana",
                "Lopez"
        );

        u1.setBirthDate(LocalDate.of(1990,5,10));
        u1.setDni("30111222");

        User u2 = new User(
                "juan@test.com",
                "1234",
                "Juan",
                "Perez"
        );

        u2.setBirthDate(LocalDate.of(1985,3,22));
        u2.setDni("28999111");

        User u3 = new User(
                "maria@test.com",
                "1234",
                "Maria",
                "Gomez"
        );

        u3.setBirthDate(LocalDate.of(2000,1,15));
        u3.setDni("40123456");

        userRepository.saveAll(List.of(u1, u2, u3));

    // ********************** RESTRICTIONS **************************

        restrictionRepository.save(
                Restriction.builder()
                        .code("GLUTEN_FREE")
                        .name("Gluten Free")
                        .category(RestrictionCategory.INTOLERANCES)
                        .description("Avoid gluten containing foods")
                        .build()
        );

        restrictionRepository.save(
                Restriction.builder()
                        .code("LACTOSE_FREE")
                        .name("Lactose Free")
                        .category(RestrictionCategory.INTOLERANCES)
                        .description("Avoid lactose and dairy products")
                        .build()
        );

        restrictionRepository.save(
                Restriction.builder()
                        .code("LOW_SODIUM")
                        .name("Low Sodium")
                        .category(RestrictionCategory.PATHOLOGICAL)
                        .description("Reduce sodium intake")
                        .build()
        );

        restrictionRepository.save(
                Restriction.builder()
                        .code("VEGAN")
                        .name("Vegan")
                        .category(RestrictionCategory.DIETARY)
                        .description("Avoid all animal products")
                        .build()
        );

        restrictionRepository.save(
                Restriction.builder()
                        .code("VEGETARIAN")
                        .name("Vegetarian")
                        .category(RestrictionCategory.DIETARY)
                        .description("Avoid meat and fish")
                        .build()
        );

        // ********************** FOODS **************************
        foodRepository.saveAll(List.of(

                Food.builder()
                        .name("Chicken Breast")
                        .caloriesPer100g(165)
                        .proteinPer100g(31)
                        .carbsPer100g(0)
                        .fatPer100g(4)
                        .foodTags(Set.of(FoodTag.MEAT))
                        .build(),

                Food.builder()
                        .name("Salmon")
                        .caloriesPer100g(208)
                        .proteinPer100g(20)
                        .carbsPer100g(0)
                        .fatPer100g(13)
                        .foodTags(Set.of(FoodTag.FISH))
                        .build(),

                Food.builder()
                        .name("Scrambled Eggs")
                        .caloriesPer100g(155)
                        .proteinPer100g(13)
                        .carbsPer100g(1)
                        .fatPer100g(11)
                        .foodTags(Set.of(FoodTag.EGG))
                        .build(),

                Food.builder()
                        .name("Whole Milk Yogurt")
                        .caloriesPer100g(61)
                        .proteinPer100g(3)
                        .carbsPer100g(5)
                        .fatPer100g(3)
                        .foodTags(Set.of(FoodTag.LACTOSE))
                        .build(),

                Food.builder()
                        .name("Oatmeal")
                        .caloriesPer100g(389)
                        .proteinPer100g(17)
                        .carbsPer100g(66)
                        .fatPer100g(7)
                        .foodTags(Set.of(FoodTag.GLUTEN))
                        .build(),

                Food.builder()
                        .name("Tofu")
                        .caloriesPer100g(76)
                        .proteinPer100g(8)
                        .carbsPer100g(2)
                        .fatPer100g(5)
                        .foodTags(Set.of(FoodTag.SOY))
                        .build(),

                Food.builder()
                        .name("Mixed Nuts")
                        .caloriesPer100g(607)
                        .proteinPer100g(20)
                        .carbsPer100g(21)
                        .fatPer100g(54)
                        .foodTags(Set.of(FoodTag.NUTS))
                        .build(),

                Food.builder()
                        .name("Honey Granola")
                        .caloriesPer100g(471)
                        .proteinPer100g(10)
                        .carbsPer100g(64)
                        .fatPer100g(20)
                        .foodTags(Set.of(FoodTag.HONEY, FoodTag.GLUTEN))
                        .build(),

                Food.builder()
                        .name("Soy Milk")
                        .caloriesPer100g(54)
                        .proteinPer100g(3)
                        .carbsPer100g(6)
                        .fatPer100g(2)
                        .foodTags(Set.of(FoodTag.SOY))
                        .build(),

                Food.builder()
                        .name("Brown Rice")
                        .caloriesPer100g(216)
                        .proteinPer100g(5)
                        .carbsPer100g(45)
                        .fatPer100g(2)
                        .foodTags(Set.of())
                        .build()
        ));

    }
}