package com.foodflow.service.impl;

import com.foodflow.calculator.TrendAnalysisCalculator;
import com.foodflow.document.ImpactAnalyticsDocument;
import com.foodflow.dto.ImpactTrendResponse;
import com.foodflow.repository.ImpactAnalyticsRepository;
import com.foodflow.service.TimeSeriesAnalyticsService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.WeekFields;

import java.util.List;
import java.util.Locale;

@Service
public class TimeSeriesAnalyticsServiceImpl implements TimeSeriesAnalyticsService {

    private final ImpactAnalyticsRepository analyticsRepository;
    private final TrendAnalysisCalculator trendCalculator;

    public TimeSeriesAnalyticsServiceImpl(
            ImpactAnalyticsRepository analyticsRepository,
            TrendAnalysisCalculator trendCalculator
    ) {
        this.analyticsRepository = analyticsRepository;
        this.trendCalculator = trendCalculator;
    }

    @Override
    public List<ImpactTrendResponse> getDailyTrends() {
        List<ImpactAnalyticsDocument> docs = analyticsRepository.findAllByOrderByDateAsc();
        return trendCalculator.calculateTrends(docs, "DAILY");
    }

    @Override
    public List<ImpactTrendResponse> getWeeklyTrends() {
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int week = now.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());
        List<ImpactAnalyticsDocument> docs = analyticsRepository.findByYearAndWeekOrderByDateAsc(year, week);
        return trendCalculator.calculateTrends(docs, "WEEKLY");
    }

    @Override
    public List<ImpactTrendResponse> getMonthlyTrends() {
        LocalDate now = LocalDate.now();
        List<ImpactAnalyticsDocument> docs = analyticsRepository.findByYearAndMonthOrderByDateAsc(now.getYear(), now.getMonthValue());
        return trendCalculator.calculateTrends(docs, "MONTHLY");
    }

    @Override
    public List<ImpactTrendResponse> getYearlyTrends() {
        LocalDate now = LocalDate.now();
        List<ImpactAnalyticsDocument> docs = analyticsRepository.findByYearOrderByDateAsc(now.getYear());
        return trendCalculator.calculateTrends(docs, "YEARLY");
    }
}
