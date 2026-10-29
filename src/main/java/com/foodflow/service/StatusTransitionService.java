package com.foodflow.service;

import com.foodflow.document.AllocationWorkflowDocument;
import com.foodflow.enums.WorkflowStatus;

import java.time.Instant;

public interface StatusTransitionService {

    AllocationWorkflowDocument transition(
            AllocationWorkflowDocument workflow,
            WorkflowStatus targetStatus,
            String changedBy,
            String remarks
    );

    AllocationWorkflowDocument transitionSchedulePickup(
            AllocationWorkflowDocument workflow,
            Instant pickupScheduledTime,
            String changedBy
    );
}
