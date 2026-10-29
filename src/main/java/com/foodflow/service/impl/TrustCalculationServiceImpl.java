package com.foodflow.service.impl;

import com.foodflow.calculator.BayesianScoreCalculator;
import com.foodflow.calculator.TrustExplanationGenerator;
import com.foodflow.calculator.TrustLevelCalculator;
import com.foodflow.document.AllocationWorkflowDocument;
import com.foodflow.document.ScoreBreakdown;
import com.foodflow.document.TrustDocument;
import com.foodflow.dto.*;
import com.foodflow.enums.EntityType;
import com.foodflow.enums.TriggerSource;
import com.foodflow.enums.TrustLevel;
import com.foodflow.enums.WorkflowStatus;
import com.foodflow.exception.TrustNotFoundException;
import com.foodflow.repository.AllocationWorkflowRepository;
import com.foodflow.repository.NgoProfileRepository;
import com.foodflow.repository.RestaurantProfileRepository;
import com.foodflow.repository.TrustRepository;
import com.foodflow.service.TrustCalculationService;
import com.foodflow.service.TrustHistoryService;
import com.foodflow.strategy.TrustCalculationStrategy;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TrustCalculationServiceImpl implements TrustCalculationService {

    private final List<TrustCalculationStrategy> strategies;
    private final BayesianScoreCalculator bayesianCalculator;
    private final TrustLevelCalculator trustLevelCalculator;
    private final TrustExplanationGenerator explanationGenerator;
    private final TrustRepository trustRepository;
    private final TrustHistoryService trustHistoryService;
    private final AllocationWorkflowRepository workflowRepository;
    private final RestaurantProfileRepository restaurantRepository;
    private final NgoProfileRepository ngoRepository;

    public TrustCalculationServiceImpl(
            List<TrustCalculationStrategy> strategies,
            BayesianScoreCalculator bayesianCalculator,
            TrustLevelCalculator trustLevelCalculator,
            TrustExplanationGenerator explanationGenerator,
            TrustRepository trustRepository,
            TrustHistoryService trustHistoryService,
            AllocationWorkflowRepository workflowRepository,
            RestaurantProfileRepository restaurantRepository,
            NgoProfileRepository ngoRepository
    ) {
        this.strategies = strategies;
        this.bayesianCalculator = bayesianCalculator;
        this.trustLevelCalculator = trustLevelCalculator;
        this.explanationGenerator = explanationGenerator;
        this.trustRepository = trustRepository;
        this.trustHistoryService = trustHistoryService;
        this.workflowRepository = workflowRepository;
        this.restaurantRepository = restaurantRepository;
        this.ngoRepository = ngoRepository;
    }

    private TrustCalculationStrategy getStrategy(EntityType entityType) {
        return strategies.stream()
                .filter(s -> s.getEntityType() == entityType)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No calculation strategy found for entity type: " + entityType));
    }

    @Override
    public TrustResponse calculateAndUpdateTrust(String entityId, EntityType entityType, TriggerSource triggerSource, String reason) {
        TrustCalculationStrategy strategy = getStrategy(entityType);
        ScoreBreakdown breakdown = strategy.calculateScoreBreakdown(entityId);
        double weightedScore = strategy.calculateWeightedScore(breakdown);

        List<AllocationWorkflowDocument> workflows = entityType == EntityType.RESTAURANT
                ? workflowRepository.findByRestaurantIdOrderByCreatedAtDesc(entityId, Pageable.unpaged()).getContent()
                : workflowRepository.findByNgoIdOrderByCreatedAtDesc(entityId, Pageable.unpaged()).getContent();

        int totalCount = workflows.size();
        int completedCount = (int) workflows.stream().filter(w -> w.getCurrentStatus() == WorkflowStatus.COMPLETED).count();
        int cancelledCount = (int) workflows.stream().filter(w -> w.getCurrentStatus() == WorkflowStatus.CANCELLED).count();
        int failedCount = totalCount - completedCount - cancelledCount;

        double bayesianScore = bayesianCalculator.calculateBayesianScore(weightedScore, completedCount);
        double finalScore = Math.max(0.0, Math.min(100.0, bayesianScore));
        TrustLevel trustLevel = trustLevelCalculator.calculateTrustLevel(finalScore);

        Optional<TrustDocument> existingOpt = trustRepository.findByEntityIdAndEntityType(entityId, entityType);
        Double oldWeighted = existingOpt.map(TrustDocument::getWeightedScore).orElse(0.0);
        Double oldBayesian = existingOpt.map(TrustDocument::getBayesianScore).orElse(0.0);
        Double oldFinal = existingOpt.map(TrustDocument::getFinalTrustScore).orElse(0.0);

        TrustDocument doc = existingOpt.orElseGet(() -> TrustDocument.builder()
                .entityId(entityId)
                .entityType(entityType)
                .build());

        doc.setWeightedScore(weightedScore);
        doc.setBayesianScore(bayesianScore);
        doc.setFinalTrustScore(finalScore);
        doc.setTrustLevel(trustLevel);
        doc.setScoreBreakdown(breakdown);
        doc.setSuccessfulAllocations(completedCount);
        doc.setFailedAllocations(Math.max(0, failedCount));
        doc.setCancelledAllocations(cancelledCount);
        doc.setTotalAllocations(totalCount);
        doc.setLastCalculatedAt(Instant.now());

        TrustDocument savedDoc = trustRepository.save(doc);

        trustHistoryService.logHistory(savedDoc, oldWeighted, oldBayesian, oldFinal, reason, triggerSource);

        List<String> bullets = explanationGenerator.generateExplanationBullets(
                entityType, breakdown, weightedScore, bayesianScore, finalScore, trustLevel
        );

        TrustExplanationResponse explanation = TrustExplanationResponse.builder()
                .weightedScore(weightedScore)
                .bayesianScore(bayesianScore)
                .finalTrustScore(finalScore)
                .trustLevel(trustLevel)
                .explanationBullets(bullets)
                .scoreBreakdown(breakdown)
                .build();

        return mapToResponse(savedDoc, explanation);
    }

    @Override
    public void recalculateTrustForWorkflow(String restaurantId, String ngoId, String allocationId) {
        if (restaurantId != null && !restaurantId.isBlank()) {
            calculateAndUpdateTrust(restaurantId, EntityType.RESTAURANT, TriggerSource.WORKFLOW_COMPLETED, "Workflow completed: " + allocationId);
        }
        if (ngoId != null && !ngoId.isBlank()) {
            calculateAndUpdateTrust(ngoId, EntityType.NGO, TriggerSource.WORKFLOW_COMPLETED, "Workflow completed: " + allocationId);
        }
    }

    @Override
    public TrustResponse getTrustScore(String entityId, EntityType entityType) {
        TrustDocument doc = trustRepository.findByEntityIdAndEntityType(entityId, entityType)
                .orElseGet(() -> {
                    // Compute on initial view if not computed yet
                    TrustResponse computed = calculateAndUpdateTrust(entityId, entityType, TriggerSource.SYSTEM_RECALCULATION, "Initial trust score initialization");
                    return trustRepository.findByEntityIdAndEntityType(entityId, entityType).orElseThrow();
                });

        List<String> bullets = explanationGenerator.generateExplanationBullets(
                entityType, doc.getScoreBreakdown(), doc.getWeightedScore(), doc.getBayesianScore(), doc.getFinalTrustScore(), doc.getTrustLevel()
        );

        TrustExplanationResponse explanation = TrustExplanationResponse.builder()
                .weightedScore(doc.getWeightedScore())
                .bayesianScore(doc.getBayesianScore())
                .finalTrustScore(doc.getFinalTrustScore())
                .trustLevel(doc.getTrustLevel())
                .explanationBullets(bullets)
                .scoreBreakdown(doc.getScoreBreakdown())
                .build();

        return mapToResponse(doc, explanation);
    }

    @Override
    public TrustExplanationResponse getTrustExplanation(String entityId, EntityType entityType) {
        TrustResponse response = getTrustScore(entityId, entityType);
        return response.getExplanation();
    }

    @Override
    public LeaderboardResponse getLeaderboard(EntityType entityType) {
        List<TrustDocument> trustDocs = trustRepository.findByEntityTypeOrderByFinalTrustScoreDesc(entityType);

        List<TrustSummaryResponse> rankings = new ArrayList<>();
        int rank = 1;

        for (TrustDocument doc : trustDocs) {
            String name = "Entity " + doc.getEntityId();
            if (entityType == EntityType.RESTAURANT) {
                name = restaurantRepository.findById(doc.getEntityId())
                        .map(r -> r.getRestaurantName())
                        .or(() -> restaurantRepository.findByUserId(doc.getEntityId()).map(r -> r.getRestaurantName()))
                        .orElse(name);
            } else {
                name = ngoRepository.findById(doc.getEntityId())
                        .map(n -> n.getNgoName())
                        .or(() -> ngoRepository.findByUserId(doc.getEntityId()).map(n -> n.getNgoName()))
                        .orElse(name);
            }

            rankings.add(TrustSummaryResponse.builder()
                    .entityId(doc.getEntityId())
                    .entityName(name)
                    .entityType(entityType)
                    .finalTrustScore(doc.getFinalTrustScore())
                    .trustLevel(doc.getTrustLevel())
                    .totalAllocations(doc.getTotalAllocations())
                    .rank(rank++)
                    .build());
        }

        return LeaderboardResponse.builder()
                .entityType(entityType)
                .totalEntities(rankings.size())
                .rankings(rankings)
                .build();
    }

    @Override
    public TrustRecalculationResponse forceRecalculation(String entityId, EntityType entityType) {
        TrustDocument before = trustRepository.findByEntityIdAndEntityType(entityId, entityType).orElse(null);
        Double previousScore = before != null ? before.getFinalTrustScore() : 0.0;

        TrustResponse updated = calculateAndUpdateTrust(entityId, entityType, TriggerSource.ADMIN_RECALCULATION, "Manual admin recalculation triggered");

        return TrustRecalculationResponse.builder()
                .entityId(entityId)
                .entityType(entityType)
                .previousFinalScore(previousScore)
                .newFinalScore(updated.getFinalTrustScore())
                .trustLevel(updated.getTrustLevel())
                .message("Trust score successfully recalculated.")
                .recalculatedAt(Instant.now())
                .build();
    }

    private TrustResponse mapToResponse(TrustDocument doc, TrustExplanationResponse explanation) {
        return TrustResponse.builder()
                .id(doc.getId())
                .entityId(doc.getEntityId())
                .entityType(doc.getEntityType())
                .weightedScore(doc.getWeightedScore())
                .bayesianScore(doc.getBayesianScore())
                .finalTrustScore(doc.getFinalTrustScore())
                .trustLevel(doc.getTrustLevel())
                .scoreBreakdown(doc.getScoreBreakdown())
                .successfulAllocations(doc.getSuccessfulAllocations())
                .failedAllocations(doc.getFailedAllocations())
                .cancelledAllocations(doc.getCancelledAllocations())
                .totalAllocations(doc.getTotalAllocations())
                .lastCalculatedAt(doc.getLastCalculatedAt())
                .explanation(explanation)
                .build();
    }
}
