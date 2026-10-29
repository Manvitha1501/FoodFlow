package com.foodflow.exception;

public class TrustHistoryNotFoundException extends RuntimeException {
    public TrustHistoryNotFoundException(String message) {
        super(message);
    }
}
