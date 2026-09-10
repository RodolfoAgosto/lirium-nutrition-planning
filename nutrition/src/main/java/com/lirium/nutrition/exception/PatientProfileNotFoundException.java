package com.lirium.nutrition.exception;

public class PatientProfileNotFoundException extends NotFoundException {
  public PatientProfileNotFoundException(Long id) {
    super("PatientProfile not found with id: " + id);
  }

  public PatientProfileNotFoundException(String message) {
    super(message);
  }
}
