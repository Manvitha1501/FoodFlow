package com.foodflow.controller;

import com.foodflow.dto.*;
import com.foodflow.service.ImpactAnalyticsService;
import com.foodflow.service.NGOImpactService;
import com.foodflow.service.RestaurantImpactService;
import com.foodflow.service.TimeSeriesAnalyticsService;
import com.foodflow.validation.ImpactValidationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@Validated
public class ImpactAnalyticsController {

    private final ImpactAnalyticsService impactAnalyticsService;
    private final RestaurantImpactService restaurantImpactService;
    private final NGOImpactService ngoImpactService;
    private final TimeSeriesAnalyticsService timeSeriesService;
    private final ImpactValidationService validationService;

    public ImpactAnalyticsController(
            ImpactAnalyticsService impactAnalyticsService,
            RestaurantImpactService restaurantImpactService,
            NGOImpactService ngoImpactService,
            TimeSeriesAnalyticsService timeSeriesService,
            ImpactValidationService validationService
    ) {
        this.impactAnalyticsService = impactAnalyticsService;
        this.restaurantImpactService = restaurantImpactService;
        this.ngoImpactService = ngoImpactService;
        this.timeSeriesService = timeSeriesService;
        this.validationService = validationService;
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('RESTAURANT', 'NGO', 'ADMIN')")
    public ResponseEntity<DashboardResponse> getPlatformDashboard() {
        return ResponseEntity.ok(impactAnalyticsService.getPlatformDashboard());
    }

    @GetMapping("/sustainability")
    @PreAuthorize("hasAnyRole('RESTAURANT', 'NGO', 'ADMIN')")
    public ResponseEntity<DashboardResponse> getSustainabilityImpact() {
        return ResponseEntity.ok(impactAnalyticsService.getPlatformDashboard());
    }

    @GetMapping("/restaurant/{restaurantId}")
    @PreAuthorize("hasAnyRole('RESTAURANT', 'ADMIN')")
    public ResponseEntity<RestaurantImpactResponse> getRestaurantImpact(@PathVariable String restaurantId) {
        validationService.validateRestaurantExists(restaurantId);
        return ResponseEntity.ok(restaurantImpactService.getRestaurantImpact(restaurantId));
    }

    @GetMapping("/ngo/{ngoId}")
    @PreAuthorize("hasAnyRole('NGO', 'ADMIN')")
    public ResponseEntity<NGOImpactResponse> getNgoImpact(@PathVariable String ngoId) {
        validationService.validateNgoExists(ngoId);
        return ResponseEntity.ok(ngoImpactService.getNgoImpact(ngoId));
    }

    @GetMapping("/trends/daily")
    @PreAuthorize("hasAnyRole('RESTAURANT', 'NGO', 'ADMIN')")
    public ResponseEntity<List<ImpactTrendResponse>> getDailyTrends() {
        return ResponseEntity.ok(timeSeriesService.getDailyTrends());
    }

    @GetMapping("/trends/weekly")
    @PreAuthorize("hasAnyRole('RESTAURANT', 'NGO', 'ADMIN')")
    public ResponseEntity<List<ImpactTrendResponse>> getWeeklyTrends() {
        return ResponseEntity.ok(timeSeriesService.getWeeklyTrends());
    }

    @GetMapping("/trends/monthly")
    @PreAuthorize("hasAnyRole('RESTAURANT', 'NGO', 'ADMIN')")
    public ResponseEntity<List<ImpactTrendResponse>> getMonthlyTrends() {
        return ResponseEntity.ok(timeSeriesService.getMonthlyTrends());
    }

    @GetMapping("/trends/yearly")
    @PreAuthorize("hasAnyRole('RESTAURANT', 'NGO', 'ADMIN')")
    public ResponseEntity<List<ImpactTrendResponse>> getYearlyTrends() {
        return ResponseEntity.ok(timeSeriesService.getYearlyTrends());
    }

    @GetMapping("/leaderboard/restaurants")
    @PreAuthorize("hasAnyRole('RESTAURANT', 'NGO', 'ADMIN')")
    public ResponseEntity<List<RestaurantImpactResponse>> getTopRestaurants() {
        return ResponseEntity.ok(restaurantImpactService.getTopRestaurantContributors());
    }

    @GetMapping("/leaderboard/ngos")
    @PreAuthorize("hasAnyRole('RESTAURANT', 'NGO', 'ADMIN')")
    public ResponseEntity<List<NGOImpactResponse>> getTopNgos() {
        return ResponseEntity.ok(ngoImpactService.getTopNgoContributors());
    }

    @GetMapping("/environment")
    @PreAuthorize("hasAnyRole('RESTAURANT', 'NGO', 'ADMIN')")
    public ResponseEntity<EnvironmentImpactResponse> getEnvironmentImpact() {
        return ResponseEntity.ok(impactAnalyticsService.getEnvironmentImpact());
    }
}
