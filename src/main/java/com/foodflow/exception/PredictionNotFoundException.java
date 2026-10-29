package com.foodflow.exception;

public class PredictionNotFoundException extends RuntimeException {
    public PredictionNotFoundException(String message) {
        super(message);
    }
}
