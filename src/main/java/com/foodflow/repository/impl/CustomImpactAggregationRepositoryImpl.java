package com.foodflow.repository.impl;

import com.foodflow.dto.DashboardResponse;
import com.foodflow.repository.CustomImpactAggregationRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public class CustomImpactAggregationRepositoryImpl implements CustomImpactAggregationRepository {

    private final MongoTemplate mongoTemplate;

    public CustomImpactAggregationRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public DashboardResponse getAggregatedPlatformMetrics() {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.group()
                        .sum("foodSavedKg").as("totalFoodSavedKg")
                        .sum("mealsDelivered").as("totalMealsDelivered")
                        .sum("co2ReductionKg").as("totalCO2ReductionKg")
                        .sum("successfulDonations").as("totalCompletedDonations")
                        .sum("cancelledDonations").as("totalCancelledDonations")
        );

        AggregationResults<Map> results = mongoTemplate.aggregate(agg, "impact_analytics", Map.class);
        Map<?, ?> result = results.getUniqueMappedResult();

        if (result == null) {
            return DashboardResponse.builder()
                    .totalFoodSavedKg(0.0)
                    .totalMealsDelivered(0.0)
                    .totalCO2ReductionKg(0.0)
                    .donationSuccessRate(100.0)
                    .pickupSuccessRate(100.0)
                    .totalRestaurants(0)
                    .totalNgos(0)
                    .totalCompletedDonations(0)
                    .totalCancelledDonations(0)
                    .build();
        }

        double food = result.get("totalFoodSavedKg") != null ? ((Number) result.get("totalFoodSavedKg")).doubleValue() : 0.0;
        double meals = result.get("totalMealsDelivered") != null ? ((Number) result.get("totalMealsDelivered")).doubleValue() : 0.0;
        double co2 = result.get("totalCO2ReductionKg") != null ? ((Number) result.get("totalCO2ReductionKg")).doubleValue() : 0.0;
        int completed = result.get("totalCompletedDonations") != null ? ((Number) result.get("totalCompletedDonations")).intValue() : 0;
        int cancelled = result.get("totalCancelledDonations") != null ? ((Number) result.get("totalCancelledDonations")).intValue() : 0;

        int total = completed + cancelled;
        double successRate = total > 0 ? ((double) completed / total) * 100.0 : 100.0;

        long restCount = mongoTemplate.count(new org.springframework.data.mongodb.core.query.Query(), "restaurant_profiles");
        long ngoCount = mongoTemplate.count(new org.springframework.data.mongodb.core.query.Query(), "ngo_profiles");

        return DashboardResponse.builder()
                .totalFoodSavedKg(food)
                .totalMealsDelivered(meals)
                .totalCO2ReductionKg(co2)
                .donationSuccessRate(successRate)
                .pickupSuccessRate(100.0)
                .totalRestaurants((int) restCount)
                .totalNgos((int) ngoCount)
                .totalCompletedDonations(completed)
                .totalCancelledDonations(cancelled)
                .build();
    }
}
