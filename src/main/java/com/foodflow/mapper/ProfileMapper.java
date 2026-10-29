package com.foodflow.mapper;

import com.foodflow.document.NgoProfileDocument;
import com.foodflow.document.RestaurantProfileDocument;
import com.foodflow.dto.NGOResponse;
import com.foodflow.dto.NGOUpdateRequest;
import com.foodflow.dto.RestaurantResponse;
import com.foodflow.dto.RestaurantUpdateRequest;
import org.springframework.stereotype.Component;

@Component
public class ProfileMapper {

    public void updateDocument(RestaurantProfileDocument doc, RestaurantUpdateRequest request) {
        if (request == null || doc == null) {
            return;
        }
        doc.setRestaurantName(request.getRestaurantName());
        doc.setOwnerName(request.getOwnerName());
        doc.setEmail(request.getEmail());
        doc.setPhoneNumber(request.getPhoneNumber());
        doc.setRestaurantType(request.getRestaurantType());
        doc.setFoodCategories(request.getFoodCategories());
        doc.setAddress(request.getAddress());
        doc.setCity(request.getCity());
        doc.setState(request.getState());
        doc.setPincode(request.getPincode());
        doc.setCoordinates(request.getCoordinates());
        doc.setOpeningTime(request.getOpeningTime());
        doc.setClosingTime(request.getClosingTime());
        doc.setProfileImage(request.getProfileImage());
        doc.setDescription(request.getDescription());
    }

    public RestaurantResponse toResponse(RestaurantProfileDocument doc) {
        if (doc == null) {
            return null;
        }
        return RestaurantResponse.builder()
                .id(doc.getId())
                .userId(doc.getUserId())
                .restaurantName(doc.getRestaurantName())
                .ownerName(doc.getOwnerName())
                .email(doc.getEmail())
                .phoneNumber(doc.getPhoneNumber())
                .licenseNumber(doc.getLicenseNumber())
                .restaurantType(doc.getRestaurantType())
                .foodCategories(doc.getFoodCategories())
                .address(doc.getAddress())
                .city(doc.getCity())
                .state(doc.getState())
                .pincode(doc.getPincode())
                .coordinates(doc.getCoordinates())
                .openingTime(doc.getOpeningTime())
                .closingTime(doc.getClosingTime())
                .profileImage(doc.getProfileImage())
                .description(doc.getDescription())
                .trustScore(doc.getTrustScore())
                .verificationStatus(doc.getVerificationStatus())
                .totalMealsDonated(doc.getTotalMealsDonated())
                .totalCO2Saved(doc.getTotalCO2Saved())
                .createdAt(doc.getCreatedAt())
                .updatedAt(doc.getUpdatedAt())
                .build();
    }

    public void updateDocument(NgoProfileDocument doc, NGOUpdateRequest request) {
        if (request == null || doc == null) {
            return;
        }
        doc.setNgoName(request.getNgoName());
        doc.setContactPerson(request.getContactPerson());
        doc.setEmail(request.getEmail());
        doc.setPhoneNumber(request.getPhoneNumber());
        doc.setAddress(request.getAddress());
        doc.setCity(request.getCity());
        doc.setState(request.getState());
        doc.setPincode(request.getPincode());
        doc.setCoordinates(request.getCoordinates());
        doc.setCapacity(request.getCapacity());
        doc.setStorageType(request.getStorageType());
        doc.setVehicleAvailable(request.isVehicleAvailable());
        doc.setOperatingHours(request.getOperatingHours());
        doc.setAcceptedFoodTypes(request.getAcceptedFoodTypes());
        doc.setProfileImage(request.getProfileImage());
        doc.setDescription(request.getDescription());
    }

    public NGOResponse toResponse(NgoProfileDocument doc) {
        if (doc == null) {
            return null;
        }
        return NGOResponse.builder()
                .id(doc.getId())
                .userId(doc.getUserId())
                .ngoName(doc.getNgoName())
                .registrationNumber(doc.getRegistrationNumber())
                .contactPerson(doc.getContactPerson())
                .email(doc.getEmail())
                .phoneNumber(doc.getPhoneNumber())
                .address(doc.getAddress())
                .city(doc.getCity())
                .state(doc.getState())
                .pincode(doc.getPincode())
                .coordinates(doc.getCoordinates())
                .capacity(doc.getCapacity())
                .storageType(doc.getStorageType())
                .vehicleAvailable(doc.isVehicleAvailable())
                .operatingHours(doc.getOperatingHours())
                .acceptedFoodTypes(doc.getAcceptedFoodTypes())
                .profileImage(doc.getProfileImage())
                .description(doc.getDescription())
                .trustScore(doc.getTrustScore())
                .beneficiariesServed(doc.getBeneficiariesServed())
                .verificationStatus(doc.getVerificationStatus())
                .createdAt(doc.getCreatedAt())
                .updatedAt(doc.getUpdatedAt())
                .build();
    }
}
