package com.lirium.nutrition;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NutritionApplication {

  public static void main(String[] args) {
    SpringApplication.run(NutritionApplication.class, args);
  }
}
