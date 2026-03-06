package com.lirium.nutrition.exception;

public class EmailNotValidatedException extends RuntimeException {

    public EmailNotValidatedException(String email) {
        super("Email not validated: " + email);
    }

}