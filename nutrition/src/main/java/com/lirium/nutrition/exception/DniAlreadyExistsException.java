package com.lirium.nutrition.exception;

public class DniAlreadyExistsException extends RuntimeException {

  public DniAlreadyExistsException(String email) {
    super("DNI already registered: " + email);
  }
}
