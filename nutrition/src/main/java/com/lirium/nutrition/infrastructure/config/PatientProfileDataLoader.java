package com.lirium.nutrition.infrastructure.config;

import com.lirium.nutrition.model.enums.ActivityLevel;
import com.lirium.nutrition.model.enums.GoalType;
import com.lirium.nutrition.model.enums.Sex;
import com.lirium.nutrition.model.valueobject.Height;
import com.lirium.nutrition.model.valueobject.Weight;
import com.lirium.nutrition.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Order(2)
public class PatientProfileDataLoader implements CommandLineRunner {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public void run(String... args) {

        userRepository.findByEmail("ana@test.com").ifPresent(user -> {
            if (user.getPatientProfile().getWeight() != null) return;
            user.getPatientProfile().update(
                    Sex.FEMALE,
                    ActivityLevel.ACTIVE,
                    Weight.of(62_000),
                    Height.of(163),
                    null,
                    Collections.emptySet(),
                    Collections.emptyList(),
                    GoalType.WEIGHT_MAINTENANCE
            );
            userRepository.save(user);
        });

        userRepository.findByEmail("juan@test.com").ifPresent(user -> {
            if (user.getPatientProfile().getWeight() != null) return;
            user.getPatientProfile().update(
                    Sex.MALE,
                    ActivityLevel.MODERATE,
                    Weight.of(85_000),
                    Height.of(175),
                    null,
                    Collections.emptySet(),
                    Collections.emptyList(),
                    GoalType.WEIGHT_LOSS
            );
            userRepository.save(user);
        });

        userRepository.findByEmail("maria@test.com").ifPresent(user -> {
            if (user.getPatientProfile().getWeight() != null) return;
            user.getPatientProfile().update(
                    Sex.FEMALE,
                    ActivityLevel.VERY_ACTIVE,
                    Weight.of(58_000),
                    Height.of(160),
                    null,
                    Collections.emptySet(),
                    Collections.emptyList(),
                    GoalType.MUSCLE_GAIN
            );
            userRepository.save(user);
        });
    }
}