package com.foodflow.service.impl;

import com.foodflow.document.DonationDocument;
import com.foodflow.document.NgoProfileDocument;
import com.foodflow.document.RankedNGO;
import com.foodflow.enums.StorageCondition;
import com.foodflow.service.WeightedScoringService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class WeightedScoringServiceImpl implements WeightedScoringService {

    @Value("${matching.weights.food-demand:0.25}")
    private double weightFoodDemand;

    @Value("${matching.weights.storage-availability:0.20}")
    private double weightStorage;

    @Value("${matching.weights.beneficiary-capacity:0.15}")
    private double weightBeneficiary;

    @Value("${matching.weights.food-compatibility:0.15}")
    private double weightFoodCompatibility;

    @Value("${matching.weights.distance:0.10}")
    private double weightDistance;

    @Value("${matching.weights.trust-score:0.10}")
    private double weightTrustScore;

    @Value("${matching.weights.pickup-availability:0.05}")
    private double weightPickupAvailability;

    @PostConstruct
    public void validateWeights() {
        double sum = weightFoodDemand + weightStorage + weightBeneficiary + 
                     weightFoodCompatibility + weightDistance + weightTrustScore + 
                     weightPickupAvailability;
        if (Math.abs(sum - 1.0) > 0.001) {
            throw new IllegalStateException(String.format(
                "Module 6 Weight Configuration Error: Injected weights must sum to exactly 1.0. Current sum is: %f", sum
            ));
        }
    }

    @Override
    public List<RankedNGO> calculateSuitability(DonationDocument donation, List<NgoDistance> candidates) {
        List<RankedNGO> results = new ArrayList<>();
        if (candidates == null || candidates.isEmpty()) {
            return results;
        }

        // Find min/max values in the pool for relative normalization
        int minCapacity = Integer.MAX_VALUE;
        int maxCapacity = Integer.MIN_VALUE;
        int minDemand = Integer.MAX_VALUE;
        int maxDemand = Integer.MIN_VALUE;

        for (NgoDistance candidate : candidates) {
            NgoProfileDocument ngo = candidate.getNgo();
            int capacity = ngo.getCapacity() != null ? ngo.getCapacity() : 0;
            minCapacity = Math.min(minCapacity, capacity);
            maxCapacity = Math.max(maxCapacity, capacity);

            int demand = ngo.getBeneficiariesServed() != null ? ngo.getBeneficiariesServed() : 0;
            minDemand = Math.min(minDemand, demand);
            maxDemand = Math.max(maxDemand, demand);
        }

        for (NgoDistance candidate : candidates) {
            NgoProfileDocument ngo = candidate.getNgo();
            double distanceKm = candidate.getDistanceKm();

            // 1. Food Demand Score (Relative Min-Max Normalization)
            int demand = ngo.getBeneficiariesServed() != null ? ngo.getBeneficiariesServed() : 0;
            double demandScore = (maxDemand == minDemand) ? 100.0 : 
                ((double) (demand - minDemand) / (maxDemand - minDemand)) * 100.0;

            // 2. Storage Availability Score
            double storageScore = 100.0;
            if (donation.getStorageCondition() == StorageCondition.REFRIGERATED || 
                donation.getStorageCondition() == StorageCondition.FROZEN) {
                if ("DRY".equalsIgnoreCase(ngo.getStorageType())) {
                    storageScore = 0.0;
                } else if ("COLD".equalsIgnoreCase(ngo.getStorageType())) {
                    storageScore = 100.0;
                } else {
                    storageScore = 80.0; // BOTH or other
                }
            }

            // 3. Beneficiary Capacity Score (Relative Min-Max Normalization)
            int capacity = ngo.getCapacity() != null ? ngo.getCapacity() : 0;
            double beneficiaryScore = (maxCapacity == minCapacity) ? 100.0 : 
                ((double) (capacity - minCapacity) / (maxCapacity - minCapacity)) * 100.0;

            // 4. Food Type Compatibility Score (Non-Binary matching details with null-safe checks)
            double compatibilityScore = 0.0;
            List<String> accepted = ngo.getAcceptedFoodTypes();
            if (accepted != null) {
                boolean categoryMatches = accepted.stream()
                    .anyMatch(t -> t != null && t.equalsIgnoreCase(donation.getFoodCategory()));
                boolean typeMatches = accepted.stream()
                    .anyMatch(t -> t != null && t.equalsIgnoreCase(donation.getFoodType()));
                if (categoryMatches && typeMatches) {
                    compatibilityScore = 100.0;
                } else if (categoryMatches || typeMatches) {
                    compatibilityScore = 50.0;
                }
            }

            // 5. Distance Score (Absolute decay, capped at 15km)
            double distanceScore = Math.max(0.0, 100.0 - (distanceKm * 6.67));

            // 6. Trust Score
            double trust = ngo.getTrustScore() != null ? ngo.getTrustScore() : 5.0;
            double trustScore = (trust / 5.0) * 100.0;

            // 7. Pickup Vehicle Availability Score
            double pickupScore = ngo.isVehicleAvailable() ? 100.0 : 0.0;

            // 8. Calculate Final Score
            double rawFinalScore = (demandScore * weightFoodDemand) +
                                  (storageScore * weightStorage) +
                                  (beneficiaryScore * weightBeneficiary) +
                                  (compatibilityScore * weightFoodCompatibility) +
                                  (distanceScore * weightDistance) +
                                  (trustScore * weightTrustScore) +
                                  (pickupScore * weightPickupAvailability);

            double finalScore = Math.round(rawFinalScore * 10.0) / 10.0;

            // Build dynamic text justification for ranking
            StringBuilder reasonBuilder = new StringBuilder();
            if (finalScore >= 85.0) reasonBuilder.append("Excellent suitability: ");
            else if (finalScore >= 60.0) reasonBuilder.append("Good suitability: ");
            else reasonBuilder.append("Moderate suitability: ");

            if (distanceKm <= 3.0) reasonBuilder.append("very close distance; ");
            if (ngo.isVehicleAvailable()) reasonBuilder.append("vehicle available for pickup; ");
            if (trust >= 4.5) reasonBuilder.append("high organization trust score; ");
            if (capacity >= 300) reasonBuilder.append("high beneficiary capacity; ");

            String reason = reasonBuilder.toString();
            if (reason.endsWith("; ")) {
                reason = reason.substring(0, reason.length() - 2);
            }

            results.add(RankedNGO.builder()
                    .ngoId(ngo.getId())
                    .ngoName(ngo.getNgoName())
                    .matchingScore(finalScore)
                    .distanceKm(Math.round(distanceKm * 100.0) / 100.0)
                    .foodDemandScore(Math.round(demandScore * 10.0) / 10.0)
                    .storageScore(Math.round(storageScore * 10.0) / 10.0)
                    .beneficiaryScore(Math.round(beneficiaryScore * 10.0) / 10.0)
                    .foodCompatibilityScore(Math.round(compatibilityScore * 10.0) / 10.0)
                    .trustScore(Math.round(trustScore * 10.0) / 10.0)
                    .pickupAvailabilityScore(Math.round(pickupScore * 10.0) / 10.0)
                    .reason(reason)
                    .build());
        }

        return results;
    }
}
