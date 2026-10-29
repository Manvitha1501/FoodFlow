package com.foodflow.dto;

import com.foodflow.enums.WorkflowStatus;
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
public class WorkflowResponse {
    private String id;
    private String allocationId;
    private String donationId;
    private String restaurantId;
    private String ngoId;
    private WorkflowStatus currentStatus;
    private Instant acceptedAt;
    private Instant confirmedAt;
    private Instant pickupScheduledTime;
    private Instant pickupStartedAt;
    private Instant pickupCompletedAt;
    private Instant deliveryCompletedAt;
    private Instant completedAt;
    private Instant cancelledAt;
    private String completionRemarks;
    private Instant createdAt;
    private Instant updatedAt;
    private List<WorkflowHistoryResponse> statusHistory;
}
