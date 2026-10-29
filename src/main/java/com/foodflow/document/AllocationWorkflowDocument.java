package com.foodflow.document;

import com.foodflow.enums.WorkflowStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "allocation_workflows")
@CompoundIndex(name = "allocation_ngo_unique", def = "{'allocationId': 1, 'ngoId': 1}", unique = true)
public class AllocationWorkflowDocument {

    @Id
    private String id;

    // No redundant @Indexed on allocationId since it is the prefix of the compound index.
    private String allocationId;

    @Indexed
    private String donationId;

    @Indexed
    private String restaurantId;

    @Indexed
    private String ngoId;

    private WorkflowStatus currentStatus;

    private Instant acceptedAt;

    private Instant confirmedAt;

    private Instant pickupScheduledTime;

    private Instant pickupStartedAt;

    private Instant pickupCompletedAt;

    private Instant deliveryCompletedAt;

    private Instant completedAt;

    private Instant cancelledAt;

    private String completionRemarks;

    @Version
    private Long version;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @Builder.Default
    private List<StatusHistory> statusHistory = new ArrayList<>();
}
