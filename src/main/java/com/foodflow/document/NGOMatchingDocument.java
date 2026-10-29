package com.foodflow.document;

import com.foodflow.enums.MatchingStatus;
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
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "ngo_matchings")
public class NGOMatchingDocument {

    @Id
    private String id;

    @Indexed
    private String donationId;

    @Indexed
    private String restaurantId;

    private Instant generatedAt;

    private String matchingVersion;

    private MatchingStatus matchingStatus;

    private List<RankedNGO> rankedNGOs;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
