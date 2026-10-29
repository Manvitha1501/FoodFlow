package com.foodflow.service;

import com.foodflow.document.AllocationWorkflowDocument;
import com.foodflow.document.DonationAllocationDocument;
import com.foodflow.enums.Role;

import java.time.Instant;

public interface WorkflowValidationService {

    void validateNgoAllocationAssignment(DonationAllocationDocument allocation, String ngoId);

    void validateRestaurantOwnership(String donationRestaurantId, String callerUserId, Role callerRole);

    void validateFutureTime(Instant scheduledTime);

    void validateFuturePickupTime(Instant scheduledTime);

    void validateWorkflowNotCompleted(AllocationWorkflowDocument workflow);

    void validateWorkflowNotCancelled(AllocationWorkflowDocument workflow);

    void validateWorkflowEditable(AllocationWorkflowDocument workflow);
}
