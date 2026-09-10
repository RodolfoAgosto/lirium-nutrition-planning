package com.lirium.nutrition.exception;

public class DailyPlanNotFoundException extends NotFoundException {
  public DailyPlanNotFoundException(Long id) {
    super("DailyPlan not found with id: " + id);
  }

  public DailyPlanNotFoundException(String message) {
    super(message);
  }
}
