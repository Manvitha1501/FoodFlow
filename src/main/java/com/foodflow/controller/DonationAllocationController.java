package com.foodflow.controller;

import com.foodflow.dto.AllocationRequest;
import com.foodflow.dto.AllocationResponse;
import com.foodflow.dto.AllocationSummaryResponse;
import com.foodflow.dto.NGOAllocationResponse;
import com.foodflow.service.DonationAllocationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/allocation")
public class DonationAllocationController {

    private final DonationAllocationService allocationService;

    public DonationAllocationController(DonationAllocationService allocationService) {
        this.allocationService = allocationService;
    }

    @PostMapping("/{donationId}")
    public ResponseEntity<AllocationResponse> generateAllocation(
            Principal principal,
            @PathVariable String donationId,
            @Valid @RequestBody(required = false) AllocationRequest request) {
        AllocationRequest req = request != null ? request : new AllocationRequest();
        return ResponseEntity.ok(allocationService.generateAllocation(principal.getName(), donationId, req));
    }

    @GetMapping("/{allocationId}")
    public ResponseEntity<AllocationResponse> getAllocationById(
            Principal principal,
            @PathVariable String allocationId) {
        return ResponseEntity.ok(allocationService.getAllocationById(principal.getName(), allocationId));
    }

    @GetMapping("/{allocationId}/my-portion")
    public ResponseEntity<NGOAllocationResponse> getNgoAllocationById(
            Principal principal,
            @PathVariable String allocationId) {
        return ResponseEntity.ok(allocationService.getNgoAllocationById(principal.getName(), allocationId));
    }

    @GetMapping("/donation/{donationId}")
    public ResponseEntity<AllocationResponse> getAllocationByDonationId(
            Principal principal,
            @PathVariable String donationId) {
        return ResponseEntity.ok(allocationService.getAllocationByDonationId(principal.getName(), donationId));
    }

    @GetMapping("/donation/{donationId}/my-portion")
    public ResponseEntity<NGOAllocationResponse> getNgoAllocationByDonationId(
            Principal principal,
            @PathVariable String donationId) {
        return ResponseEntity.ok(allocationService.getNgoAllocationByDonationId(principal.getName(), donationId));
    }

    @GetMapping("/history")
    public ResponseEntity<Page<AllocationSummaryResponse>> getAllocationHistory(
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (page < 0) page = 0;
        if (size < 1 || size > 50) size = 10;
        return ResponseEntity.ok(allocationService.getAllocationHistory(principal.getName(), page, size));
    }
}
