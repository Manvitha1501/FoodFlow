package com.foodflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImpactTrendResponse {
    private String periodType;
    private String periodLabel;
    private Double foodSavedKg;
    private Double mealsDelivered;
    private Double co2ReductionKg;
    private Integer successfulDonations;
    private Integer cancelledDonations;
}
