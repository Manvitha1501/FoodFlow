package com.foodflow.dto;

import com.foodflow.enums.AllocationAlgorithm;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AllocationRequest {
    private AllocationAlgorithm algorithm;
}
