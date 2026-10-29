package com.foodflow.service;

import com.foodflow.dto.ImpactTrendResponse;

import java.util.List;

public interface TimeSeriesAnalyticsService {

    List<ImpactTrendResponse> getDailyTrends();

    List<ImpactTrendResponse> getWeeklyTrends();

    List<ImpactTrendResponse> getMonthlyTrends();

    List<ImpactTrendResponse> getYearlyTrends();
}
