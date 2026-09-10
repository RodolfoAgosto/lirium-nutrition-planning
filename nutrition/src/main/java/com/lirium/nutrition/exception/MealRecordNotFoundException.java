package com.lirium.nutrition.exception;

public class MealRecordNotFoundException extends NotFoundException {
  public MealRecordNotFoundException(Long id) {
    super("MealRecord not found with id: " + id);
  }

  public MealRecordNotFoundException(String message) {
    super(message);
  }
}
