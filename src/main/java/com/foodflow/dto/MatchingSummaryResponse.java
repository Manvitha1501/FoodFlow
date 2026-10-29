package com.foodflow.dto;

import com.foodflow.enums.MatchingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchingSummaryResponse {
    private String id;
    private String donationId;
    private Instant generatedAt;
    private MatchingStatus matchingStatus;
    private Integer totalNgosMatched;
    private String topNgoName;
    private Double topMatchingScore;
}
