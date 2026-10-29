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
public class NGOResponse {
    private String id;
    private String userId;
    private String ngoName;
    private String registrationNumber;
    private String contactPerson;
    private String email;
    private String phoneNumber;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private GeoCoordinates coordinates;
    private Integer capacity;
    private String storageType;
    private boolean vehicleAvailable;
    private String operatingHours;
    private List<String> acceptedFoodTypes;
    private String profileImage;
    private String description;
    private Double trustScore;
    private Integer beneficiariesServed;
    private VerificationStatus verificationStatus;
    private Instant createdAt;
    private Instant updatedAt;
}
