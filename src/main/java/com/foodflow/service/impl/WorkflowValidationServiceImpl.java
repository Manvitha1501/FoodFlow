package com.foodflow.service.impl;

import com.foodflow.document.AllocationWorkflowDocument;
import com.foodflow.document.DonationAllocationDocument;
import com.foodflow.document.EmbeddedAllocation;
import com.foodflow.document.RestaurantProfileDocument;
import com.foodflow.enums.Role;
import com.foodflow.enums.WorkflowStatus;
import com.foodflow.exception.RestaurantNotFoundException;
import com.foodflow.exception.WorkflowValidationException;
import com.foodflow.repository.RestaurantProfileRepository;
import com.foodflow.service.WorkflowValidationService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class WorkflowValidationServiceImpl implements WorkflowValidationService {

    private final RestaurantProfileRepository restaurantProfileRepository;

    public WorkflowValidationServiceImpl(RestaurantProfileRepository restaurantProfileRepository) {
        this.restaurantProfileRepository = restaurantProfileRepository;
    }

    @Override
    public void validateNgoAllocationAssignment(DonationAllocationDocument allocation, String ngoId) {
        if (allocation == null || allocation.getAllocations() == null || allocation.getAllocations().isEmpty()) {
            throw new AccessDeniedException("No allocations present for this donation slice");
        }
        if (ngoId == null) {
            throw new AccessDeniedException("NGO identifier is required for assignment validation");
        }

        boolean isAssigned = allocation.getAllocations().stream()
                .map(EmbeddedAllocation::getNgoId)
                .anyMatch(ngoId::equals);

        if (!isAssigned) {
            throw new AccessDeniedException("NGO is not assigned to any portion of this allocation");
        }
    }

    @Override
    public void validateRestaurantOwnership(String donationRestaurantId, String callerUserId, Role callerRole) {
        if (callerRole == Role.ADMIN) {
            return; // Admin bypass
        }
        if (callerRole != Role.RESTAURANT) {
            throw new AccessDeniedException("Only restaurants or admins can manage donation workflows");
        }

        RestaurantProfileDocument restaurant = restaurantProfileRepository.findByUserIdAndIsDeletedFalse(callerUserId)
                .orElseThrow(() -> new RestaurantNotFoundException("Restaurant profile not found for user: " + callerUserId));

        if (!restaurant.getId().equals(donationRestaurantId)) {
            throw new AccessDeniedException("You do not own this donation allocation workflow");
        }
    }

    @Override
    public void validateFutureTime(Instant scheduledTime) {
        validateFuturePickupTime(scheduledTime);
    }

    @Override
    public void validateFuturePickupTime(Instant scheduledTime) {
        if (scheduledTime == null) {
            throw new WorkflowValidationException("Scheduled pickup time is required");
        }
        if (scheduledTime.isBefore(Instant.now())) {
            throw new WorkflowValidationException("Scheduled pickup time must be in the future");
        }
    }

    @Override
    public void validateWorkflowNotCompleted(AllocationWorkflowDocument workflow) {
        if (workflow != null && workflow.getCurrentStatus() == WorkflowStatus.COMPLETED) {
            throw new WorkflowValidationException("Completed workflows cannot be modified");
        }
    }

    @Override
    public void validateWorkflowNotCancelled(AllocationWorkflowDocument workflow) {
        if (workflow != null && workflow.getCurrentStatus() == WorkflowStatus.CANCELLED) {
            throw new WorkflowValidationException("Cancelled workflows cannot continue");
        }
    }

    @Override
    public void validateWorkflowEditable(AllocationWorkflowDocument workflow) {
        validateWorkflowNotCompleted(workflow);
        validateWorkflowNotCancelled(workflow);
    }
}
