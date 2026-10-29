package com.foodflow.exception;

public class TrustNotFoundException extends RuntimeException {
    public TrustNotFoundException(String message) {
        super(message);
    }
}
