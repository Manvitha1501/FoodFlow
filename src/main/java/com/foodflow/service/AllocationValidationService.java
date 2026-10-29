package com.foodflow.service;

import com.foodflow.document.DonationDocument;
import com.foodflow.document.NGOMatchingDocument;

public interface AllocationValidationService {
    void validateGeneration(String email, DonationDocument donation, NGOMatchingDocument matching);
}
