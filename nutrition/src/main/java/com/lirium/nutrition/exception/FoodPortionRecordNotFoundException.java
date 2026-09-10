package com.lirium.nutrition.exception;

public class FoodPortionRecordNotFoundException extends NotFoundException {
  public FoodPortionRecordNotFoundException(Long id) {
    super("FoodPortionRecord not found with id: " + id);
  }

  public FoodPortionRecordNotFoundException(String message) {
    super(message);
  }
}
