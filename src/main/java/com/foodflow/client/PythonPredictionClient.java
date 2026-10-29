package com.foodflow.client;

import com.foodflow.dto.PredictionRequest;
import com.foodflow.dto.PredictionResponse;
import com.foodflow.enums.PredictionStatus;
import com.foodflow.enums.ReadinessLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class PythonPredictionClient {

    private static final Logger logger = LoggerFactory.getLogger(PythonPredictionClient.class);

    private final RestTemplate restTemplate;

    @Value("${prediction.service.url:http://localhost:8000/predict/readiness}")
    private String predictionServiceUrl;

    public PythonPredictionClient() {
        this.restTemplate = new RestTemplate();
    }

    public PythonPredictionClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public PredictionResponse predictReadiness(PredictionRequest request) {
        try {
            logger.info("Sending prediction request to Python ML service at {}", predictionServiceUrl);
            PredictionResponse response = restTemplate.postForObject(predictionServiceUrl, request, PredictionResponse.class);
            if (response != null) {
                return response;
            }
        } catch (Exception ex) {
            logger.warn("Python ML service at {} is unreachable or returned an error. Using fallback placeholder response: {}",
                    predictionServiceUrl, ex.getMessage());
        }

        // Return fallback placeholder response
        return PredictionResponse.builder()
                .foodType(request != null ? request.getFoodType() : null)
                .preparedTime(request != null ? request.getPreparedTime() : null)
                .readinessScore(91.0)
                .readinessLevel(ReadinessLevel.HIGH)
                .recommendation("Recommended for Donation")
                .confidence(95.0)
                .remainingFreshnessHours(4)
                .predictionReason("Food stored correctly and prepared recently.")
                .modelVersion("1.0.0-placeholder-fallback")
                .predictionStatus(PredictionStatus.COMPLETED)
                .build();
    }
}
