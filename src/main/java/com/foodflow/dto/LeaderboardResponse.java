package com.foodflow.dto;

import com.foodflow.enums.EntityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaderboardResponse {
    private EntityType entityType;
    private Integer totalEntities;
    private List<TrustSummaryResponse> rankings;
}
