package com.foodflow.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScoreBreakdown {
    private Double foodAccuracy;
    private Double successfulDonations;
    private Double cancellationRate;
    private Double qualityHistory;
    private Double responseTime;

    private Double pickupPunctuality;
    private Double completionRate;
    private Double acceptanceRate;
    private Double responseSpeed;
    private Double restaurantRating;
}
