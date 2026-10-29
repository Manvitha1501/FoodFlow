package com.foodflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResponse {
    private Double totalFoodSavedKg;
    private Double totalMealsDelivered;
    private Double totalCO2ReductionKg;
    private Double donationSuccessRate;
    private Double pickupSuccessRate;
    private Integer totalRestaurants;
    private Integer totalNgos;
    private Integer totalCompletedDonations;
    private Integer totalCancelledDonations;
}
