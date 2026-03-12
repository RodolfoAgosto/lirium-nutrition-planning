package com.lirium.nutrition.exception;

public class DuplicateFoodException extends RuntimeException {
    public DuplicateFoodException(String message) {
        super(message);
    }
}