package com.foodflow.validation.impl;

import com.foodflow.enums.EntityType;
import com.foodflow.exception.EntityNotFoundException;
import com.foodflow.repository.NgoProfileRepository;
import com.foodflow.repository.RestaurantProfileRepository;
import com.foodflow.repository.UserRepository;
import com.foodflow.validation.TrustValidationService;
import org.springframework.stereotype.Service;

@Service
public class TrustValidationServiceImpl implements TrustValidationService {

    private final RestaurantProfileRepository restaurantRepository;
    private final NgoProfileRepository ngoRepository;
    private final UserRepository userRepository;

    public TrustValidationServiceImpl(
            RestaurantProfileRepository restaurantRepository,
            NgoProfileRepository ngoRepository,
            UserRepository userRepository
    ) {
        this.restaurantRepository = restaurantRepository;
        this.ngoRepository = ngoRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void validateEntityExists(String entityId, EntityType entityType) {
        if (entityId == null || entityId.isBlank()) {
            throw new IllegalArgumentException("Entity ID cannot be null or empty.");
        }

        if (entityType == EntityType.RESTAURANT) {
            boolean exists = restaurantRepository.existsById(entityId)
                    || restaurantRepository.findByUserId(entityId).isPresent()
                    || userRepository.existsById(entityId);
            if (!exists) {
                throw new EntityNotFoundException("Restaurant entity not found with ID: " + entityId);
            }
        } else if (entityType == EntityType.NGO) {
            boolean exists = ngoRepository.existsById(entityId)
                    || ngoRepository.findByUserId(entityId).isPresent()
                    || userRepository.existsById(entityId);
            if (!exists) {
                throw new EntityNotFoundException("NGO entity not found with ID: " + entityId);
            }
        }
    }
}
