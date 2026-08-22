package com.lirium.nutrition.exception;

public class RestrictionAlreadyExistsException extends RuntimeException {
    public RestrictionAlreadyExistsException(String message) {
        super(message);
    }
}
