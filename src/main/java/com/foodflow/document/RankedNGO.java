package com.foodflow.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RankedNGO {
    private String ngoId;
    private String ngoName;
    private Double matchingScore;
    private Integer rank;
    private Double distanceKm;
    private Double foodDemandScore;
    private Double storageScore;
    private Double beneficiaryScore;
    private Double foodCompatibilityScore;
    private Double trustScore;
    private Double pickupAvailabilityScore;
    private String reason;
}
