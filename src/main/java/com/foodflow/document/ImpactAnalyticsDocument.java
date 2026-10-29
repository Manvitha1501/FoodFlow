package com.foodflow.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "impact_analytics")
@CompoundIndex(name = "date_period_idx", def = "{'year': 1, 'month': 1, 'week': 1, 'date': 1}")
public class ImpactAnalyticsDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private String date; // YYYY-MM-DD format

    private Integer day;
    private Integer week;
    private Integer month;
    private Integer year;

    @Builder.Default
    private Double foodSavedKg = 0.0;

    @Builder.Default
    private Double mealsDelivered = 0.0;

    @Builder.Default
    private Double co2ReductionKg = 0.0;

    @Builder.Default
    private Integer successfulDonations = 0;

    @Builder.Default
    private Integer cancelledDonations = 0;

    @Builder.Default
    private Integer successfulPickupCount = 0;

    @Builder.Default
    private Integer failedPickupCount = 0;

    @Builder.Default
    private Integer restaurantCount = 0;

    @Builder.Default
    private Integer ngoCount = 0;

    @Builder.Default
    private Double averagePickupTime = 0.0;

    @Builder.Default
    private Double averageDeliveryTime = 0.0;

    @Builder.Default
    private Double successRate = 0.0;

    @Builder.Default
    private Double pickupSuccessRate = 0.0;

    @Version
    private Long version;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
