package com.foodflow.exception;

public class DonationValidationException extends RuntimeException {
    public DonationValidationException(String message) {
        super(message);
    }
}
