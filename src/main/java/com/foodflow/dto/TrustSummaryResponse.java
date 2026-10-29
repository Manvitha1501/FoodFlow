package com.foodflow.dto;

import com.foodflow.enums.EntityType;
import com.foodflow.enums.TrustLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrustSummaryResponse {
    private String entityId;
    private String entityName;
    private EntityType entityType;
    private Double finalTrustScore;
    private TrustLevel trustLevel;
    private Integer totalAllocations;
    private Integer rank;
}
