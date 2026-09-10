package com.lirium.nutrition.exception;

public class PlanMealNotFoundException extends NotFoundException {
  public PlanMealNotFoundException(Long id) {
    super("PlanMeal not found with id: " + id);
  }

  public PlanMealNotFoundException(String message) {
    super(message);
  }
}
