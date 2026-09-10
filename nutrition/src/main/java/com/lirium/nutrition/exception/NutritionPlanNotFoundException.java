package com.lirium.nutrition.exception;

public class NutritionPlanNotFoundException extends NotFoundException {
  public NutritionPlanNotFoundException(Long id) {
    super("NutritionPlan not found with id: " + id);
  }

  public NutritionPlanNotFoundException(String message) {
    super(message);
  }
}
