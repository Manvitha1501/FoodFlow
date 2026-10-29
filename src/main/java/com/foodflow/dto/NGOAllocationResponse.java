package com.foodflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NGOAllocationResponse {
    private String ngoId;
    private String ngoName;
    private Double matchingScore;
    private Integer allocationRank;
    private Double allocatedQuantity;
    private String allocatedUnit;
    private Double ngoCapacity;
    private Double remainingCapacity;
    private String estimatedPickupTime;
    private String allocationReason;
}
