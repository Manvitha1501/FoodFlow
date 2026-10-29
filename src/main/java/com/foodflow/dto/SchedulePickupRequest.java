package com.foodflow.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchedulePickupRequest {
    @NotNull(message = "Pickup scheduled time is required")
    private Instant pickupScheduledTime;
}
