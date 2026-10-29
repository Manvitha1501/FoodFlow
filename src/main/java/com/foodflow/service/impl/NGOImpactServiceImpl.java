package com.foodflow.service.impl;

import com.foodflow.calculator.DonationSuccessCalculator;
import com.foodflow.calculator.MealCalculator;
import com.foodflow.document.AllocationWorkflowDocument;
import com.foodflow.document.NGOImpactDocument;
import com.foodflow.document.TrustDocument;
import com.foodflow.dto.NGOImpactResponse;
import com.foodflow.enums.EntityType;
import com.foodflow.enums.WorkflowStatus;
import com.foodflow.repository.AllocationWorkflowRepository;
import com.foodflow.repository.NGOImpactRepository;
import com.foodflow.repository.NgoProfileRepository;
import com.foodflow.repository.TrustRepository;
import com.foodflow.service.NGOImpactService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NGOImpactServiceImpl implements NGOImpactService {

    private final NGOImpactRepository impactRepository;
    private final NgoProfileRepository ngoProfileRepository;
    private final AllocationWorkflowRepository workflowRepository;
    private final TrustRepository trustRepository;
    private final MealCalculator mealCalculator;
    private final DonationSuccessCalculator successCalculator;

    public NGOImpactServiceImpl(
            NGOImpactRepository impactRepository,
            NgoProfileRepository ngoProfileRepository,
            AllocationWorkflowRepository workflowRepository,
            TrustRepository trustRepository,
            MealCalculator mealCalculator,
            DonationSuccessCalculator successCalculator
    ) {
        this.impactRepository = impactRepository;
        this.ngoProfileRepository = ngoProfileRepository;
        this.workflowRepository = workflowRepository;
        this.trustRepository = trustRepository;
        this.mealCalculator = mealCalculator;
        this.successCalculator = successCalculator;
    }

    @Override
    public NGOImpactResponse updateNgoImpactOnWorkflowCompletion(String ngoId, double foodSavedKg) {
        String profileId = ngoProfileRepository.findByUserId(ngoId)
                .map(n -> n.getId())
                .orElse(ngoId);

        String userId = ngoProfileRepository.findById(ngoId)
                .map(n -> n.getUserId())
                .orElse(ngoId);

        double incrementKg = foodSavedKg > 0 ? foodSavedKg : 25.0;
        double meals = mealCalculator.calculateMeals(incrementKg);

        double trustScore = trustRepository.findByEntityIdAndEntityType(profileId, EntityType.NGO)
                .or(() -> trustRepository.findByEntityIdAndEntityType(userId, EntityType.NGO))
                .map(TrustDocument::getFinalTrustScore)
                .orElse(75.0);

        String ngoName = ngoProfileRepository.findById(profileId)
                .map(n -> n.getNgoName())
                .orElse("NGO " + profileId);

        NGOImpactDocument doc = impactRepository.findByNgoId(profileId)
                .or(() -> impactRepository.findByNgoId(userId))
                .orElseGet(() -> new NGOImpactDocument());

        doc.setNgoId(profileId);
        doc.setNgoName(ngoName);
        if (doc.getTotalFoodReceivedKg() == null) doc.setTotalFoodReceivedKg(0.0);
        if (doc.getTotalMealsReceived() == null) doc.setTotalMealsReceived(0.0);
        if (doc.getTotalCompletedPickups() == null) doc.setTotalCompletedPickups(0);

        doc.setTotalFoodReceivedKg(doc.getTotalFoodReceivedKg() + incrementKg);
        doc.setTotalMealsReceived(doc.getTotalMealsReceived() + meals);
        doc.setTotalCompletedPickups(doc.getTotalCompletedPickups() + 1);
        doc.setAveragePickupTime(25.0);
        doc.setCompletionRate(100.0);
        doc.setCurrentTrustScore(trustScore);
        doc.setLastUpdated(Instant.now());

        NGOImpactDocument saved = impactRepository.save(doc);
        return mapToResponse(saved);
    }

    @Override
    public NGOImpactResponse getNgoImpact(String ngoId) {
        String profileId = ngoProfileRepository.findByUserId(ngoId)
                .map(n -> n.getId())
                .orElse(ngoId);

        String userId = ngoProfileRepository.findById(ngoId)
                .map(n -> n.getUserId())
                .orElse(ngoId);

        NGOImpactDocument doc = impactRepository.findByNgoId(profileId)
                .or(() -> impactRepository.findByNgoId(userId))
                .orElseGet(() -> {
                    updateNgoImpactOnWorkflowCompletion(profileId, 0.0);
                    return impactRepository.findByNgoId(profileId)
                            .or(() -> impactRepository.findByNgoId(userId))
                            .orElseThrow();
                });
        return mapToResponse(doc);
    }

    @Override
    public List<NGOImpactResponse> getTopNgoContributors() {
        return impactRepository.findAllByOrderByTotalMealsReceivedDesc()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private NGOImpactResponse mapToResponse(NGOImpactDocument doc) {
        return NGOImpactResponse.builder()
                .ngoId(doc.getNgoId())
                .ngoName(doc.getNgoName())
                .totalMealsReceived(doc.getTotalMealsReceived())
                .totalFoodReceivedKg(doc.getTotalFoodReceivedKg())
                .totalCompletedPickups(doc.getTotalCompletedPickups())
                .averagePickupTime(doc.getAveragePickupTime())
                .completionRate(doc.getCompletionRate())
                .currentTrustScore(doc.getCurrentTrustScore())
                .lastUpdated(doc.getLastUpdated())
                .build();
    }
}
