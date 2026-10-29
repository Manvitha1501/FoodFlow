package com.foodflow.dto;

import com.foodflow.enums.MatchingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NGOMatchResponse {
    private String id;
    private String donationId;
    private String restaurantId;
    private Instant generatedAt;
    private String matchingVersion;
    private MatchingStatus matchingStatus;
    private List<RankedNGOResponse> rankedNGOs;
}
