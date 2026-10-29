package com.foodflow.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "restaurant_impacts")
public class RestaurantImpactDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private String restaurantId;

    private String restaurantName;

    @Builder.Default
    private Double totalFoodSavedKg = 0.0;

    @Builder.Default
    private Double totalMealsDelivered = 0.0;

    @Builder.Default
    private Double totalCO2ReductionKg = 0.0;

    @Builder.Default
    private Integer totalCompletedDonations = 0;

    @Builder.Default
    private Integer totalCancelledDonations = 0;

    @Builder.Default
    private Double averageDonationSize = 0.0;

    @Builder.Default
    private Double successRate = 0.0;

    @Builder.Default
    private Double currentTrustScore = 0.0;

    private Instant lastUpdated;

    @Version
    private Long version;
}
