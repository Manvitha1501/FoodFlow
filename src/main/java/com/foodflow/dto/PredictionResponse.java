package com.foodflow.dto;

import com.foodflow.enums.PredictionStatus;
import com.foodflow.enums.ReadinessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PredictionResponse {
    private String id;
    private String donationId;
    private String restaurantId;
    private String foodType;
    private Instant preparedTime;
    private Instant predictionRequestedAt;
    private Double readinessScore;
    private ReadinessLevel readinessLevel;
    private String recommendation;
    private Double confidence;
    private Integer remainingFreshnessHours;
    private String predictionReason;
    private String modelVersion;
    private PredictionStatus predictionStatus;
    private Instant createdAt;
}
