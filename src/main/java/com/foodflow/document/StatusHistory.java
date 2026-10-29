package com.foodflow.document;

import com.foodflow.enums.WorkflowStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatusHistory {
    private WorkflowStatus previousStatus;
    private WorkflowStatus newStatus;
    private String changedBy;
    private Instant changedAt;
    private String remarks;
}
