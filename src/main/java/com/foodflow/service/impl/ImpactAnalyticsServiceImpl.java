package com.foodflow.service.impl;

import com.foodflow.calculator.CarbonReductionCalculator;
import com.foodflow.calculator.DonationSuccessCalculator;
import com.foodflow.calculator.FoodSavedCalculator;
import com.foodflow.calculator.MealCalculator;
import com.foodflow.document.DonationDocument;
import com.foodflow.document.ImpactAnalyticsDocument;
import com.foodflow.dto.DashboardResponse;
import com.foodflow.dto.EnvironmentImpactResponse;
import com.foodflow.repository.*;
import com.foodflow.service.ImpactAnalyticsService;
import com.foodflow.service.NGOImpactService;
import com.foodflow.service.RestaurantImpactService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;

@Service
public class ImpactAnalyticsServiceImpl implements ImpactAnalyticsService {

    private final ImpactAnalyticsRepository analyticsRepository;
    private final DonationRepository donationRepository;
    private final RestaurantProfileRepository restaurantRepository;
    private final NgoProfileRepository ngoRepository;
    private final RestaurantImpactService restaurantImpactService;
    private final NGOImpactService ngoImpactService;
    private final FoodSavedCalculator foodSavedCalculator;
    private final MealCalculator mealCalculator;
    private final CarbonReductionCalculator carbonCalculator;
    private final DonationSuccessCalculator successCalculator;

    public ImpactAnalyticsServiceImpl(
            ImpactAnalyticsRepository analyticsRepository,
            DonationRepository donationRepository,
            RestaurantProfileRepository restaurantRepository,
            NgoProfileRepository ngoRepository,
            RestaurantImpactService restaurantImpactService,
            NGOImpactService ngoImpactService,
            FoodSavedCalculator foodSavedCalculator,
            MealCalculator mealCalculator,
            CarbonReductionCalculator carbonCalculator,
            DonationSuccessCalculator successCalculator
    ) {
        this.analyticsRepository = analyticsRepository;
        this.donationRepository = donationRepository;
        this.restaurantRepository = restaurantRepository;
        this.ngoRepository = ngoRepository;
        this.restaurantImpactService = restaurantImpactService;
        this.ngoImpactService = ngoImpactService;
        this.foodSavedCalculator = foodSavedCalculator;
        this.mealCalculator = mealCalculator;
        this.carbonCalculator = carbonCalculator;
        this.successCalculator = successCalculator;
    }

    @Override
    public void processWorkflowCompleted(String allocationId, String donationId, String restaurantId, String ngoId) {
        double quantity = donationRepository.findById(donationId)
                .map(DonationDocument::getQuantity)
                .orElse(25.0);

        double foodSavedKg = foodSavedCalculator.calculateFoodSavedKg(quantity);
        double meals = mealCalculator.calculateMeals(foodSavedKg);
        double co2 = carbonCalculator.calculateCO2Reduction(foodSavedKg);

        LocalDate today = LocalDate.now();
        String dateStr = today.toString();

        ImpactAnalyticsDocument doc = analyticsRepository.findByDate(dateStr)
                .orElseGet(() -> ImpactAnalyticsDocument.builder()
                        .date(dateStr)
                        .day(today.getDayOfMonth())
                        .week(today.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear()))
                        .month(today.getMonthValue())
                        .year(today.getYear())
                        .build());

        doc.setFoodSavedKg(doc.getFoodSavedKg() + foodSavedKg);
        doc.setMealsDelivered(doc.getMealsDelivered() + meals);
        doc.setCo2ReductionKg(doc.getCo2ReductionKg() + co2);
        doc.setSuccessfulDonations(doc.getSuccessfulDonations() + 1);
        doc.setSuccessfulPickupCount(doc.getSuccessfulPickupCount() + 1);

        int totalDonations = doc.getSuccessfulDonations() + doc.getCancelledDonations();
        doc.setSuccessRate(successCalculator.calculateSuccessRate(doc.getSuccessfulDonations(), totalDonations));

        int totalPickups = doc.getSuccessfulPickupCount() + doc.getFailedPickupCount();
        doc.setPickupSuccessRate(successCalculator.calculateSuccessRate(doc.getSuccessfulPickupCount(), totalPickups));

        analyticsRepository.save(doc);

        // Update restaurant & NGO profiles
        restaurantImpactService.updateRestaurantImpactOnWorkflowCompletion(restaurantId, foodSavedKg);
        ngoImpactService.updateNgoImpactOnWorkflowCompletion(ngoId, foodSavedKg);
    }

    @Override
    public DashboardResponse getPlatformDashboard() {
        List<ImpactAnalyticsDocument> allDocs = analyticsRepository.findAll();

        double totalFood = allDocs.stream().mapToDouble(ImpactAnalyticsDocument::getFoodSavedKg).sum();
        double totalMeals = allDocs.stream().mapToDouble(ImpactAnalyticsDocument::getMealsDelivered).sum();
        double totalCO2 = allDocs.stream().mapToDouble(ImpactAnalyticsDocument::getCo2ReductionKg).sum();

        int completed = allDocs.stream().mapToInt(ImpactAnalyticsDocument::getSuccessfulDonations).sum();
        int cancelled = allDocs.stream().mapToInt(ImpactAnalyticsDocument::getCancelledDonations).sum();

        double successRate = successCalculator.calculateSuccessRate(completed, completed + cancelled);

        int totalRestaurants = (int) restaurantRepository.count();
        int totalNgos = (int) ngoRepository.count();

        return DashboardResponse.builder()
                .totalFoodSavedKg(totalFood)
                .totalMealsDelivered(totalMeals)
                .totalCO2ReductionKg(totalCO2)
                .donationSuccessRate(successRate)
                .pickupSuccessRate(100.0)
                .totalRestaurants(totalRestaurants)
                .totalNgos(totalNgos)
                .totalCompletedDonations(completed)
                .totalCancelledDonations(cancelled)
                .build();
    }

    @Override
    public EnvironmentImpactResponse getEnvironmentImpact() {
        DashboardResponse db = getPlatformDashboard();

        double foodSaved = db.getTotalFoodSavedKg();
        double meals = db.getTotalMealsDelivered();
        double co2 = db.getTotalCO2ReductionKg();

        double trees = carbonCalculator.calculateTreesSaved(co2);
        double water = carbonCalculator.calculateWaterSavedLiters(foodSaved);

        return EnvironmentImpactResponse.builder()
                .totalFoodSavedKg(foodSaved)
                .totalMealsGenerated(meals)
                .totalCO2ReductionKg(co2)
                .equivalentTreesSaved(trees)
                .equivalentWaterSavedLiters(water)
                .build();
    }
}
