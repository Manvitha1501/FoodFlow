package com.foodflow.service.impl;

import com.foodflow.document.DonationDocument;
import com.foodflow.document.NGOMatchingDocument;
import com.foodflow.document.NgoProfileDocument;
import com.foodflow.document.RankedNGO;
import com.foodflow.document.RestaurantProfileDocument;
import com.foodflow.document.UserDocument;
import com.foodflow.dto.MatchingSummaryResponse;
import com.foodflow.dto.NGOMatchResponse;
import com.foodflow.dto.RankedNGOResponse;
import com.foodflow.enums.DonationStatus;
import com.foodflow.enums.MatchingStatus;
import com.foodflow.enums.Role;
import com.foodflow.enums.StorageCondition;
import com.foodflow.enums.VerificationStatus;
import com.foodflow.exception.DonationNotFoundException;
import com.foodflow.exception.DonationValidationException;
import com.foodflow.exception.MatchingNotFoundException;
import com.foodflow.exception.NoEligibleNgosFoundException;
import com.foodflow.exception.ProfileNotFoundException;
import com.foodflow.exception.RestaurantNotFoundException;
import com.foodflow.repository.DonationRepository;
import com.foodflow.repository.NGOMatchingRepository;
import com.foodflow.repository.NgoProfileRepository;
import com.foodflow.repository.RestaurantProfileRepository;
import com.foodflow.repository.UserRepository;
import com.foodflow.service.DistanceCalculationService;
import com.foodflow.service.NGOMatchingService;
import com.foodflow.service.WeightedScoringService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NGOMatchingServiceImpl implements NGOMatchingService {

    private final DonationRepository donationRepository;
    private final RestaurantProfileRepository restaurantProfileRepository;
    private final NgoProfileRepository ngoProfileRepository;
    private final UserRepository userRepository;
    private final NGOMatchingRepository ngoMatchingRepository;
    private final DistanceCalculationService distanceCalculationService;
    private final WeightedScoringService weightedScoringService;

    @Value("${matching.max-active-load:3}")
    private int maxActiveLoad;

    public NGOMatchingServiceImpl(
            DonationRepository donationRepository,
            RestaurantProfileRepository restaurantProfileRepository,
            NgoProfileRepository ngoProfileRepository,
            UserRepository userRepository,
            NGOMatchingRepository ngoMatchingRepository,
            DistanceCalculationService distanceCalculationService,
            WeightedScoringService weightedScoringService) {
        this.donationRepository = donationRepository;
        this.restaurantProfileRepository = restaurantProfileRepository;
        this.ngoProfileRepository = ngoProfileRepository;
        this.userRepository = userRepository;
        this.ngoMatchingRepository = ngoMatchingRepository;
        this.distanceCalculationService = distanceCalculationService;
        this.weightedScoringService = weightedScoringService;
    }

    @Override
    public NGOMatchResponse generateMatching(String userEmail, String donationId) {
        UserDocument user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userEmail));

        DonationDocument donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new DonationNotFoundException("Donation not found: " + donationId));

        if (user.getRole() == Role.RESTAURANT) {
            RestaurantProfileDocument restaurant = restaurantProfileRepository.findByUserIdAndIsDeletedFalse(user.getId())
                    .orElseThrow(() -> new RestaurantNotFoundException("Restaurant profile not found"));
            if (!donation.getRestaurantId().equals(restaurant.getId())) {
                throw new AccessDeniedException("You do not own this donation");
            }
        } else if (user.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only restaurants or admins can generate suitability matchings");
        }

        if (donation.getStatus() != DonationStatus.PUBLISHED) {
            throw new DonationValidationException("Matching can only be generated for PUBLISHED donations");
        }

        RestaurantProfileDocument restaurantProfile = restaurantProfileRepository.findById(donation.getRestaurantId())
                .orElseThrow(() -> new RestaurantNotFoundException("Restaurant not found"));

        List<NgoProfileDocument> allNgos = ngoProfileRepository.findAll().stream()
                .filter(ngo -> !ngo.isDeleted() && ngo.getVerificationStatus() == VerificationStatus.APPROVED)
                .collect(Collectors.toList());

        List<NgoProfileDocument> eligibleNgos = allNgos.stream()
                .filter(ngo -> isFoodCategoryAccepted(ngo, donation.getFoodCategory(), donation.getFoodType()))
                .filter(ngo -> isStorageCompatible(ngo, donation.getStorageCondition()))
                .filter(this::isOperatingHoursCompatible)
                .filter(this::isActiveLoadAcceptable)
                .collect(Collectors.toList());

        if (eligibleNgos.isEmpty()) {
            throw new NoEligibleNgosFoundException("No eligible NGOs found matching donation requirements");
        }

        List<WeightedScoringService.NgoDistance> candidates = new ArrayList<>();
        for (NgoProfileDocument ngo : eligibleNgos) {
            double distance = distanceCalculationService.calculateDistance(
                    restaurantProfile.getCoordinates(), ngo.getCoordinates()
            );
            candidates.add(new WeightedScoringService.NgoDistance(ngo, distance));
        }

        List<RankedNGO> rankedNGOs = weightedScoringService.calculateSuitability(donation, candidates);

        rankedNGOs.sort(Comparator.comparing(RankedNGO::getMatchingScore).reversed());
        for (int i = 0; i < rankedNGOs.size(); i++) {
            rankedNGOs.get(i).setRank(i + 1);
        }

        NGOMatchingDocument matching = NGOMatchingDocument.builder()
                .donationId(donationId)
                .restaurantId(donation.getRestaurantId())
                .generatedAt(Instant.now())
                .matchingVersion("1.0.0")
                .matchingStatus(MatchingStatus.COMPLETED)
                .rankedNGOs(rankedNGOs)
                .build();

        NGOMatchingDocument saved = ngoMatchingRepository.save(matching);
        return toMatchResponse(saved);
    }

    @Override
    public NGOMatchResponse getLatestMatching(String userEmail, String donationId) {
        UserDocument user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userEmail));

        DonationDocument donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new DonationNotFoundException("Donation not found: " + donationId));

        NGOMatchingDocument matching = ngoMatchingRepository.findTopByDonationIdOrderByCreatedAtDesc(donationId)
                .orElseThrow(() -> new MatchingNotFoundException("No matching results found for donation: " + donationId));

        if (user.getRole() == Role.RESTAURANT) {
            RestaurantProfileDocument restaurant = restaurantProfileRepository.findByUserIdAndIsDeletedFalse(user.getId())
                    .orElseThrow(() -> new RestaurantNotFoundException("Restaurant profile not found"));
            if (!matching.getRestaurantId().equals(restaurant.getId())) {
                throw new AccessDeniedException("You do not own this matching history");
            }
        } else if (user.getRole() == Role.NGO) {
            NgoProfileDocument ngo = ngoProfileRepository.findByUserIdAndIsDeletedFalse(user.getId())
                    .orElseThrow(() -> new ProfileNotFoundException("NGO profile not found"));
            if (donation.getNgoId() == null || !donation.getNgoId().equals(ngo.getId())) {
                throw new AccessDeniedException("You are not assigned to this matching recommendation");
            }
        }

        return toMatchResponse(matching);
    }

    @Override
    public Page<MatchingSummaryResponse> getMatchingHistory(String userEmail, Pageable pageable) {
        UserDocument user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userEmail));

        Page<NGOMatchingDocument> documents;
        if (user.getRole() == Role.ADMIN) {
            documents = ngoMatchingRepository.findAllByOrderByCreatedAtDesc(pageable);
        } else if (user.getRole() == Role.RESTAURANT) {
            RestaurantProfileDocument restaurant = restaurantProfileRepository.findByUserIdAndIsDeletedFalse(user.getId())
                    .orElseThrow(() -> new RestaurantNotFoundException("Restaurant profile not found"));
            documents = ngoMatchingRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurant.getId(), pageable);
        } else {
            throw new AccessDeniedException("NGO users do not have access to matching history list views");
        }

        return documents.map(this::toSummaryResponse);
    }

    private boolean isFoodCategoryAccepted(NgoProfileDocument ngo, String foodCategory, String foodType) {
        List<String> accepted = ngo.getAcceptedFoodTypes();
        if (accepted == null || accepted.isEmpty()) {
            return false;
        }
        return accepted.stream().anyMatch(t -> t != null && (t.equalsIgnoreCase(foodCategory) || t.equalsIgnoreCase(foodType)));
    }

    private boolean isStorageCompatible(NgoProfileDocument ngo, StorageCondition condition) {
        if (condition == StorageCondition.REFRIGERATED || condition == StorageCondition.FROZEN) {
            return !"DRY".equalsIgnoreCase(ngo.getStorageType());
        }
        return true;
    }

    private boolean isOperatingHoursCompatible(NgoProfileDocument ngo) {
        if (ngo.getOperatingHours() == null || ngo.getOperatingHours().isBlank()) {
            return true;
        }
        try {
            String hours = ngo.getOperatingHours().trim();
            if (hours.matches("\\d{2}:\\d{2}-\\d{2}:\\d{2}")) {
                String[] parts = hours.split("-");
                LocalTime open = LocalTime.parse(parts[0]);
                LocalTime close = LocalTime.parse(parts[1]);
                LocalTime now = LocalTime.now(ZoneId.of("Asia/Kolkata"));
                
                if (close.isAfter(open)) {
                    // Normal same-day range (e.g. 09:00 - 18:00)
                    return !now.isBefore(open) && !now.isAfter(close);
                } else {
                    // Cross-midnight range (e.g. 22:00 - 06:00)
                    return !now.isBefore(open) || !now.isAfter(close);
                }
            }
        } catch (Exception e) {
            return true;
        }
        return true;
    }

    private boolean isActiveLoadAcceptable(NgoProfileDocument ngo) {
        // Count active reservations or pickups assigned to this NGO (excludes COMPLETED/CANCELLED/DRAFT)
        long activeCount = donationRepository.countByNgoIdAndStatusInAndIsActiveTrue(
                ngo.getId(), 
                List.of(DonationStatus.RESERVED, DonationStatus.PICKED_UP)
        );
        return activeCount < maxActiveLoad;
    }

    private NGOMatchResponse toMatchResponse(NGOMatchingDocument doc) {
        if (doc == null) return null;

        List<RankedNGOResponse> rankedResponses = doc.getRankedNGOs().stream()
                .map(r -> RankedNGOResponse.builder()
                        .ngoId(r.getNgoId())
                        .ngoName(r.getNgoName())
                        .matchingScore(r.getMatchingScore())
                        .rank(r.getRank())
                        .distanceKm(r.getDistanceKm())
                        .foodDemandScore(r.getFoodDemandScore())
                        .storageScore(r.getStorageScore())
                        .beneficiaryScore(r.getBeneficiaryScore())
                        .foodCompatibilityScore(r.getFoodCompatibilityScore())
                        .trustScore(r.getTrustScore())
                        .pickupAvailabilityScore(r.getPickupAvailabilityScore())
                        .reason(r.getReason())
                        .build())
                .collect(Collectors.toList());

        return NGOMatchResponse.builder()
                .id(doc.getId())
                .donationId(doc.getDonationId())
                .restaurantId(doc.getRestaurantId())
                .generatedAt(doc.getGeneratedAt())
                .matchingVersion(doc.getMatchingVersion())
                .matchingStatus(doc.getMatchingStatus())
                .rankedNGOs(rankedResponses)
                .build();
    }

    private MatchingSummaryResponse toSummaryResponse(NGOMatchingDocument doc) {
        if (doc == null) return null;

        int totalNgos = 0;
        String topNgo = "N/A";
        double topScore = 0.0;

        List<RankedNGO> ranked = doc.getRankedNGOs();
        if (ranked != null && !ranked.isEmpty()) {
            totalNgos = ranked.size();
            RankedNGO top = ranked.get(0);
            topNgo = top.getNgoName();
            topScore = top.getMatchingScore();
        }

        return MatchingSummaryResponse.builder()
                .id(doc.getId())
                .donationId(doc.getDonationId())
                .generatedAt(doc.getGeneratedAt())
                .matchingStatus(doc.getMatchingStatus())
                .totalNgosMatched(totalNgos)
                .topNgoName(topNgo)
                .topMatchingScore(topScore)
                .build();
    }
}
