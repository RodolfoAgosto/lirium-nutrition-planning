package com.lirium.nutrition.exception;

public abstract class NotFoundException extends RuntimeException {
  protected NotFoundException(String entity, Object id) {
    super(entity + " not found with id: " + id);
  }
}
