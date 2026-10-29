package com.foodflow.service;

import com.foodflow.dto.AllocationRequest;
import com.foodflow.dto.AllocationResponse;
import com.foodflow.dto.AllocationSummaryResponse;
import com.foodflow.dto.NGOAllocationResponse;
import org.springframework.data.domain.Page;

public interface DonationAllocationService {

    AllocationResponse generateAllocation(String email, String donationId, AllocationRequest request);

    AllocationResponse getAllocationById(String email, String id);

    NGOAllocationResponse getNgoAllocationById(String email, String id);

    AllocationResponse getAllocationByDonationId(String email, String donationId);

    NGOAllocationResponse getNgoAllocationByDonationId(String email, String donationId);

    Page<AllocationSummaryResponse> getAllocationHistory(String email, int page, int size);
}
