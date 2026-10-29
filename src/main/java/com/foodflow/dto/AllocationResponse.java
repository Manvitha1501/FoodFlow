package com.foodflow.dto;

import com.foodflow.enums.AllocationAlgorithm;
import com.foodflow.enums.AllocationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AllocationResponse {
    private String id;
    private String donationId;
    private String restaurantId;
    private Double totalQuantity;
    private String quantityUnit;
    private Double remainingQuantity;
    private AllocationStatus allocationStatus;
    private AllocationAlgorithm allocationAlgorithm;
    private Instant generatedAt;
    private List<NGOAllocationResponse> allocations;
    private Instant createdAt;
    private Instant updatedAt;
}
