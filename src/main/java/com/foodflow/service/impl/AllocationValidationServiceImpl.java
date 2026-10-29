package com.foodflow.service.impl;

import com.foodflow.document.DonationDocument;
import com.foodflow.document.NGOMatchingDocument;
import com.foodflow.document.RestaurantProfileDocument;
import com.foodflow.document.UserDocument;
import com.foodflow.enums.DonationStatus;
import com.foodflow.enums.Role;
import com.foodflow.exception.AllocationException;
import com.foodflow.exception.DonationValidationException;
import com.foodflow.exception.NoEligibleNgosFoundException;
import com.foodflow.exception.RestaurantNotFoundException;
import com.foodflow.repository.RestaurantProfileRepository;
import com.foodflow.repository.UserRepository;
import com.foodflow.service.AllocationValidationService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AllocationValidationServiceImpl implements AllocationValidationService {

    private final UserRepository userRepository;
    private final RestaurantProfileRepository restaurantProfileRepository;

    public AllocationValidationServiceImpl(
            UserRepository userRepository,
            RestaurantProfileRepository restaurantProfileRepository) {
        this.userRepository = userRepository;
        this.restaurantProfileRepository = restaurantProfileRepository;
    }

    @Override
    public void validateGeneration(String email, DonationDocument donation, NGOMatchingDocument matching) {
        UserDocument user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        // 1. Role defense-in-depth
        if (user.getRole() != Role.RESTAURANT && user.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Access denied: only Restaurants and Admins can trigger allocations");
        }

        // 2. Restaurant ownership validation
        if (user.getRole() == Role.RESTAURANT) {
            RestaurantProfileDocument restaurant = restaurantProfileRepository.findByUserIdAndIsDeletedFalse(user.getId())
                    .orElseThrow(() -> new RestaurantNotFoundException("Restaurant profile not found"));
            if (!donation.getRestaurantId().equals(restaurant.getId())) {
                throw new AccessDeniedException("You do not own this donation");
            }
        }

        // 3. Donation status validation
        if (donation.getStatus() != DonationStatus.PUBLISHED) {
            throw new DonationValidationException("Only published donations can be allocated");
        }

        // 4. Donation quantity validation
        if (donation.getQuantity() == null || donation.getQuantity() <= 0.0) {
            throw new DonationValidationException("Donation quantity must be greater than zero");
        }

        // 5. Matching correlation validation
        if (!matching.getDonationId().equals(donation.getId())) {
            throw new AllocationException("Matching results do not match the target donation");
        }

        // 6. Eligible candidates availability (matching phase empty pool check)
        if (matching.getRankedNGOs() == null || matching.getRankedNGOs().isEmpty()) {
            throw new NoEligibleNgosFoundException("No candidate matched NGOs exist in the matching results");
        }
    }
}
