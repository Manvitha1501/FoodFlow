package com.foodflow.dto;

import com.foodflow.document.GeoCoordinates;
import com.foodflow.enums.VerificationStatus;
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
public class RestaurantResponse {
    private String id;
    private String userId;
    private String restaurantName;
    private String ownerName;
    private String email;
    private String phoneNumber;
    private String licenseNumber;
    private String restaurantType;
    private List<String> foodCategories;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private GeoCoordinates coordinates;
    private String openingTime;
    private String closingTime;
    private String profileImage;
    private String description;
    private Double trustScore;
    private VerificationStatus verificationStatus;
    private Integer totalMealsDonated;
    private Double totalCO2Saved;
    private Instant createdAt;
    private Instant updatedAt;
}
