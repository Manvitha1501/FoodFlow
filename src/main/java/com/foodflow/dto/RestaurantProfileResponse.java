package com.foodflow.dto;

import com.foodflow.document.GeoCoordinates;
import com.foodflow.enums.VerificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantProfileResponse {
    private String restaurantName;
    private String address;
    private GeoCoordinates coordinates;
    private String licenseNumber;
    private Double trustScore;
    private VerificationStatus verificationStatus;
}
