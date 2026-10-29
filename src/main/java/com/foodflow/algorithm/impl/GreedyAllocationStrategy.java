package com.foodflow.algorithm.impl;

import com.foodflow.algorithm.AllocationStrategy;
import com.foodflow.document.DonationDocument;
import com.foodflow.document.EmbeddedAllocation;
import com.foodflow.document.NgoProfileDocument;
import com.foodflow.document.RankedNGO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Component
public class GreedyAllocationStrategy implements AllocationStrategy {

    @Override
    public List<EmbeddedAllocation> allocate(
            DonationDocument donation,
            Map<String, NgoProfileDocument> activeNgosMap,
            List<RankedNGO> matchingRanks
    ) {
        List<EmbeddedAllocation> allocations = new ArrayList<>();
        double remainingQuantity = donation.getQuantity() != null ? donation.getQuantity() : 0.0;

        if (remainingQuantity <= 0 || matchingRanks == null || matchingRanks.isEmpty()) {
            return allocations;
        }

        // Ensure order is sorted by Rank ascending (Rank 1 first)
        List<RankedNGO> sortedRanks = new ArrayList<>(matchingRanks);
        sortedRanks.sort(Comparator.comparing(RankedNGO::getRank));

        for (RankedNGO rankedNgo : sortedRanks) {
            if (remainingQuantity <= 0) {
                break;
            }

            NgoProfileDocument ngoProfile = activeNgosMap.get(rankedNgo.getNgoId());
            if (ngoProfile == null) {
                continue;
            }

            // DESIGN NOTE (Option B - Deliberate Simplification):
            // NGO capacity is treated as nominal/static for the scope of the allocation run.
            // Real-time concurrent reservation commitment subtraction is handled out of scope 
            // of this strategy layer (coarse active-load checks are delegated to service-layer filters).
            double ngoCapacity = ngoProfile.getCapacity() != null ? ngoProfile.getCapacity().doubleValue() : 0.0;
            if (ngoCapacity <= 0.0) {
                continue;
            }

            double allocatedAmount = Math.min(remainingQuantity, ngoCapacity);
            if (allocatedAmount <= 0.0) {
                continue;
            }

            remainingQuantity -= allocatedAmount;
            double remainingCapacity = ngoCapacity - allocatedAmount;

            // Travel time calculation: ~ 30 km/h (2 mins per km) + 15 mins loading buffer
            Double distanceKm = rankedNgo.getDistanceKm();
            String estimatedPickupTime = "30 mins";
            if (distanceKm != null) {
                double timeMinutes = Math.round((distanceKm * 2.0) + 15.0);
                estimatedPickupTime = String.format("%.0f mins", timeMinutes);
            }

            String reason = String.format("Allocated %.1f %s using Greedy algorithm based on suitability matching rank %d (Score: %.1f%%)",
                    allocatedAmount,
                    donation.getQuantityUnit() != null ? donation.getQuantityUnit() : "meals",
                    rankedNgo.getRank(),
                    rankedNgo.getMatchingScore() != null ? rankedNgo.getMatchingScore() : 0.0);

            EmbeddedAllocation allocation = EmbeddedAllocation.builder()
                    .ngoId(rankedNgo.getNgoId())
                    .ngoName(ngoProfile.getNgoName() != null ? ngoProfile.getNgoName() : rankedNgo.getNgoName())
                    .matchingScore(rankedNgo.getMatchingScore())
                    .allocationRank(rankedNgo.getRank())
                    .allocatedQuantity(allocatedAmount)
                    .allocatedUnit(donation.getQuantityUnit())
                    .ngoCapacity(ngoCapacity)
                    .remainingCapacity(remainingCapacity)
                    .estimatedPickupTime(estimatedPickupTime)
                    .allocationReason(reason)
                    .build();

            allocations.add(allocation);
        }

        return allocations;
    }
}
