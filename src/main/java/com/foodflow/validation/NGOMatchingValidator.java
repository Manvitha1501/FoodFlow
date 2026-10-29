package com.foodflow.validation;

import com.foodflow.exception.DonationValidationException;
import org.springframework.stereotype.Component;

@Component
public class NGOMatchingValidator {

    /**
     * Validates that a string is a valid MongoDB hexadecimal ObjectId.
     * Prevents database query issues or parsing failures at the controller boundary.
     */
    public void validateObjectId(String id, String fieldName) {
        if (id == null || id.isBlank()) {
            throw new DonationValidationException(fieldName + " must not be empty");
        }
        if (!id.matches("^[a-fA-F0-9]{24}$")) {
            throw new DonationValidationException(fieldName + " must be a valid 24-character hexadecimal MongoDB ID");
        }
    }
}
