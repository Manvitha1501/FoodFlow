package com.foodflow.exception;

public class DuplicateRegistrationNumberException extends RuntimeException {
    public DuplicateRegistrationNumberException(String message) {
        super(message);
    }
}
