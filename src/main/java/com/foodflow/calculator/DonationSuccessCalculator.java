package com.foodflow.calculator;

import org.springframework.stereotype.Component;

@Component
public class DonationSuccessCalculator {

    public double calculateSuccessRate(int completed, int total) {
        if (total <= 0) {
            return 0.0;
        }
        return Math.max(0.0, Math.min(100.0, ((double) completed / total) * 100.0));
    }
}
