package com.foodflow.calculator;

import com.foodflow.config.TrustWeightConfiguration;
import org.springframework.stereotype.Component;

@Component
public class BayesianScoreCalculator {

    private final TrustWeightConfiguration weightConfig;

    public BayesianScoreCalculator(TrustWeightConfiguration weightConfig) {
        this.weightConfig = weightConfig;
    }

    public double calculateBayesianScore(double weightedScore, int completedAllocations) {
        double priorWeight = weightConfig.getBayesian().getPriorWeight();
        double globalAvg = weightConfig.getBayesian().getGlobalAverage();

        if (completedAllocations <= 0) {
            return globalAvg;
        }

        double bayesian = ((priorWeight * globalAvg) + (completedAllocations * weightedScore)) / (priorWeight + completedAllocations);
        return Math.max(0.0, Math.min(100.0, bayesian));
    }
}
