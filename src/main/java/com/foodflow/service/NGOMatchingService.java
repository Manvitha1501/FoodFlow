package com.foodflow.service;

import com.foodflow.dto.NGOMatchResponse;
import com.foodflow.dto.MatchingSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NGOMatchingService {
    NGOMatchResponse generateMatching(String userEmail, String donationId);
    NGOMatchResponse getLatestMatching(String userEmail, String donationId);
    Page<MatchingSummaryResponse> getMatchingHistory(String userEmail, Pageable pageable);
}
