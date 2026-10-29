package com.foodflow.exception;

public class PredictionAlreadyExistsException extends RuntimeException {
    public PredictionAlreadyExistsException(String message) {
        super(message);
    }
}
