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
public class RiskPredictionSummary {

    private String id;
    private String donationId;
    private String restaurantId;
    private String ngoId;
    private Double successProbability;
    private RiskLevel riskLevel;
    private PredictionStatus predictionStatus;
    private Instant createdAt;
}
