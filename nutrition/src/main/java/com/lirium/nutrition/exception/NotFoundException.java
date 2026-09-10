package com.lirium.nutrition.exception;

public abstract class NotFoundException extends RuntimeException {
  protected NotFoundException(String message) {
    super(message);
  }
}
