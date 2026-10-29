package com.foodflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantImpactResponse {
    private String restaurantId;
    private String restaurantName;
    private Double totalFoodSavedKg;
    private Double totalMealsDelivered;
    private Double totalCO2ReductionKg;
    private Integer totalCompletedDonations;
    private Integer totalCancelledDonations;
    private Double averageDonationSize;
    private Double successRate;
    private Double currentTrustScore;
    private Instant lastUpdated;
}
