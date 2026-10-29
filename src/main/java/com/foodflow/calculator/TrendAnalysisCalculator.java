package com.foodflow.calculator;

import com.foodflow.document.ImpactAnalyticsDocument;
import com.foodflow.dto.ImpactTrendResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TrendAnalysisCalculator {

    public List<ImpactTrendResponse> calculateTrends(List<ImpactAnalyticsDocument> docs, String periodType) {
        return docs.stream()
                .map(doc -> ImpactTrendResponse.builder()
                        .periodType(periodType)
                        .periodLabel(doc.getDate())
                        .foodSavedKg(doc.getFoodSavedKg())
                        .mealsDelivered(doc.getMealsDelivered())
                        .co2ReductionKg(doc.getCo2ReductionKg())
                        .successfulDonations(doc.getSuccessfulDonations())
                        .cancelledDonations(doc.getCancelledDonations())
                        .build())
                .collect(Collectors.toList());
    }
}
