package com.lirium.nutrition.exception;

public class DailyRecordNotFoundException extends NotFoundException {
  public DailyRecordNotFoundException(Long id) {
    super("DailyRecord not found with id: " + id);
  }

  public DailyRecordNotFoundException(String message) {
    super(message);
  }
}
