package com.foodflow.service;

import com.foodflow.dto.*;
import org.springframework.data.domain.Page;

public interface DonationWorkflowService {

    WorkflowResponse acceptAllocation(String email, String allocationId);

    WorkflowResponse confirmNgo(String email, String allocationId, String targetNgoId);

    WorkflowResponse schedulePickup(String email, String allocationId, String targetNgoId, SchedulePickupRequest request);

    WorkflowResponse startPickup(String email, String allocationId);

    WorkflowResponse completePickup(String email, String allocationId);

    WorkflowResponse completeDelivery(String email, String allocationId, DeliveryCompleteRequest request);

    WorkflowResponse cancelWorkflow(String email, String allocationId, String targetNgoId, CancelDonationRequest request);

    WorkflowResponse getWorkflowForNgo(String email, String allocationId);

    WorkflowResponse getWorkflowById(String email, String id);

    Page<WorkflowResponse> getWorkflowHistory(String email, int page, int size);

    DonationWorkflowAggregateResponse getDonationAggregate(String email, String donationId);
}
