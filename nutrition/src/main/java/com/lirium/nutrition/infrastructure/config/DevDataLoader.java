package com.lirium.nutrition.infrastructure.config;

        import com.lirium.nutrition.model.entity.Restriction;
        import com.lirium.nutrition.model.entity.User;
        import com.lirium.nutrition.model.enums.RestrictionCategory;
        import com.lirium.nutrition.repository.RestrictionRepository;
        import com.lirium.nutrition.repository.UserRepository;
        import lombok.RequiredArgsConstructor;
        import org.springframework.boot.CommandLineRunner;
        import org.springframework.context.annotation.Profile;
        import org.springframework.stereotype.Component;

        import java.time.LocalDate;
        import java.util.List;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevDataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RestrictionRepository restrictionRepository;

    @Override
    public void run(String... args) {

        if(userRepository.count() > 0){
            return;
        }

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

        // Restricctions

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

    }
}