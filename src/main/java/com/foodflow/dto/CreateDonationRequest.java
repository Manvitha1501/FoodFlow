package com.foodflow.dto;

import com.foodflow.document.GeoCoordinates;
import com.foodflow.enums.PackagingType;
import com.foodflow.enums.StorageCondition;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
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
public class CreateDonationRequest {

    @NotBlank(message = "Food name is required")
    private String foodName;

    private String foodCategory;

    private String foodDescription;

    private String foodType;

    @Positive(message = "Quantity must be greater than 0")
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

    @Valid
    private GeoCoordinates coordinates;

    private List<String> foodImages;

    private String specialInstructions;
}
