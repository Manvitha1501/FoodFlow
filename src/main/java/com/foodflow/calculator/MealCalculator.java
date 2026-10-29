package com.foodflow.calculator;

import com.foodflow.config.ImpactConfiguration;
import org.springframework.stereotype.Component;

@Component
public class MealCalculator {

    private final ImpactConfiguration config;

    public MealCalculator(ImpactConfiguration config) {
        this.config = config;
    }

    public double calculateMeals(double foodSavedKg) {
        double mealWeight = config.getAverageMealWeight();
        if (mealWeight <= 0) {
            mealWeight = 0.45;
        }
        return Math.max(0.0, foodSavedKg / mealWeight);
    }
}
