package com.foodflow.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskPredictionRequest {

    @Builder.Default
    private String weatherCondition = "CLEAR";

    @Builder.Default
    private String trafficCondition = "NORMAL";

    @DecimalMin("0.0")
    private Double distanceInKm;

    @Min(0)
    private Integer estimatedTravelTimeMinutes;

    @DecimalMin("0.0")
    private Double availableStorage;

    @DecimalMin("0.0")
    private Double averagePickupDelayMinutes;

    @DecimalMin("0.0")
    @DecimalMax("100.0")
    private Double completionRate;
}
