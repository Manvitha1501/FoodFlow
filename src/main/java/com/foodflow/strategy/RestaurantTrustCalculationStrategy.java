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
public class RestaurantTrustCalculationStrategy implements TrustCalculationStrategy {

    private final AllocationWorkflowRepository workflowRepository;
    private final TrustWeightConfiguration weightConfig;
    private final WeightedScoreCalculator calculator;

    public RestaurantTrustCalculationStrategy(
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
        return EntityType.RESTAURANT;
    }

    @Override
    public ScoreBreakdown calculateScoreBreakdown(String entityId) {
        List<AllocationWorkflowDocument> workflows = workflowRepository
                .findByRestaurantIdOrderByCreatedAtDesc(entityId, Pageable.unpaged())
                .getContent();

        int total = workflows.size();
        double successfulDonationsScore = 80.0;
        double cancellationScore = 90.0;

        if (total > 0) {
            long completedCount = workflows.stream().filter(w -> w.getCurrentStatus() == WorkflowStatus.COMPLETED).count();
            long cancelledCount = workflows.stream().filter(w -> w.getCurrentStatus() == WorkflowStatus.CANCELLED).count();

            successfulDonationsScore = ((double) completedCount / total) * 100.0;
            cancellationScore = 100.0 - (((double) cancelledCount / total) * 100.0);
        }

        return ScoreBreakdown.builder()
                .foodAccuracy(95.0)
                .successfulDonations(calculator.clamp(successfulDonationsScore))
                .cancellationRate(calculator.clamp(cancellationScore))
                .qualityHistory(90.0)
                .responseTime(90.0)
                .build();
    }

    @Override
    public Double calculateWeightedScore(ScoreBreakdown breakdown) {
        TrustWeightConfiguration.RestaurantWeights w = weightConfig.getRestaurant();
        double total = (breakdown.getFoodAccuracy() * w.getFoodAccuracyWeight())
                + (breakdown.getSuccessfulDonations() * w.getSuccessfulDonationWeight())
                + (breakdown.getCancellationRate() * w.getCancellationWeight())
                + (breakdown.getQualityHistory() * w.getQualityWeight())
                + (breakdown.getResponseTime() * w.getResponseWeight());

        return calculator.clamp(total);
    }
}
