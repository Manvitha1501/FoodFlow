package com.foodflow.controller;

import com.foodflow.dto.*;
import com.foodflow.service.DonationWorkflowService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/workflow")
@Validated
public class DonationWorkflowController {

    private final DonationWorkflowService workflowService;

    public DonationWorkflowController(DonationWorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @PostMapping("/{allocationId}/accept")
    @PreAuthorize("hasAnyRole('NGO', 'ADMIN')")
    public ResponseEntity<WorkflowResponse> acceptAllocation(
            @PathVariable String allocationId,
            Principal principal
    ) {
        return ResponseEntity.ok(workflowService.acceptAllocation(principal.getName(), allocationId));
    }

    @PostMapping("/{allocationId}/confirm")
    @PreAuthorize("hasAnyRole('RESTAURANT', 'ADMIN')")
    public ResponseEntity<WorkflowResponse> confirmNgo(
            @PathVariable String allocationId,
            @RequestParam(required = false) String targetNgoId,
            Principal principal
    ) {
        return ResponseEntity.ok(workflowService.confirmNgo(principal.getName(), allocationId, targetNgoId));
    }

    @PostMapping("/{allocationId}/schedule-pickup")
    @PreAuthorize("hasAnyRole('RESTAURANT', 'ADMIN')")
    public ResponseEntity<WorkflowResponse> schedulePickup(
            @PathVariable String allocationId,
            @RequestParam(required = false) String targetNgoId,
            @Valid @RequestBody SchedulePickupRequest request,
            Principal principal
    ) {
        return ResponseEntity.ok(workflowService.schedulePickup(principal.getName(), allocationId, targetNgoId, request));
    }

    @PostMapping("/{allocationId}/pickup-start")
    @PreAuthorize("hasAnyRole('NGO', 'ADMIN')")
    public ResponseEntity<WorkflowResponse> startPickup(
            @PathVariable String allocationId,
            Principal principal
    ) {
        return ResponseEntity.ok(workflowService.startPickup(principal.getName(), allocationId));
    }

    @PostMapping("/{allocationId}/pickup-complete")
    @PreAuthorize("hasAnyRole('NGO', 'ADMIN')")
    public ResponseEntity<WorkflowResponse> completePickup(
            @PathVariable String allocationId,
            Principal principal
    ) {
        return ResponseEntity.ok(workflowService.completePickup(principal.getName(), allocationId));
    }

    @PostMapping("/{allocationId}/delivery-complete")
    @PreAuthorize("hasAnyRole('NGO', 'ADMIN')")
    public ResponseEntity<WorkflowResponse> completeDelivery(
            @PathVariable String allocationId,
            @RequestBody(required = false) DeliveryCompleteRequest request,
            Principal principal
    ) {
        return ResponseEntity.ok(workflowService.completeDelivery(principal.getName(), allocationId, request));
    }

    @PostMapping("/{allocationId}/cancel")
    @PreAuthorize("hasAnyRole('RESTAURANT', 'NGO', 'ADMIN')")
    public ResponseEntity<WorkflowResponse> cancelWorkflow(
            @PathVariable String allocationId,
            @RequestParam(required = false) String targetNgoId,
            @Valid @RequestBody CancelDonationRequest request,
            Principal principal
    ) {
        return ResponseEntity.ok(workflowService.cancelWorkflow(principal.getName(), allocationId, targetNgoId, request));
    }

    @GetMapping("/{allocationId}")
    @PreAuthorize("hasAnyRole('RESTAURANT', 'NGO', 'ADMIN')")
    public ResponseEntity<WorkflowResponse> getWorkflow(
            @PathVariable String allocationId,
            Principal principal
    ) {
        return ResponseEntity.ok(workflowService.getWorkflowForNgo(principal.getName(), allocationId));
    }

    @GetMapping("/{allocationId}/history")
    @PreAuthorize("hasAnyRole('RESTAURANT', 'NGO', 'ADMIN')")
    public ResponseEntity<List<WorkflowHistoryResponse>> getWorkflowHistory(
            @PathVariable String allocationId,
            Principal principal
    ) {
        WorkflowResponse response = workflowService.getWorkflowForNgo(principal.getName(), allocationId);
        return ResponseEntity.ok(response.getStatusHistory());
    }

    @GetMapping("/donation/{donationId}")
    @PreAuthorize("hasAnyRole('RESTAURANT', 'ADMIN')")
    public ResponseEntity<DonationWorkflowAggregateResponse> getDonationAggregate(
            @PathVariable String donationId,
            Principal principal
    ) {
        return ResponseEntity.ok(workflowService.getDonationAggregate(principal.getName(), donationId));
    }

    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('RESTAURANT', 'NGO', 'ADMIN')")
    public ResponseEntity<Page<WorkflowResponse>> getPaginatedWorkflowHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal principal
    ) {
        return ResponseEntity.ok(workflowService.getWorkflowHistory(principal.getName(), page, size));
    }
}
