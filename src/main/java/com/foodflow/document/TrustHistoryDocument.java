package com.foodflow.document;

import com.foodflow.enums.EntityType;
import com.foodflow.enums.TriggerSource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "trust_histories")
@CompoundIndex(name = "entity_history_idx", def = "{'entityId': 1, 'calculatedAt': -1}")
public class TrustHistoryDocument {

    @Id
    private String id;

    @Indexed
    private String trustId;

    private String entityId;

    @Indexed
    private EntityType entityType;

    private Double previousWeightedScore;

    private Double newWeightedScore;

    private Double previousBayesianScore;

    private Double newBayesianScore;

    private Double previousFinalScore;

    private Double newFinalScore;

    private String calculationReason;

    private TriggerSource triggerSource;

    private Instant calculatedAt;

    @CreatedDate
    private Instant createdAt;
}
