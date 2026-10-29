package com.foodflow.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "foodflow.trust")
public class TrustWeightConfiguration {

    private RestaurantWeights restaurant = new RestaurantWeights();
    private NgoWeights ngo = new NgoWeights();
    private BayesianPriors bayesian = new BayesianPriors();

    @Data
    public static class RestaurantWeights {
        private double foodAccuracyWeight = 0.30;
        private double successfulDonationWeight = 0.25;
        private double cancellationWeight = 0.20;
        private double qualityWeight = 0.15;
        private double responseWeight = 0.10;
    }

    @Data
    public static class NgoWeights {
        private double pickupWeight = 0.30;
        private double completionWeight = 0.25;
        private double acceptanceWeight = 0.20;
        private double responseWeight = 0.15;
        private double ratingWeight = 0.10;
    }

    @Data
    public static class BayesianPriors {
        private double priorWeight = 5.0;
        private double globalAverage = 75.0;
        private int minimumObservations = 1;
    }
}
