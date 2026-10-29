package com.foodflow.document;

import com.foodflow.enums.EntityType;
import com.foodflow.enums.TrustLevel;
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
@Document(collection = "trust_scores")
@CompoundIndex(name = "entity_unique", def = "{'entityId': 1, 'entityType': 1}", unique = true)
public class TrustDocument {

    @Id
    private String id;

    private String entityId;

    @Indexed
    private EntityType entityType;

    private Double weightedScore;

    private Double bayesianScore;

    @Indexed
    private Double finalTrustScore;

    private TrustLevel trustLevel;

    private ScoreBreakdown scoreBreakdown;

    @Builder.Default
    private Integer successfulAllocations = 0;

    @Builder.Default
    private Integer failedAllocations = 0;

    @Builder.Default
    private Integer cancelledAllocations = 0;

    @Builder.Default
    private Integer totalAllocations = 0;

    private Instant lastCalculatedAt;

    @Version
    private Long version;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
