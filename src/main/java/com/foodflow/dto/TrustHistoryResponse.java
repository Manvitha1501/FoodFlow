package com.foodflow.dto;

import com.foodflow.enums.EntityType;
import com.foodflow.enums.TriggerSource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrustHistoryResponse {
    private String id;
    private String trustId;
    private String entityId;
    private EntityType entityType;
    private Double previousWeightedScore;
    private Double newWeightedScore;
    private Double previousBayesianScore;
    private Double newBayesianScore;
    private Double previousFinalScore;
    private Double newFinalScore;
    private String calculationReason;
    private TriggerSource triggerSource;
    private Instant calculatedAt;
}
