package com.foodflow.dto;

import com.foodflow.document.ScoreBreakdown;
import com.foodflow.enums.EntityType;
import com.foodflow.enums.TrustLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrustResponse {
    private String id;
    private String entityId;
    private EntityType entityType;
    private Double weightedScore;
    private Double bayesianScore;
    private Double finalTrustScore;
    private TrustLevel trustLevel;
    private ScoreBreakdown scoreBreakdown;
    private Integer successfulAllocations;
    private Integer failedAllocations;
    private Integer cancelledAllocations;
    private Integer totalAllocations;
    private Instant lastCalculatedAt;
    private TrustExplanationResponse explanation;
}
