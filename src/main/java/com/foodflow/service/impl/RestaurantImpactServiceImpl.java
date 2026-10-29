package com.foodflow.service.impl;

import com.foodflow.calculator.CarbonReductionCalculator;
import com.foodflow.calculator.DonationSuccessCalculator;
import com.foodflow.calculator.MealCalculator;
import com.foodflow.document.AllocationWorkflowDocument;
import com.foodflow.document.RestaurantImpactDocument;
import com.foodflow.document.TrustDocument;
import com.foodflow.dto.RestaurantImpactResponse;
import com.foodflow.enums.EntityType;
import com.foodflow.enums.WorkflowStatus;
import com.foodflow.repository.AllocationWorkflowRepository;
import com.foodflow.repository.RestaurantImpactRepository;
import com.foodflow.repository.RestaurantProfileRepository;
import com.foodflow.repository.TrustRepository;
import com.foodflow.service.RestaurantImpactService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RestaurantImpactServiceImpl implements RestaurantImpactService {

    private final RestaurantImpactRepository impactRepository;
    private final RestaurantProfileRepository restaurantProfileRepository;
    private final AllocationWorkflowRepository workflowRepository;
    private final TrustRepository trustRepository;
    private final MealCalculator mealCalculator;
    private final CarbonReductionCalculator carbonCalculator;
    private final DonationSuccessCalculator successCalculator;

    public RestaurantImpactServiceImpl(
            RestaurantImpactRepository impactRepository,
            RestaurantProfileRepository restaurantProfileRepository,
            AllocationWorkflowRepository workflowRepository,
            TrustRepository trustRepository,
            MealCalculator mealCalculator,
            CarbonReductionCalculator carbonCalculator,
            DonationSuccessCalculator successCalculator
    ) {
        this.impactRepository = impactRepository;
        this.restaurantProfileRepository = restaurantProfileRepository;
        this.workflowRepository = workflowRepository;
        this.trustRepository = trustRepository;
        this.mealCalculator = mealCalculator;
        this.carbonCalculator = carbonCalculator;
        this.successCalculator = successCalculator;
    }

    @Override
    public RestaurantImpactResponse updateRestaurantImpactOnWorkflowCompletion(String restaurantId, double foodSavedKg) {
        String profileId = restaurantProfileRepository.findByUserId(restaurantId)
                .map(r -> r.getId())
                .orElse(restaurantId);

        String userId = restaurantProfileRepository.findById(restaurantId)
                .map(r -> r.getUserId())
                .orElse(restaurantId);

        double incrementKg = foodSavedKg > 0 ? foodSavedKg : 25.0;
        double meals = mealCalculator.calculateMeals(incrementKg);
        double co2 = carbonCalculator.calculateCO2Reduction(incrementKg);

        double trustScore = trustRepository.findByEntityIdAndEntityType(profileId, EntityType.RESTAURANT)
                .or(() -> trustRepository.findByEntityIdAndEntityType(userId, EntityType.RESTAURANT))
                .map(TrustDocument::getFinalTrustScore)
                .orElse(75.0);

        String restaurantName = restaurantProfileRepository.findById(profileId)
                .map(r -> r.getRestaurantName())
                .orElse("Restaurant " + profileId);

        RestaurantImpactDocument doc = impactRepository.findByRestaurantId(profileId)
                .or(() -> impactRepository.findByRestaurantId(userId))
                .orElseGet(() -> new RestaurantImpactDocument());

        doc.setRestaurantId(profileId);
        doc.setRestaurantName(restaurantName);
        if (doc.getTotalFoodSavedKg() == null) doc.setTotalFoodSavedKg(0.0);
        if (doc.getTotalMealsDelivered() == null) doc.setTotalMealsDelivered(0.0);
        if (doc.getTotalCO2ReductionKg() == null) doc.setTotalCO2ReductionKg(0.0);
        if (doc.getTotalCompletedDonations() == null) doc.setTotalCompletedDonations(0);
        if (doc.getTotalCancelledDonations() == null) doc.setTotalCancelledDonations(0);

        doc.setTotalFoodSavedKg(doc.getTotalFoodSavedKg() + incrementKg);
        doc.setTotalMealsDelivered(doc.getTotalMealsDelivered() + meals);
        doc.setTotalCO2ReductionKg(doc.getTotalCO2ReductionKg() + co2);
        doc.setTotalCompletedDonations(doc.getTotalCompletedDonations() + 1);

        int totalDonations = doc.getTotalCompletedDonations() + doc.getTotalCancelledDonations();
        doc.setSuccessRate(successCalculator.calculateSuccessRate(doc.getTotalCompletedDonations(), totalDonations));
        doc.setAverageDonationSize(doc.getTotalCompletedDonations() > 0 ? (doc.getTotalFoodSavedKg() / doc.getTotalCompletedDonations()) : 0.0);
        doc.setCurrentTrustScore(trustScore);
        doc.setLastUpdated(Instant.now());

        RestaurantImpactDocument saved = impactRepository.save(doc);
        System.out.println("[IMPACT-DEBUG] Updated Restaurant Impact for ProfileID=" + profileId + ", UserID=" + userId + ", TotalFoodSavedKg=" + saved.getTotalFoodSavedKg());
        return mapToResponse(saved);
    }

    @Override
    public RestaurantImpactResponse getRestaurantImpact(String restaurantId) {
        String profileId = restaurantProfileRepository.findByUserId(restaurantId)
                .map(r -> r.getId())
                .orElse(restaurantId);

        String userId = restaurantProfileRepository.findById(restaurantId)
                .map(r -> r.getUserId())
                .orElse(restaurantId);

        System.out.println("[IMPACT-DEBUG] getRestaurantImpact requested ID=" + restaurantId + ", Resolved ProfileID=" + profileId + ", UserID=" + userId);

        RestaurantImpactDocument doc = impactRepository.findByRestaurantId(profileId)
                .or(() -> impactRepository.findByRestaurantId(userId))
                .orElseGet(() -> {
                    updateRestaurantImpactOnWorkflowCompletion(profileId, 0.0);
                    return impactRepository.findByRestaurantId(profileId)
                            .or(() -> impactRepository.findByRestaurantId(userId))
                            .orElseThrow();
                });
        System.out.println("[IMPACT-DEBUG] Found Document for " + restaurantId + ": TotalFoodSavedKg=" + doc.getTotalFoodSavedKg());
        return mapToResponse(doc);
    }

    @Override
    public List<RestaurantImpactResponse> getTopRestaurantContributors() {
        return impactRepository.findAllByOrderByTotalFoodSavedKgDesc()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private RestaurantImpactResponse mapToResponse(RestaurantImpactDocument doc) {
        return RestaurantImpactResponse.builder()
                .restaurantId(doc.getRestaurantId())
                .restaurantName(doc.getRestaurantName())
                .totalFoodSavedKg(doc.getTotalFoodSavedKg())
                .totalMealsDelivered(doc.getTotalMealsDelivered())
                .totalCO2ReductionKg(doc.getTotalCO2ReductionKg())
                .totalCompletedDonations(doc.getTotalCompletedDonations())
                .totalCancelledDonations(doc.getTotalCancelledDonations())
                .averageDonationSize(doc.getAverageDonationSize())
                .successRate(doc.getSuccessRate())
                .currentTrustScore(doc.getCurrentTrustScore())
                .lastUpdated(doc.getLastUpdated())
                .build();
    }
}
