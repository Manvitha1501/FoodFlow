package com.foodflow.strategy;

import com.foodflow.calculator.WeightedScoreCalculator;
import com.foodflow.config.TrustWeightConfiguration;
import com.foodflow.document.AllocationWorkflowDocument;
import com.foodflow.document.ScoreBreakdown;
import com.foodflow.enums.EntityType;
import com.foodflow.enums.WorkflowStatus;
import com.foodflow.repository.AllocationWorkflowRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NGOTrustCalculationStrategy implements TrustCalculationStrategy {

    private final AllocationWorkflowRepository workflowRepository;
    private final TrustWeightConfiguration weightConfig;
    private final WeightedScoreCalculator calculator;

    public NGOTrustCalculationStrategy(
            AllocationWorkflowRepository workflowRepository,
            TrustWeightConfiguration weightConfig,
            WeightedScoreCalculator calculator
    ) {
        this.workflowRepository = workflowRepository;
        this.weightConfig = weightConfig;
        this.calculator = calculator;
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.NGO;
    }

    @Override
    public ScoreBreakdown calculateScoreBreakdown(String entityId) {
        List<AllocationWorkflowDocument> workflows = workflowRepository
                .findByNgoIdOrderByCreatedAtDesc(entityId, Pageable.unpaged())
                .getContent();

        int total = workflows.size();
        double completionRateScore = 85.0;
        double acceptanceRateScore = 90.0;

        if (total > 0) {
            long completedCount = workflows.stream().filter(w -> w.getCurrentStatus() == WorkflowStatus.COMPLETED).count();
            long acceptedCount = workflows.stream().filter(w -> w.getCurrentStatus() != WorkflowStatus.PUBLISHED).count();

            completionRateScore = ((double) completedCount / total) * 100.0;
            acceptanceRateScore = ((double) acceptedCount / total) * 100.0;
        }

        return ScoreBreakdown.builder()
                .pickupPunctuality(90.0)
                .completionRate(calculator.clamp(completionRateScore))
                .acceptanceRate(calculator.clamp(acceptanceRateScore))
                .responseSpeed(85.0)
                .restaurantRating(95.0)
                .build();
    }

    @Override
    public Double calculateWeightedScore(ScoreBreakdown breakdown) {
        TrustWeightConfiguration.NgoWeights w = weightConfig.getNgo();
        double total = (breakdown.getPickupPunctuality() * w.getPickupWeight())
                + (breakdown.getCompletionRate() * w.getCompletionWeight())
                + (breakdown.getAcceptanceRate() * w.getAcceptanceWeight())
                + (breakdown.getResponseSpeed() * w.getResponseWeight())
                + (breakdown.getRestaurantRating() * w.getRatingWeight());

        return calculator.clamp(total);
    }
}
