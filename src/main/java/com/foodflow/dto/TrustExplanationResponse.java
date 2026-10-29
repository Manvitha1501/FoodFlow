package com.foodflow.dto;

import com.foodflow.document.ScoreBreakdown;
import com.foodflow.enums.TrustLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrustExplanationResponse {
    private Double weightedScore;
    private Double bayesianScore;
    private Double finalTrustScore;
    private TrustLevel trustLevel;
    private List<String> explanationBullets;
    private ScoreBreakdown scoreBreakdown;
}
