package com.foodflow.calculator;

import org.springframework.stereotype.Component;

@Component
public class FoodSavedCalculator {

    public double calculateFoodSavedKg(Double quantity) {
        if (quantity == null || quantity < 0) {
            return 0.0;
        }
        return quantity;
    }
}
