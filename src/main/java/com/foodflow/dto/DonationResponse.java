package com.foodflow.dto;

import com.foodflow.document.GeoCoordinates;
import com.foodflow.enums.DonationStatus;
import com.foodflow.enums.PackagingType;
import com.foodflow.enums.StorageCondition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonationResponse {
    private String id;
    private String restaurantId;
    private String foodName;
    private String foodCategory;
    private String foodDescription;
    private String foodType;
    private Double quantity;
    private String quantityUnit;
    private Instant preparedTime;
    private Instant expiryTime;
    private StorageCondition storageCondition;
    private PackagingType packagingType;
    private String foodTemperature;
    private String pickupAddress;
    private String city;
    private String state;
    private String pincode;
    private GeoCoordinates coordinates;
    private List<String> foodImages;
    private String specialInstructions;
    private DonationStatus status;
    private Instant createdAt;
    private Instant updatedAt;
    private boolean isActive;
}
