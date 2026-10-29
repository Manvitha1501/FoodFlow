package com.foodflow.controller;

import com.foodflow.dto.*;
import com.foodflow.enums.EntityType;
import com.foodflow.service.TrustCalculationService;
import com.foodflow.service.TrustHistoryService;
import com.foodflow.validation.TrustValidationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trust")
@Validated
public class TrustController {

    private final TrustCalculationService trustCalculationService;
    private final TrustHistoryService trustHistoryService;
    private final TrustValidationService validationService;

    public TrustController(
            TrustCalculationService trustCalculationService,
            TrustHistoryService trustHistoryService,
            TrustValidationService validationService
    ) {
        this.trustCalculationService = trustCalculationService;
        this.trustHistoryService = trustHistoryService;
        this.validationService = validationService;
    }

    @GetMapping("/restaurant/{restaurantId}")
    @PreAuthorize("hasAnyRole('RESTAURANT', 'NGO', 'ADMIN')")
    public ResponseEntity<TrustResponse> getRestaurantTrustScore(@PathVariable String restaurantId) {
        validationService.validateEntityExists(restaurantId, EntityType.RESTAURANT);
        return ResponseEntity.ok(trustCalculationService.getTrustScore(restaurantId, EntityType.RESTAURANT));
    }

    @GetMapping("/ngo/{ngoId}")
    @PreAuthorize("hasAnyRole('NGO', 'RESTAURANT', 'ADMIN')")
    public ResponseEntity<TrustResponse> getNgoTrustScore(@PathVariable String ngoId) {
        validationService.validateEntityExists(ngoId, EntityType.NGO);
        return ResponseEntity.ok(trustCalculationService.getTrustScore(ngoId, EntityType.NGO));
    }

    @GetMapping("/history/{entityType}/{entityId}")
    @PreAuthorize("hasAnyRole('RESTAURANT', 'NGO', 'ADMIN')")
    public ResponseEntity<List<TrustHistoryResponse>> getTrustHistory(
            @PathVariable EntityType entityType,
            @PathVariable String entityId
    ) {
        validationService.validateEntityExists(entityId, entityType);
        return ResponseEntity.ok(trustHistoryService.getHistory(entityId, entityType));
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('RESTAURANT', 'NGO', 'ADMIN')")
    public ResponseEntity<LeaderboardResponse> getTrustSummary() {
        return ResponseEntity.ok(trustCalculationService.getLeaderboard(EntityType.RESTAURANT));
    }

    @GetMapping("/leaderboard/restaurants")
    @PreAuthorize("hasAnyRole('RESTAURANT', 'NGO', 'ADMIN')")
    public ResponseEntity<LeaderboardResponse> getRestaurantLeaderboard() {
        return ResponseEntity.ok(trustCalculationService.getLeaderboard(EntityType.RESTAURANT));
    }

    @GetMapping("/leaderboard/ngos")
    @PreAuthorize("hasAnyRole('NGO', 'RESTAURANT', 'ADMIN')")
    public ResponseEntity<LeaderboardResponse> getNgoLeaderboard() {
        return ResponseEntity.ok(trustCalculationService.getLeaderboard(EntityType.NGO));
    }

    @PostMapping("/recalculate/{entityType}/{entityId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TrustRecalculationResponse> forceRecalculation(
            @PathVariable EntityType entityType,
            @PathVariable String entityId
    ) {
        validationService.validateEntityExists(entityId, entityType);
        return ResponseEntity.ok(trustCalculationService.forceRecalculation(entityId, entityType));
    }

    @GetMapping("/explanation/{entityType}/{entityId}")
    @PreAuthorize("hasAnyRole('RESTAURANT', 'NGO', 'ADMIN')")
    public ResponseEntity<TrustExplanationResponse> getTrustExplanation(
            @PathVariable EntityType entityType,
            @PathVariable String entityId
    ) {
        validationService.validateEntityExists(entityId, entityType);
        return ResponseEntity.ok(trustCalculationService.getTrustExplanation(entityId, entityType));
    }
}
