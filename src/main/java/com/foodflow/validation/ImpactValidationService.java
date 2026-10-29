package com.foodflow.validation;

public interface ImpactValidationService {

    void validateRestaurantExists(String restaurantId);

    void validateNgoExists(String ngoId);
}
