package com.foodflow.service;

import com.foodflow.dto.RiskPredictionRequest;
import com.foodflow.dto.RiskPredictionResponse;
import com.foodflow.dto.RiskPredictionSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RiskPredictionService {

    RiskPredictionResponse triggerPrediction(String donationId, String ngoId, RiskPredictionRequest request, String currentUsername, boolean regenerate);

    RiskPredictionResponse getPredictionById(String predictionId, String currentUsername);

    Page<RiskPredictionSummary> getPredictionHistory(String currentUsername, Pageable pageable);

    List<RiskPredictionResponse> getPredictionsByDonation(String donationId, String currentUsername);
}
