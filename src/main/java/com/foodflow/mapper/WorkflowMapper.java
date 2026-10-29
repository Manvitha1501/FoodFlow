package com.foodflow.mapper;

import com.foodflow.document.AllocationWorkflowDocument;
import com.foodflow.document.StatusHistory;
import com.foodflow.dto.DonationWorkflowAggregateResponse;
import com.foodflow.dto.WorkflowHistoryResponse;
import com.foodflow.dto.WorkflowResponse;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class WorkflowMapper {

    public WorkflowHistoryResponse toHistoryResponse(StatusHistory history) {
        if (history == null) {
            return null;
        }
        return WorkflowHistoryResponse.builder()
                .previousStatus(history.getPreviousStatus())
                .newStatus(history.getNewStatus())
                .changedBy(history.getChangedBy())
                .changedAt(history.getChangedAt())
                .remarks(history.getRemarks())
                .build();
    }

    public WorkflowResponse toResponse(AllocationWorkflowDocument doc) {
        if (doc == null) {
            return null;
        }
        List<WorkflowHistoryResponse> historyResponses = doc.getStatusHistory() != null
                ? doc.getStatusHistory().stream().map(this::toHistoryResponse).collect(Collectors.toList())
                : Collections.emptyList();

        return WorkflowResponse.builder()
                .id(doc.getId())
                .allocationId(doc.getAllocationId())
                .donationId(doc.getDonationId())
                .restaurantId(doc.getRestaurantId())
                .ngoId(doc.getNgoId())
                .currentStatus(doc.getCurrentStatus())
                .acceptedAt(doc.getAcceptedAt())
                .confirmedAt(doc.getConfirmedAt())
                .pickupScheduledTime(doc.getPickupScheduledTime())
                .pickupStartedAt(doc.getPickupStartedAt())
                .pickupCompletedAt(doc.getPickupCompletedAt())
                .deliveryCompletedAt(doc.getDeliveryCompletedAt())
                .completedAt(doc.getCompletedAt())
                .cancelledAt(doc.getCancelledAt())
                .completionRemarks(doc.getCompletionRemarks())
                .createdAt(doc.getCreatedAt())
                .updatedAt(doc.getUpdatedAt())
                .statusHistory(historyResponses)
                .build();
    }

    public DonationWorkflowAggregateResponse toAggregateResponse(String donationId, List<AllocationWorkflowDocument> docs) {
        List<WorkflowResponse> workflowResponses = docs != null
                ? docs.stream().map(this::toResponse).collect(Collectors.toList())
                : Collections.emptyList();

        return DonationWorkflowAggregateResponse.builder()
                .donationId(donationId)
                .totalAllocatedNgos(workflowResponses.size())
                .workflows(workflowResponses)
                .build();
    }
}
