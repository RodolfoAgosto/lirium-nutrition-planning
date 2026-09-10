package com.lirium.nutrition.exception;

public class FoodNotFoundException extends NotFoundException {
  public FoodNotFoundException(Long id) {
    super("Food not found with id: " + id);
  }

  public FoodNotFoundException(String message) {
    super(message);
  }
}
