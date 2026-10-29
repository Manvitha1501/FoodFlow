package com.foodflow.calculator;

import com.foodflow.document.ScoreBreakdown;
import com.foodflow.enums.EntityType;
import com.foodflow.enums.TrustLevel;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TrustExplanationGenerator {

    public List<String> generateExplanationBullets(
            EntityType entityType,
            ScoreBreakdown breakdown,
            double weightedScore,
            double bayesianScore,
            double finalScore,
            TrustLevel level
    ) {
        List<String> bullets = new ArrayList<>();

        bullets.add(String.format("Final Trust Score of %.1f categorized as %s", finalScore, level));
        bullets.add(String.format("Raw Weighted Score: %.1f, adjusted to %.1f using Bayesian prior smoothing", weightedScore, bayesianScore));

        if (breakdown == null) {
            return bullets;
        }

        if (entityType == EntityType.RESTAURANT) {
            if (breakdown.getFoodAccuracy() != null && breakdown.getFoodAccuracy() >= 80.0) {
                bullets.add(String.format("High food accuracy rating (%.1f%%)", breakdown.getFoodAccuracy()));
            }
            if (breakdown.getSuccessfulDonations() != null && breakdown.getSuccessfulDonations() >= 80.0) {
                bullets.add(String.format("Consistent donation fulfillment history (%.1f%%)", breakdown.getSuccessfulDonations()));
            }
            if (breakdown.getCancellationRate() != null && breakdown.getCancellationRate() >= 80.0) {
                bullets.add(String.format("Low cancellation history (%.1f%% score)", breakdown.getCancellationRate()));
            }
            if (breakdown.getQualityHistory() != null && breakdown.getQualityHistory() >= 80.0) {
                bullets.add(String.format("Strong food quality compliance (%.1f%%)", breakdown.getQualityHistory()));
            }
            if (breakdown.getResponseTime() != null && breakdown.getResponseTime() >= 80.0) {
                bullets.add(String.format("Fast response & confirmation times (%.1f%%)", breakdown.getResponseTime()));
            }
        } else if (entityType == EntityType.NGO) {
            if (breakdown.getPickupPunctuality() != null && breakdown.getPickupPunctuality() >= 80.0) {
                bullets.add(String.format("Excellent pickup punctuality (%.1f%%)", breakdown.getPickupPunctuality()));
            }
            if (breakdown.getCompletionRate() != null && breakdown.getCompletionRate() >= 80.0) {
                bullets.add(String.format("High workflow completion rate (%.1f%%)", breakdown.getCompletionRate()));
            }
            if (breakdown.getAcceptanceRate() != null && breakdown.getAcceptanceRate() >= 80.0) {
                bullets.add(String.format("Strong allocation acceptance rate (%.1f%%)", breakdown.getAcceptanceRate()));
            }
            if (breakdown.getResponseSpeed() != null && breakdown.getResponseSpeed() >= 80.0) {
                bullets.add(String.format("Rapid response & pickup start times (%.1f%%)", breakdown.getResponseSpeed()));
            }
            if (breakdown.getRestaurantRating() != null && breakdown.getRestaurantRating() >= 80.0) {
                bullets.add(String.format("High restaurant feedback ratings (%.1f%%)", breakdown.getRestaurantRating()));
            }
        }

        return bullets;
    }
}
