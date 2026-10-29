package com.foodflow.service;

import com.foodflow.dto.LeaderboardResponse;
import com.foodflow.dto.TrustExplanationResponse;
import com.foodflow.dto.TrustRecalculationResponse;
import com.foodflow.dto.TrustResponse;
import com.foodflow.enums.EntityType;
import com.foodflow.enums.TriggerSource;

public interface TrustCalculationService {

    TrustResponse calculateAndUpdateTrust(String entityId, EntityType entityType, TriggerSource triggerSource, String reason);

    void recalculateTrustForWorkflow(String restaurantId, String ngoId, String allocationId);

    TrustResponse getTrustScore(String entityId, EntityType entityType);

    TrustExplanationResponse getTrustExplanation(String entityId, EntityType entityType);

    LeaderboardResponse getLeaderboard(EntityType entityType);

    TrustRecalculationResponse forceRecalculation(String entityId, EntityType entityType);
}
