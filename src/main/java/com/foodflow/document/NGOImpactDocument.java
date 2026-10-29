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
@Document(collection = "ngo_impacts")
public class NGOImpactDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private String ngoId;

    private String ngoName;

    @Builder.Default
    private Double totalMealsReceived = 0.0;

    @Builder.Default
    private Double totalFoodReceivedKg = 0.0;

    @Builder.Default
    private Integer totalCompletedPickups = 0;

    @Builder.Default
    private Double averagePickupTime = 0.0;

    @Builder.Default
    private Double completionRate = 0.0;

    @Builder.Default
    private Double currentTrustScore = 0.0;

    private Instant lastUpdated;

    @Version
    private Long version;
}
