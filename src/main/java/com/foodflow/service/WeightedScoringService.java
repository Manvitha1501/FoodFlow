package com.foodflow.service;

import com.foodflow.document.DonationDocument;
import com.foodflow.document.NgoProfileDocument;
import com.foodflow.document.RankedNGO;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

public interface WeightedScoringService {

    @Data
    @AllArgsConstructor
    class NgoDistance {
        private NgoProfileDocument ngo;
        private Double distanceKm;
    }

    List<RankedNGO> calculateSuitability(DonationDocument donation, List<NgoDistance> candidates);
}
