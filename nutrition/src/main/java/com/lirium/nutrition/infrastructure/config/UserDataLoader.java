package com.lirium.nutrition.infrastructure.config;

import com.lirium.nutrition.model.entity.User;
import com.lirium.nutrition.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Order(1)
public class UserDataLoader implements CommandLineRunner {

    private final UserRepository userRepository;

    @Override
    public void run(String... args) {

        if (userRepository.count() > 0) return;

        User u1 = new User("ana@test.com", "1234", "Ana", "Lopez");
        u1.setBirthDate(LocalDate.of(1990, 5, 10));
        u1.setDni("30111222");

        User u2 = new User("juan@test.com", "1234", "Juan", "Perez");
        u2.setBirthDate(LocalDate.of(1985, 3, 22));
        u2.setDni("28999111");

        User u3 = new User("maria@test.com", "1234", "Maria", "Gomez");
        u3.setBirthDate(LocalDate.of(2000, 1, 15));
        u3.setDni("40123456");

        userRepository.saveAll(List.of(u1, u2, u3));
    }
}
