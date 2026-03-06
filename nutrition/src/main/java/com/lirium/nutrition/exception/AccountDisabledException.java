package com.lirium.nutrition.exception;

public class AccountDisabledException extends RuntimeException {

    public AccountDisabledException(Long id) {
        super("Account disabled for id: " + id);
    }

}