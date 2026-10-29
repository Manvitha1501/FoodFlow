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
public class NgoProfileResponse {
    private String ngoName;
    private String address;
    private GeoCoordinates coordinates;
    private Integer capacity;
    private String storageType;
    private Double trustScore;
    private VerificationStatus verificationStatus;
}
