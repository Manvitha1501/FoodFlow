package com.foodflow.controller;

import com.foodflow.dto.PredictionResponse;
import com.foodflow.dto.PredictionSummaryResponse;
import com.foodflow.service.PredictionService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/predictions")
public class PredictionController {

    private final PredictionService predictionService;

    public PredictionController(PredictionService predictionService) {
        this.predictionService = predictionService;
    }

    @PostMapping("/readiness/{donationId}")
    public ResponseEntity<PredictionResponse> triggerPrediction(
            Principal principal,
            @PathVariable String donationId) {
        return ResponseEntity.ok(predictionService.triggerPrediction(principal.getName(), donationId));
    }

    @GetMapping("/readiness/{donationId}")
    public ResponseEntity<PredictionResponse> getLatestPrediction(
            Principal principal,
            @PathVariable String donationId) {
        return ResponseEntity.ok(predictionService.getLatestPrediction(principal.getName(), donationId));
    }

    @GetMapping("/history")
    public ResponseEntity<Page<PredictionSummaryResponse>> getPredictionHistory(
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(predictionService.getPredictionHistory(principal.getName(), page, size));
    }
}
