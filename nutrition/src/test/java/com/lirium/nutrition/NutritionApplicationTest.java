package com.lirium.nutrition;

import com.lirium.nutrition.config.PostgresTestContainer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@ImportTestcontainers(PostgresTestContainer.class)
class NutritionApplicationTest {

  @Test
  void contextLoads() {}
}
