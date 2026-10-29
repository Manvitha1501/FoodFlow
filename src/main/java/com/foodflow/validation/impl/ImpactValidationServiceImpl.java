package com.foodflow.validation.impl;

import com.foodflow.exception.EntityNotFoundException;
import com.foodflow.repository.NgoProfileRepository;
import com.foodflow.repository.RestaurantProfileRepository;
import com.foodflow.repository.UserRepository;
import com.foodflow.validation.ImpactValidationService;
import org.springframework.stereotype.Service;

@Service
public class ImpactValidationServiceImpl implements ImpactValidationService {

    private final RestaurantProfileRepository restaurantRepository;
    private final NgoProfileRepository ngoRepository;
    private final UserRepository userRepository;

    public ImpactValidationServiceImpl(
            RestaurantProfileRepository restaurantRepository,
            NgoProfileRepository ngoRepository,
            UserRepository userRepository
    ) {
        this.restaurantRepository = restaurantRepository;
        this.ngoRepository = ngoRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void validateRestaurantExists(String restaurantId) {
        boolean exists = restaurantRepository.existsById(restaurantId)
                || restaurantRepository.findByUserId(restaurantId).isPresent()
                || userRepository.existsById(restaurantId);
        if (!exists) {
            throw new EntityNotFoundException("Restaurant entity not found with ID: " + restaurantId);
        }
    }

    @Override
    public void validateNgoExists(String ngoId) {
        boolean exists = ngoRepository.existsById(ngoId)
                || ngoRepository.findByUserId(ngoId).isPresent()
                || userRepository.existsById(ngoId);
        if (!exists) {
            throw new EntityNotFoundException("NGO entity not found with ID: " + ngoId);
        }
    }
}
