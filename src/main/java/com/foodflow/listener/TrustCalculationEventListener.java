package com.foodflow.listener;

import com.foodflow.event.WorkflowCompletedEvent;
import com.foodflow.service.TrustCalculationService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class TrustCalculationEventListener {

    private final TrustCalculationService trustCalculationService;

    public TrustCalculationEventListener(TrustCalculationService trustCalculationService) {
        this.trustCalculationService = trustCalculationService;
    }

    @EventListener
    public void handleWorkflowCompleted(WorkflowCompletedEvent event) {
        trustCalculationService.recalculateTrustForWorkflow(
                event.getRestaurantId(),
                event.getNgoId(),
                event.getAllocationId()
        );
    }
}
