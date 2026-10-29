package com.foodflow.service;

import com.foodflow.dto.RestaurantImpactResponse;

import java.util.List;

public interface RestaurantImpactService {

    RestaurantImpactResponse updateRestaurantImpactOnWorkflowCompletion(String restaurantId, double foodSavedKg);

    RestaurantImpactResponse getRestaurantImpact(String restaurantId);

    List<RestaurantImpactResponse> getTopRestaurantContributors();
}
