package com.foodflow.document;

import com.foodflow.enums.AllocationAlgorithm;
import com.foodflow.enums.AllocationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "donation_allocations")
public class DonationAllocationDocument {

    @Id
    private String id;

    @Indexed
    private String donationId;

    @Indexed
    private String restaurantId;

    private Double totalQuantity;

    private String quantityUnit;

    private Double remainingQuantity;

    @Indexed
    private AllocationStatus allocationStatus;

    private AllocationAlgorithm allocationAlgorithm;

    private Instant generatedAt;

    private List<EmbeddedAllocation> allocations;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
