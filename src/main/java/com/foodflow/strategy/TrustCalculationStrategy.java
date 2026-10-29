package com.foodflow.strategy;

import com.foodflow.document.ScoreBreakdown;
import com.foodflow.enums.EntityType;

public interface TrustCalculationStrategy {

    EntityType getEntityType();

    ScoreBreakdown calculateScoreBreakdown(String entityId);

    Double calculateWeightedScore(ScoreBreakdown breakdown);
}
