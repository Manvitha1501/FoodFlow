package com.foodflow.dto;

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
public class TrustRecalculationResponse {
    private String entityId;
    private EntityType entityType;
    private Double previousFinalScore;
    private Double newFinalScore;
    private TrustLevel trustLevel;
    private String message;
    private Instant recalculatedAt;
}
