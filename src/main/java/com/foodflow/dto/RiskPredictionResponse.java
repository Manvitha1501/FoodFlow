package com.foodflow.dto;

import com.foodflow.enums.PredictionStatus;
import com.foodflow.enums.RiskLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskPredictionResponse {

    private String id;
    private String donationId;
    private String restaurantId;
    private String ngoId;
    private Instant predictionRequestedAt;
    private Double successProbability;
    private Double failureProbability;
    private RiskLevel riskLevel;
    private Double confidenceScore;
    private String predictionExplanation;
    private String estimatedPickupTime;
    private String estimatedDeliveryTime;
    private String modelVersion;
    private PredictionStatus predictionStatus;
    private Instant createdAt;
    private Instant updatedAt;
}
