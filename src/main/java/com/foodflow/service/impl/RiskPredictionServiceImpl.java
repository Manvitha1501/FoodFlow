package com.foodflow.service.impl;

import com.foodflow.client.PythonRiskPredictionClient;
import com.foodflow.document.*;
import com.foodflow.dto.*;
import com.foodflow.enums.DonationStatus;
import com.foodflow.enums.PredictionStatus;
import com.foodflow.enums.Role;
import com.foodflow.enums.VerificationStatus;
import com.foodflow.exception.*;
import com.foodflow.repository.*;
import com.foodflow.service.RiskPredictionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RiskPredictionServiceImpl implements RiskPredictionService {

    private final UserRepository userRepository;
    private final RestaurantProfileRepository restaurantProfileRepository;
    private final NgoProfileRepository ngoProfileRepository;
    private final DonationRepository donationRepository;
    private final DonationRiskPredictionRepository donationRiskPredictionRepository;
    private final DonationPredictionRepository donationPredictionRepository;
    private final PythonRiskPredictionClient pythonRiskPredictionClient;

    public RiskPredictionServiceImpl(
            UserRepository userRepository,
            RestaurantProfileRepository restaurantProfileRepository,
            NgoProfileRepository ngoProfileRepository,
            DonationRepository donationRepository,
            DonationRiskPredictionRepository donationRiskPredictionRepository,
            DonationPredictionRepository donationPredictionRepository,
            PythonRiskPredictionClient pythonRiskPredictionClient) {
        this.userRepository = userRepository;
        this.restaurantProfileRepository = restaurantProfileRepository;
        this.ngoProfileRepository = ngoProfileRepository;
        this.donationRepository = donationRepository;
        this.donationRiskPredictionRepository = donationRiskPredictionRepository;
        this.donationPredictionRepository = donationPredictionRepository;
        this.pythonRiskPredictionClient = pythonRiskPredictionClient;
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

    private NgoProfileDocument getNgoProfileByEmail(String email) {
        UserDocument user = getUserByEmail(email);
        return ngoProfileRepository.findByUserIdAndIsDeletedFalse(user.getId())
                .orElseThrow(() -> new ProfileNotFoundException("NGO profile not found"));
    }

    private RiskPredictionResponse toResponse(DonationRiskPredictionDocument doc) {
        if (doc == null) {
            return null;
        }
        return RiskPredictionResponse.builder()
                .id(doc.getId())
                .donationId(doc.getDonationId())
                .restaurantId(doc.getRestaurantId())
                .ngoId(doc.getNgoId())
                .predictionRequestedAt(doc.getPredictionRequestedAt())
                .successProbability(doc.getSuccessProbability())
                .failureProbability(doc.getFailureProbability())
                .riskLevel(doc.getRiskLevel())
                .confidenceScore(doc.getConfidenceScore())
                .predictionExplanation(doc.getPredictionExplanation())
                .estimatedPickupTime(doc.getEstimatedPickupTime())
                .estimatedDeliveryTime(doc.getEstimatedDeliveryTime())
                .modelVersion(doc.getModelVersion())
                .predictionStatus(doc.getPredictionStatus())
                .createdAt(doc.getCreatedAt())
                .updatedAt(doc.getUpdatedAt())
                .build();
    }

    private RiskPredictionSummary toSummary(DonationRiskPredictionDocument doc) {
        if (doc == null) {
            return null;
        }
        return RiskPredictionSummary.builder()
                .id(doc.getId())
                .donationId(doc.getDonationId())
                .restaurantId(doc.getRestaurantId())
                .ngoId(doc.getNgoId())
                .successProbability(doc.getSuccessProbability())
                .riskLevel(doc.getRiskLevel())
                .predictionStatus(doc.getPredictionStatus())
                .createdAt(doc.getCreatedAt())
                .build();
    }

    @Override
    public RiskPredictionResponse triggerPrediction(
            String donationId, String ngoId, RiskPredictionRequest request, String currentUsername, boolean regenerate) {

        UserDocument user = getUserByEmail(currentUsername);

        DonationDocument donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new DonationNotFoundException("Donation not found"));

        NgoProfileDocument ngoProfile = ngoProfileRepository.findById(ngoId)
                .orElseThrow(() -> new NgoNotFoundException("NGO not found"));

        if (ngoProfile.isDeleted() || ngoProfile.getVerificationStatus() != VerificationStatus.APPROVED) {
            throw new IllegalArgumentException("NGO must be approved and active");
        }

        RestaurantProfileDocument restaurantProfile = restaurantProfileRepository.findById(donation.getRestaurantId())
                .orElseThrow(() -> new RestaurantNotFoundException("Restaurant not found"));

        // Validate Ownership: Restaurant Owner or Admin only
        if (user.getRole() == Role.RESTAURANT) {
            RestaurantProfileDocument callerProfile = restaurantProfileRepository.findByUserIdAndIsDeletedFalse(user.getId())
                    .orElseThrow(() -> new ProfileNotFoundException("Caller restaurant profile not found"));
            if (!donation.getRestaurantId().equals(callerProfile.getId())) {
                throw new AccessDeniedException("You do not own this donation");
            }
        } else if (user.getRole() == Role.NGO) {
            throw new AccessDeniedException("NGOs are not authorized to trigger risk predictions");
        }

        // Validate Donation Status
        if (!donation.isActive() || donation.getStatus() != DonationStatus.PUBLISHED) {
            throw new InvalidDonationStateException("Predictions can only be generated for published donations");
        }

        // Duplicate Check & Upsert In-Place logic
        var existingPredictionOpt = donationRiskPredictionRepository.findByDonationIdAndNgoId(donationId, ngoId);
        DonationRiskPredictionDocument predictionDoc;

        if (existingPredictionOpt.isPresent()) {
            if (!regenerate) {
                throw new PredictionAlreadyExistsException("Risk prediction already generated for this donation and NGO");
            }
            predictionDoc = existingPredictionOpt.get();
        } else {
            predictionDoc = new DonationRiskPredictionDocument();
            predictionDoc.setDonationId(donationId);
            predictionDoc.setNgoId(ngoId);
            predictionDoc.setRestaurantId(restaurantProfile.getId());
        }

        Instant requestedAt = Instant.now();
        predictionDoc.setPredictionRequestedAt(requestedAt);
        predictionDoc.setPredictionStatus(PredictionStatus.PROCESSING);
        donationRiskPredictionRepository.save(predictionDoc);

        // Source features & fallback logic
        double remainingHours = 4.0; // standard default
        if (donation.getExpiryTime() != null) {
            long hoursBetween = Duration.between(Instant.now(), donation.getExpiryTime()).toHours();
            remainingHours = hoursBetween > 0 ? (double) hoursBetween : 0.0;
        }

        // Check if there is an existing readiness prediction to get remainingFreshnessHours
        var readinessPredictionOpt = donationPredictionRepository.findTopByDonationIdOrderByCreatedAtDesc(donationId);
        if (readinessPredictionOpt.isPresent()) {
            remainingHours = readinessPredictionOpt.get().getRemainingFreshnessHours();
        }

        PythonRiskPredictionRequest clientRequest = PythonRiskPredictionRequest.builder()
                .donationId(donation.getId())
                .foodType(donation.getFoodType())
                .quantity(donation.getQuantity())
                .preparedTime(donation.getPreparedTime())
                .expiryTime(donation.getExpiryTime())
                .storageCondition(donation.getStorageCondition() != null ? donation.getStorageCondition().name() : null)
                .packagingType(donation.getPackagingType() != null ? donation.getPackagingType().name() : null)
                .temperature(donation.getFoodTemperature())
                .remainingFreshnessHours(remainingHours)
                .restaurantId(restaurantProfile.getId())
                .restaurantTrustScore(restaurantProfile.getTrustScore())
                .previousSuccessfulDonations(restaurantProfile.getTotalMealsDonated())
                .ngoId(ngoProfile.getId())
                .ngoTrustScore(ngoProfile.getTrustScore())
                .beneficiaryCapacity(ngoProfile.getCapacity() != null ? ngoProfile.getCapacity() : 0)
                .availableStorage(request.getAvailableStorage() != null ? request.getAvailableStorage() : 100.0)
                .averagePickupDelayMinutes(request.getAveragePickupDelayMinutes() != null ? request.getAveragePickupDelayMinutes() : 20.0)
                .completionRate(request.getCompletionRate() != null ? request.getCompletionRate() : 98.0)
                .distanceInKm(request.getDistanceInKm() != null ? request.getDistanceInKm() : 5.0)
                .estimatedTravelTimeMinutes(request.getEstimatedTravelTimeMinutes() != null ? request.getEstimatedTravelTimeMinutes() : 15)
                .weatherCondition(request.getWeatherCondition() != null ? request.getWeatherCondition() : "CLEAR")
                .trafficCondition(request.getTrafficCondition() != null ? request.getTrafficCondition() : "NORMAL")
                .build();

        try {
            RiskPredictionResponse clientResponse = pythonRiskPredictionClient.predictRisk(clientRequest);

            predictionDoc.setSuccessProbability(clientResponse.getSuccessProbability());
            predictionDoc.setFailureProbability(clientResponse.getFailureProbability());
            predictionDoc.setRiskLevel(clientResponse.getRiskLevel());
            predictionDoc.setConfidenceScore(clientResponse.getConfidenceScore());
            predictionDoc.setPredictionExplanation(clientResponse.getPredictionExplanation());
            predictionDoc.setEstimatedPickupTime(clientResponse.getEstimatedPickupTime());
            predictionDoc.setEstimatedDeliveryTime(clientResponse.getEstimatedDeliveryTime());
            predictionDoc.setModelVersion(clientResponse.getModelVersion());
            predictionDoc.setPredictionStatus(PredictionStatus.COMPLETED);
        } catch (Exception ex) {
            predictionDoc.setPredictionStatus(PredictionStatus.FAILED);
            donationRiskPredictionRepository.save(predictionDoc);
            throw new RuntimeException("Python Service Unavailable: " + ex.getMessage(), ex);
        }

        DonationRiskPredictionDocument saved = donationRiskPredictionRepository.save(predictionDoc);
        return toResponse(saved);
    }

    @Override
    public RiskPredictionResponse getPredictionById(String predictionId, String currentUsername) {
        UserDocument user = getUserByEmail(currentUsername);

        DonationRiskPredictionDocument prediction = donationRiskPredictionRepository.findById(predictionId)
                .orElseThrow(() -> new PredictionNotFoundException("Risk prediction not found"));

        // Security check per Role
        if (user.getRole() == Role.RESTAURANT) {
            RestaurantProfileDocument restaurantProfile = getRestaurantProfileByEmail(currentUsername);
            if (!prediction.getRestaurantId().equals(restaurantProfile.getId())) {
                throw new AccessDeniedException("You are not authorized to view this prediction");
            }
        } else if (user.getRole() == Role.NGO) {
            NgoProfileDocument ngoProfile = getNgoProfileByEmail(currentUsername);
            if (!prediction.getNgoId().equals(ngoProfile.getId())) {
                throw new AccessDeniedException("NGO is only authorized to view prediction targeting them");
            }
        }

        return toResponse(prediction);
    }

    @Override
    public Page<RiskPredictionSummary> getPredictionHistory(String currentUsername, Pageable pageable) {
        UserDocument user = getUserByEmail(currentUsername);

        if (user.getRole() == Role.ADMIN) {
            return donationRiskPredictionRepository.findAllByOrderByCreatedAtDesc(pageable)
                    .map(this::toSummary);
        } else if (user.getRole() == Role.RESTAURANT) {
            RestaurantProfileDocument restaurantProfile = getRestaurantProfileByEmail(currentUsername);
            return donationRiskPredictionRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantProfile.getId(), pageable)
                    .map(this::toSummary);
        } else {
            NgoProfileDocument ngoProfile = getNgoProfileByEmail(currentUsername);
            return donationRiskPredictionRepository.findByNgoIdOrderByCreatedAtDesc(ngoProfile.getId(), pageable)
                    .map(this::toSummary);
        }
    }

    @Override
    public List<RiskPredictionResponse> getPredictionsByDonation(String donationId, String currentUsername) {
        UserDocument user = getUserByEmail(currentUsername);

        DonationDocument donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new DonationNotFoundException("Donation not found"));

        // Security check per Role
        if (user.getRole() == Role.RESTAURANT) {
            RestaurantProfileDocument restaurantProfile = getRestaurantProfileByEmail(currentUsername);
            if (!donation.getRestaurantId().equals(restaurantProfile.getId())) {
                throw new AccessDeniedException("You are not authorized to view predictions for this donation");
            }
            return donationRiskPredictionRepository.findByDonationId(donationId).stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
        } else if (user.getRole() == Role.NGO) {
            NgoProfileDocument ngoProfile = getNgoProfileByEmail(currentUsername);
            return donationRiskPredictionRepository.findByDonationId(donationId).stream()
                    .filter(p -> p.getNgoId().equals(ngoProfile.getId()))
                    .map(this::toResponse)
                    .collect(Collectors.toList());
        }

        return donationRiskPredictionRepository.findByDonationId(donationId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
