package com.lirium.nutrition.exception;

public class RestrictionNotFoundException extends RuntimeException {

  public RestrictionNotFoundException(Long id) {
    super("Restriction not found with id: " + id);
  }
}
