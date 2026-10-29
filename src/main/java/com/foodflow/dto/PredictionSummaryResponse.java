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
public class PredictionSummaryResponse {
    private String id;
    private String donationId;
    private String restaurantId;
    private Double readinessScore;
    private ReadinessLevel readinessLevel;
    private String recommendation;
    private PredictionStatus predictionStatus;
    private Instant createdAt;
}
