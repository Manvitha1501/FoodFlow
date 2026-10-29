package com.foodflow.calculator;

import com.foodflow.enums.TrustLevel;
import org.springframework.stereotype.Component;

@Component
public class TrustLevelCalculator {

    public TrustLevel calculateTrustLevel(double finalScore) {
        if (finalScore >= 95.0) {
            return TrustLevel.VERY_HIGH;
        } else if (finalScore >= 80.0) {
            return TrustLevel.HIGH;
        } else if (finalScore >= 60.0) {
            return TrustLevel.MEDIUM;
        } else if (finalScore >= 40.0) {
            return TrustLevel.LOW;
        } else {
            return TrustLevel.VERY_LOW;
        }
    }
}
