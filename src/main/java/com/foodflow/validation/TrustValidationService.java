package com.foodflow.validation;

import com.foodflow.enums.EntityType;

public interface TrustValidationService {

    void validateEntityExists(String entityId, EntityType entityType);
}
