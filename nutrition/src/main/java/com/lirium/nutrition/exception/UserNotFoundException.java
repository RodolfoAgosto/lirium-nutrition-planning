package com.lirium.nutrition.exception;

public class UserNotFoundException extends NotFoundException {
  public UserNotFoundException(Long id) {
    super("User not found with id: " + id);
  }

  public UserNotFoundException(String message) {
    super(message);
  }
}
