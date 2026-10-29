package com.foodflow.dto;

import com.foodflow.enums.PackagingType;
import com.foodflow.enums.StorageCondition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PredictionRequest {
    private String foodType;
    private Instant preparedTime;
    private StorageCondition storageCondition;
    private PackagingType packagingType;
    private String foodTemperature;
    private Double quantity;
}
