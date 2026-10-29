package com.foodflow.exception;

public class InvalidDonationStateException extends RuntimeException {
    public InvalidDonationStateException(String message) {
        super(message);
    }
}
