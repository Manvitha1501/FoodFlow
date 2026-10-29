package com.foodflow.service.impl;

import com.foodflow.document.AllocationWorkflowDocument;
import com.foodflow.document.StatusHistory;
import com.foodflow.enums.WorkflowStatus;
import com.foodflow.event.WorkflowCompletedEvent;
import com.foodflow.exception.InvalidStatusTransitionException;
import com.foodflow.service.StatusTransitionService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

@Service
public class StatusTransitionServiceImpl implements StatusTransitionService {

    private final ApplicationEventPublisher eventPublisher;

    public StatusTransitionServiceImpl(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    private static final Map<WorkflowStatus, Set<WorkflowStatus>> ALLOWED_TRANSITIONS = Map.of(
            WorkflowStatus.PUBLISHED, Set.of(WorkflowStatus.NGO_ACCEPTED, WorkflowStatus.CANCELLED),
            WorkflowStatus.NGO_ACCEPTED, Set.of(WorkflowStatus.RESTAURANT_CONFIRMED, WorkflowStatus.CANCELLED),
            WorkflowStatus.RESTAURANT_CONFIRMED, Set.of(WorkflowStatus.PICKUP_SCHEDULED, WorkflowStatus.CANCELLED),
            WorkflowStatus.PICKUP_SCHEDULED, Set.of(WorkflowStatus.PICKUP_STARTED, WorkflowStatus.CANCELLED),
            WorkflowStatus.PICKUP_STARTED, Set.of(WorkflowStatus.PICKED_UP, WorkflowStatus.CANCELLED),
            WorkflowStatus.PICKED_UP, Set.of(WorkflowStatus.DELIVERED, WorkflowStatus.CANCELLED),
            WorkflowStatus.DELIVERED, Set.of(WorkflowStatus.COMPLETED, WorkflowStatus.CANCELLED),
            WorkflowStatus.COMPLETED, Set.of(),
            WorkflowStatus.CANCELLED, Set.of()
    );

    @Override
    public AllocationWorkflowDocument transition(
            AllocationWorkflowDocument workflow,
            WorkflowStatus targetStatus,
            String changedBy,
            String remarks
    ) {
        WorkflowStatus currentStatus = workflow.getCurrentStatus();

        // 1. Idempotency Check: Same actor repeating same transition
        if (currentStatus == targetStatus) {
            return workflow;
        }

        // 2. Terminal State Guard
        if (currentStatus == WorkflowStatus.COMPLETED || currentStatus == WorkflowStatus.CANCELLED) {
            throw new InvalidStatusTransitionException(
                    "Cannot transition workflow from terminal state " + currentStatus + " to " + targetStatus
            );
        }

        // 3. Valid State Machine Transition Check
        Set<WorkflowStatus> allowedNext = ALLOWED_TRANSITIONS.getOrDefault(currentStatus, Set.of());
        if (!allowedNext.contains(targetStatus)) {
            throw new InvalidStatusTransitionException(
                    "Invalid status transition from " + currentStatus + " to " + targetStatus
            );
        }

        // 4. Record Status History Entry
        StatusHistory history = StatusHistory.builder()
                .previousStatus(currentStatus)
                .newStatus(targetStatus)
                .changedBy(changedBy)
                .changedAt(Instant.now())
                .remarks(remarks)
                .build();

        if (workflow.getStatusHistory() == null) {
            workflow.setStatusHistory(new ArrayList<>());
        }
        workflow.getStatusHistory().add(history);

        // 5. Update Current Status & Relevant Timestamp Fields
        workflow.setCurrentStatus(targetStatus);
        Instant now = Instant.now();

        switch (targetStatus) {
            case NGO_ACCEPTED:
                workflow.setAcceptedAt(now);
                break;
            case RESTAURANT_CONFIRMED:
                workflow.setConfirmedAt(now);
                break;
            case PICKUP_STARTED:
                workflow.setPickupStartedAt(now);
                break;
            case PICKED_UP:
                workflow.setPickupCompletedAt(now);
                break;
            case DELIVERED:
                workflow.setDeliveryCompletedAt(now);
                break;
            case COMPLETED:
                workflow.setCompletedAt(now);
                eventPublisher.publishEvent(new WorkflowCompletedEvent(
                        this,
                        workflow.getAllocationId(),
                        workflow.getDonationId(),
                        workflow.getRestaurantId(),
                        workflow.getNgoId()
                ));
                break;
            case CANCELLED:
                workflow.setCancelledAt(now);
                if (remarks != null && !remarks.isBlank()) {
                    workflow.setCompletionRemarks(remarks);
                }
                break;
            default:
                break;
        }

        return workflow;
    }

    @Override
    public AllocationWorkflowDocument transitionSchedulePickup(
            AllocationWorkflowDocument workflow,
            Instant pickupScheduledTime,
            String changedBy
    ) {
        WorkflowStatus currentStatus = workflow.getCurrentStatus();

        if (currentStatus == WorkflowStatus.PICKUP_SCHEDULED) {
            // Idempotent re-scheduling update
            workflow.setPickupScheduledTime(pickupScheduledTime);
            return workflow;
        }

        String remarks = "Pickup scheduled for " + pickupScheduledTime;
        AllocationWorkflowDocument updated = transition(workflow, WorkflowStatus.PICKUP_SCHEDULED, changedBy, remarks);
        updated.setPickupScheduledTime(pickupScheduledTime);
        return updated;
    }
}
