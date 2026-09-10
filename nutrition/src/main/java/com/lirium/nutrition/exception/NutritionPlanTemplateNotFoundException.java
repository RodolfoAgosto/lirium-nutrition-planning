package com.lirium.nutrition.exception;

public class NutritionPlanTemplateNotFoundException extends NotFoundException {

  public NutritionPlanTemplateNotFoundException(Long id) {
    super("NutritionPlanTemplate not found with id: " + id);
  }

  public NutritionPlanTemplateNotFoundException(String message) {
    super(message);
  }
}
