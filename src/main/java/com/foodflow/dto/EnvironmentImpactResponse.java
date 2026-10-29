package com.foodflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnvironmentImpactResponse {
    private Double totalFoodSavedKg;
    private Double totalMealsGenerated;
    private Double totalCO2ReductionKg;
    private Double equivalentTreesSaved;
    private Double equivalentWaterSavedLiters;
}
