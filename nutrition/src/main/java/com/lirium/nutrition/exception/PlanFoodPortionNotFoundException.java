package com.lirium.nutrition.exception;

public class PlanFoodPortionNotFoundException extends NotFoundException {
  public PlanFoodPortionNotFoundException(Long id) {
    super("PlanFoodPortion not found with id: " + id);
  }

  public PlanFoodPortionNotFoundException(String message) {
    super(message);
  }
}
