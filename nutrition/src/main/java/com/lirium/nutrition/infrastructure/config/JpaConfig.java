package com.lirium.nutrition.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class JpaConfig {

    @Bean
    public org.springframework.data.domain.AuditorAware<String> auditorProvider() {
        return () -> java.util.Optional.of("system");
    }

}