package com.foodflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImpactSummaryResponse {
    private Double totalFoodSavedKg;
    private Double totalMealsDelivered;
    private Double totalCO2ReductionKg;
    private Double equivalentTreesSaved;
    private Double equivalentWaterSavedLiters;
    private Double donationSuccessRate;
    private Double pickupSuccessRate;
}
