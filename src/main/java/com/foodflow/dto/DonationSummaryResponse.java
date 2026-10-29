package com.foodflow.dto;

import com.foodflow.enums.DonationStatus;
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
public class DonationSummaryResponse {
    private String id;
    private String restaurantId;
    private String foodName;
    private Double quantity;
    private String quantityUnit;
    private Instant expiryTime;
    private DonationStatus status;
    private String foodCategory;
    private List<String> foodImages;
    private String city;
}
