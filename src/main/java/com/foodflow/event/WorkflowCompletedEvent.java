package com.foodflow.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class WorkflowCompletedEvent extends ApplicationEvent {

    private final String allocationId;
    private final String donationId;
    private final String restaurantId;
    private final String ngoId;

    public WorkflowCompletedEvent(Object source, String allocationId, String donationId, String restaurantId, String ngoId) {
        super(source);
        this.allocationId = allocationId;
        this.donationId = donationId;
        this.restaurantId = restaurantId;
        this.ngoId = ngoId;
    }
}
