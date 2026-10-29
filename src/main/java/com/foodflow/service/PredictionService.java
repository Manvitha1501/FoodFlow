package com.foodflow.service;

import com.foodflow.dto.PredictionResponse;
import com.foodflow.dto.PredictionSummaryResponse;
import org.springframework.data.domain.Page;

public interface PredictionService {

    PredictionResponse triggerPrediction(String email, String donationId);

    PredictionResponse getLatestPrediction(String email, String donationId);

    Page<PredictionSummaryResponse> getPredictionHistory(String email, int page, int size);
}
