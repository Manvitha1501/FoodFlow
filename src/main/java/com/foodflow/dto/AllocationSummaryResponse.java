package com.foodflow.dto;

import com.foodflow.enums.AllocationAlgorithm;
import com.foodflow.enums.AllocationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AllocationSummaryResponse {
    private String id;
    private String donationId;
    private Double totalQuantity;
    private String quantityUnit;
    private Double remainingQuantity;
    private AllocationStatus allocationStatus;
    private AllocationAlgorithm allocationAlgorithm;
    private Instant generatedAt;
    private Integer allocatedNgosCount;
}
