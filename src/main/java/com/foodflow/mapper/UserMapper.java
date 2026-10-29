package com.foodflow.mapper;

import com.foodflow.document.NgoProfileDocument;
import com.foodflow.document.RestaurantProfileDocument;
import com.foodflow.document.UserDocument;
import com.foodflow.dto.NgoProfileResponse;
import com.foodflow.dto.RestaurantProfileResponse;
import com.foodflow.dto.UserResponse;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toUserResponse(
            UserDocument userDoc,
            RestaurantProfileDocument restaurantProfileDoc,
            NgoProfileDocument ngoProfileDoc) {

        if (userDoc == null) {
            return null;
        }

        UserResponse.UserResponseBuilder builder = UserResponse.builder()
                .id(userDoc.getId())
                .fullName(userDoc.getFullName())
                .email(userDoc.getEmail())
                .phoneNumber(userDoc.getPhoneNumber())
                .role(userDoc.getRole())
                .accountStatus(userDoc.getAccountStatus())
                .createdAt(userDoc.getCreatedAt())
                .updatedAt(userDoc.getUpdatedAt());

        if (userDoc.getRole() != null) {
            switch (userDoc.getRole()) {
                case RESTAURANT:
                    if (restaurantProfileDoc != null) {
                        builder.restaurantProfile(RestaurantProfileResponse.builder()
                                .restaurantName(restaurantProfileDoc.getRestaurantName())
                                .address(restaurantProfileDoc.getAddress())
                                .coordinates(restaurantProfileDoc.getCoordinates())
                                .licenseNumber(restaurantProfileDoc.getLicenseNumber())
                                .trustScore(restaurantProfileDoc.getTrustScore())
                                .verificationStatus(restaurantProfileDoc.getVerificationStatus())
                                .build());
                    }
                    break;

                case NGO:
                    if (ngoProfileDoc != null) {
                        builder.ngoProfile(NgoProfileResponse.builder()
                                .ngoName(ngoProfileDoc.getNgoName())
                                .address(ngoProfileDoc.getAddress())
                                .coordinates(ngoProfileDoc.getCoordinates())
                                .capacity(ngoProfileDoc.getCapacity())
                                .storageType(ngoProfileDoc.getStorageType())
                                .trustScore(ngoProfileDoc.getTrustScore())
                                .verificationStatus(ngoProfileDoc.getVerificationStatus())
                                .build());
                    }
                    break;

                case ADMIN:
                    // Admin does not have profile responses
                    break;
            }
        }

        return builder.build();
    }
}
