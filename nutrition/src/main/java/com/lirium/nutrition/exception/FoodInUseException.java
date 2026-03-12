package com.lirium.nutrition.exception;

public class FoodInUseException extends RuntimeException {
    public FoodInUseException(String message, Long id) {
        super(message + " - id: " + id);
    }
}
