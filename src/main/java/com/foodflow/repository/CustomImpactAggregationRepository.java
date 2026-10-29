package com.foodflow.repository;

import com.foodflow.dto.DashboardResponse;

public interface CustomImpactAggregationRepository {

    DashboardResponse getAggregatedPlatformMetrics();
}
