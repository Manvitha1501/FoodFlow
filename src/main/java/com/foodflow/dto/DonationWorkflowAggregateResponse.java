package com.foodflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonationWorkflowAggregateResponse {
    private String donationId;
    private Integer totalAllocatedNgos;
    private List<WorkflowResponse> workflows;
}
