package com.foodflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PythonRiskPredictionRequest {

    // Donation Features
    private String donationId;
    private String foodType;
    private Double quantity;
    private Instant preparedTime;
    private Instant expiryTime;
    private String storageCondition;
    private String packagingType;
    private String temperature;
    private Double remainingFreshnessHours;

    // Restaurant Features
    private String restaurantId;
    private Double restaurantTrustScore;
    private Integer previousSuccessfulDonations;

    // NGO Features
    private String ngoId;
    private Double ngoTrustScore;
    private Integer beneficiaryCapacity;
    private Double availableStorage;
    private Double averagePickupDelayMinutes;
    private Double completionRate;

    // Delivery Features
    private Double distanceInKm;
    private Integer estimatedTravelTimeMinutes;
    private String weatherCondition;
    private String trafficCondition;
}
