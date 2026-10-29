package com.foodflow.document;

import com.foodflow.enums.PredictionStatus;
import com.foodflow.enums.ReadinessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "donation_predictions")
public class DonationPredictionDocument {

    @Id
    private String id;

    @Indexed
    private String donationId;

    @Indexed
    private String restaurantId;

    private String foodType;

    private Instant preparedTime;

    private Instant predictionRequestedAt;

    private Double readinessScore;

    private ReadinessLevel readinessLevel;

    private String recommendation;

    private Double confidence;

    private Integer remainingFreshnessHours;

    private String predictionReason;

    private String modelVersion;

    private PredictionStatus predictionStatus;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
