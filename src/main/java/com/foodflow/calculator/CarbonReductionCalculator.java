package com.foodflow.calculator;

import com.foodflow.config.ImpactConfiguration;
import org.springframework.stereotype.Component;

@Component
public class CarbonReductionCalculator {

    private final ImpactConfiguration config;

    public CarbonReductionCalculator(ImpactConfiguration config) {
        this.config = config;
    }

    public double calculateCO2Reduction(double foodSavedKg) {
        double co2Factor = config.getCo2Factor();
        if (co2Factor <= 0) {
            co2Factor = 2.5;
        }
        return Math.max(0.0, foodSavedKg * co2Factor);
    }

    public double calculateTreesSaved(double co2ReductionKg) {
        double treesFactor = config.getTreesFactor();
        if (treesFactor <= 0) {
            treesFactor = 21.0;
        }
        return Math.max(0.0, co2ReductionKg / treesFactor);
    }

    public double calculateWaterSavedLiters(double foodSavedKg) {
        double waterFactor = config.getWaterFactor();
        if (waterFactor <= 0) {
            waterFactor = 1000.0;
        }
        return Math.max(0.0, foodSavedKg * waterFactor);
    }
}
