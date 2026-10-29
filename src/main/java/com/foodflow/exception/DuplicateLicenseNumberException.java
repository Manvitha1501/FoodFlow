package com.foodflow.exception;

public class DuplicateLicenseNumberException extends RuntimeException {
    public DuplicateLicenseNumberException(String message) {
        super(message);
    }
}
