package com.lirium.nutrition.exception;

public class RestrictionNotFoundException extends NotFoundException {
  public RestrictionNotFoundException(Long id) {
    super("Restriction not found with id: " + id);
  }

  public RestrictionNotFoundException(String message) {
    super(message);
  }
}
