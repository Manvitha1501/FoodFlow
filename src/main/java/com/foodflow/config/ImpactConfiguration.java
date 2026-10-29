package com.foodflow.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "foodflow.analytics")
public class ImpactConfiguration {

    private double averageMealWeight = 0.45;
    private double co2Factor = 2.5;
    private double treesFactor = 21.0;
    private double waterFactor = 1000.0;
    private long dashboardRefreshInterval = 300;
}
