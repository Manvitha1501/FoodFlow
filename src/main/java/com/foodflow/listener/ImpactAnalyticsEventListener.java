package com.foodflow.listener;

import com.foodflow.event.WorkflowCompletedEvent;
import com.foodflow.service.ImpactAnalyticsService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ImpactAnalyticsEventListener {

    private final ImpactAnalyticsService analyticsService;

    public ImpactAnalyticsEventListener(ImpactAnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @EventListener
    public void handleWorkflowCompleted(WorkflowCompletedEvent event) {
        analyticsService.processWorkflowCompleted(
                event.getAllocationId(),
                event.getDonationId(),
                event.getRestaurantId(),
                event.getNgoId()
        );
    }
}
