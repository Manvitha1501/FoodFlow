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
public class NGOImpactResponse {
    private String ngoId;
    private String ngoName;
    private Double totalMealsReceived;
    private Double totalFoodReceivedKg;
    private Integer totalCompletedPickups;
    private Double averagePickupTime;
    private Double completionRate;
    private Double currentTrustScore;
    private Instant lastUpdated;
}
