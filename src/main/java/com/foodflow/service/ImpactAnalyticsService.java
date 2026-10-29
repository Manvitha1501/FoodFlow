package com.foodflow.service;

import com.foodflow.dto.DashboardResponse;
import com.foodflow.dto.EnvironmentImpactResponse;

public interface ImpactAnalyticsService {

    void processWorkflowCompleted(String allocationId, String donationId, String restaurantId, String ngoId);

    DashboardResponse getPlatformDashboard();

    EnvironmentImpactResponse getEnvironmentImpact();
}
