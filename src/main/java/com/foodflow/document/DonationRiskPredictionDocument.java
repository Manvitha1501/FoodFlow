package com.foodflow.document;

import com.foodflow.enums.PredictionStatus;
import com.foodflow.enums.RiskLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "donation_risk_predictions")
@CompoundIndex(name = "donation_ngo_unique", def = "{'donationId': 1, 'ngoId': 1}", unique = true)
public class DonationRiskPredictionDocument {

    @Id
    private String id;

    private String donationId;

    @Indexed
    private String restaurantId;

    @Indexed
    private String ngoId;

    private Instant predictionRequestedAt;

    private Double successProbability;

    private Double failureProbability;

    private RiskLevel riskLevel;

    private Double confidenceScore;

    private String predictionExplanation;

    private String estimatedPickupTime;

    private String estimatedDeliveryTime;

    private String modelVersion;

    private PredictionStatus predictionStatus;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
