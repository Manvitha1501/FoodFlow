package com.foodflow.service.impl;

import com.foodflow.client.PythonPredictionClient;
import com.foodflow.document.DonationDocument;
import com.foodflow.document.DonationPredictionDocument;
import com.foodflow.document.RestaurantProfileDocument;
import com.foodflow.document.UserDocument;
import com.foodflow.dto.PredictionRequest;
import com.foodflow.dto.PredictionResponse;
import com.foodflow.dto.PredictionSummaryResponse;
import com.foodflow.enums.DonationStatus;
import com.foodflow.enums.Role;
import com.foodflow.exception.DonationNotFoundException;
import com.foodflow.exception.InvalidDonationStateException;
import com.foodflow.exception.PredictionNotFoundException;
import com.foodflow.exception.ProfileNotFoundException;
import com.foodflow.repository.DonationPredictionRepository;
import com.foodflow.repository.DonationRepository;
import com.foodflow.repository.RestaurantProfileRepository;
import com.foodflow.repository.UserRepository;
import com.foodflow.service.PredictionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class PredictionServiceImpl implements PredictionService {

    private final UserRepository userRepository;
    private final RestaurantProfileRepository restaurantProfileRepository;
    private final DonationRepository donationRepository;
    private final DonationPredictionRepository donationPredictionRepository;
    private final PythonPredictionClient pythonPredictionClient;

    public PredictionServiceImpl(
            UserRepository userRepository,
            RestaurantProfileRepository restaurantProfileRepository,
            DonationRepository donationRepository,
            DonationPredictionRepository donationPredictionRepository,
            PythonPredictionClient pythonPredictionClient) {
        this.userRepository = userRepository;
        this.restaurantProfileRepository = restaurantProfileRepository;
        this.donationRepository = donationRepository;
        this.donationPredictionRepository = donationPredictionRepository;
        this.pythonPredictionClient = pythonPredictionClient;
    }

    private UserDocument getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    private RestaurantProfileDocument getRestaurantProfileByEmail(String email) {
        UserDocument user = getUserByEmail(email);
        return restaurantProfileRepository.findByUserIdAndIsDeletedFalse(user.getId())
                .orElseThrow(() -> new ProfileNotFoundException("Restaurant profile not found"));
    }

    private PredictionResponse toResponse(DonationPredictionDocument doc) {
        if (doc == null) {
            return null;
        }
        return PredictionResponse.builder()
                .id(doc.getId())
                .donationId(doc.getDonationId())
                .restaurantId(doc.getRestaurantId())
                .foodType(doc.getFoodType())
                .preparedTime(doc.getPreparedTime())
                .predictionRequestedAt(doc.getPredictionRequestedAt())
                .readinessScore(doc.getReadinessScore())
                .readinessLevel(doc.getReadinessLevel())
                .recommendation(doc.getRecommendation())
                .confidence(doc.getConfidence())
                .remainingFreshnessHours(doc.getRemainingFreshnessHours())
                .predictionReason(doc.getPredictionReason())
                .modelVersion(doc.getModelVersion())
                .predictionStatus(doc.getPredictionStatus())
                .createdAt(doc.getCreatedAt())
                .build();
    }

    private PredictionSummaryResponse toSummaryResponse(DonationPredictionDocument doc) {
        if (doc == null) {
            return null;
        }
        return PredictionSummaryResponse.builder()
                .id(doc.getId())
                .donationId(doc.getDonationId())
                .restaurantId(doc.getRestaurantId())
                .readinessScore(doc.getReadinessScore())
                .readinessLevel(doc.getReadinessLevel())
                .recommendation(doc.getRecommendation())
                .predictionStatus(doc.getPredictionStatus())
                .createdAt(doc.getCreatedAt())
                .build();
    }

    @Override
    public PredictionResponse triggerPrediction(String email, String donationId) {
        RestaurantProfileDocument restaurantProfile = getRestaurantProfileByEmail(email);

        DonationDocument donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new DonationNotFoundException("Donation not found"));

        if (!donation.getRestaurantId().equals(restaurantProfile.getId())) {
            throw new AccessDeniedException("You do not own this donation");
        }

        if (!donation.isActive() || (donation.getStatus() != DonationStatus.DRAFT && donation.getStatus() != DonationStatus.PUBLISHED)) {
            throw new InvalidDonationStateException("Predictions can only be generated for draft or published donations");
        }

        Instant requestedAt = Instant.now();

        PredictionRequest request = PredictionRequest.builder()
                .foodType(donation.getFoodType())
                .preparedTime(donation.getPreparedTime())
                .storageCondition(donation.getStorageCondition())
                .packagingType(donation.getPackagingType())
                .foodTemperature(donation.getFoodTemperature())
                .quantity(donation.getQuantity())
                .build();

        PredictionResponse clientResponse = pythonPredictionClient.predictReadiness(request);

        DonationPredictionDocument predictionDoc = DonationPredictionDocument.builder()
                .donationId(donation.getId())
                .restaurantId(restaurantProfile.getId())
                .foodType(donation.getFoodType())
                .preparedTime(donation.getPreparedTime())
                .predictionRequestedAt(requestedAt)
                .readinessScore(clientResponse.getReadinessScore())
                .readinessLevel(clientResponse.getReadinessLevel())
                .recommendation(clientResponse.getRecommendation())
                .confidence(clientResponse.getConfidence())
                .remainingFreshnessHours(clientResponse.getRemainingFreshnessHours())
                .predictionReason(clientResponse.getPredictionReason())
                .modelVersion(clientResponse.getModelVersion())
                .predictionStatus(clientResponse.getPredictionStatus())
                .build();

        DonationPredictionDocument saved = donationPredictionRepository.save(predictionDoc);
        return toResponse(saved);
    }

    @Override
    public PredictionResponse getLatestPrediction(String email, String donationId) {
        UserDocument user = getUserByEmail(email);

        if (user.getRole() == Role.RESTAURANT) {
            RestaurantProfileDocument restaurantProfile = restaurantProfileRepository.findByUserIdAndIsDeletedFalse(user.getId())
                    .orElseThrow(() -> new ProfileNotFoundException("Restaurant profile not found"));

            DonationDocument donation = donationRepository.findByIdAndIsActiveTrue(donationId)
                    .orElseThrow(() -> new DonationNotFoundException("Donation not found"));

            if (!donation.getRestaurantId().equals(restaurantProfile.getId())) {
                throw new AccessDeniedException("You do not own this donation");
            }
        } else if (user.getRole() == Role.ADMIN) {
            DonationDocument donation = donationRepository.findById(donationId)
                    .orElseThrow(() -> new DonationNotFoundException("Donation not found"));
        } else {
            throw new AccessDeniedException("Access denied");
        }

        DonationPredictionDocument latest = donationPredictionRepository.findTopByDonationIdOrderByCreatedAtDesc(donationId)
                .orElseThrow(() -> new PredictionNotFoundException("No predictions found for this donation"));

        return toResponse(latest);
    }

    @Override
    public Page<PredictionSummaryResponse> getPredictionHistory(String email, int page, int size) {
        UserDocument user = getUserByEmail(email);
        
        int sanitizedPage = Math.max(0, page);
        int sanitizedSize = Math.min(Math.max(1, size), 50);
        Pageable pageable = PageRequest.of(sanitizedPage, sanitizedSize);

        if (user.getRole() == Role.ADMIN) {
            Page<DonationPredictionDocument> docs = donationPredictionRepository.findAllByOrderByCreatedAtDesc(pageable);
            return docs.map(this::toSummaryResponse);
        } else if (user.getRole() == Role.RESTAURANT) {
            RestaurantProfileDocument restaurantProfile = restaurantProfileRepository.findByUserIdAndIsDeletedFalse(user.getId())
                    .orElseThrow(() -> new ProfileNotFoundException("Restaurant profile not found"));

            Page<DonationPredictionDocument> docs = donationPredictionRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantProfile.getId(), pageable);
            return docs.map(this::toSummaryResponse);
        }

        throw new AccessDeniedException("Access denied");
    }
}
