package com.lirium.nutrition.exception;

public class DNIAlreadyExistsException extends RuntimeException {

    public DNIAlreadyExistsException(String email) {
        super("DNI already registered: " + email);
    }

}
