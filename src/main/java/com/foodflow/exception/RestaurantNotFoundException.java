package com.foodflow.exception;

public class RestaurantNotFoundException extends ProfileNotFoundException {
    public RestaurantNotFoundException(String message) {
        super(message);
    }
}
