package com.foodflow.calculator;

import org.springframework.stereotype.Component;

@Component
public class WeightedScoreCalculator {

    public double clamp(double score) {
        return Math.max(0.0, Math.min(100.0, score));
    }
}
