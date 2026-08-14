package com.lirium.nutrition.exception;

public class PlanConflictException extends RuntimeException {
    public PlanConflictException(String message) {
        super(message);
    }
}