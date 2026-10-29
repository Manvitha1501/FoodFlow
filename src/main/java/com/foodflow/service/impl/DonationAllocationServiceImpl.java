package com.foodflow.service.impl;

import com.foodflow.algorithm.AllocationStrategy;
import com.foodflow.document.*;
import com.foodflow.dto.*;
import com.foodflow.enums.AllocationAlgorithm;
import com.foodflow.enums.AllocationStatus;
import com.foodflow.enums.Role;
import com.foodflow.enums.VerificationStatus;
import com.foodflow.exception.*;
import com.foodflow.factory.AllocationStrategyFactory;
import com.foodflow.repository.*;
import com.foodflow.service.AllocationValidationService;
import com.foodflow.service.DonationAllocationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DonationAllocationServiceImpl implements DonationAllocationService {

    private final DonationAllocationRepository donationAllocationRepository;
    private final DonationRepository donationRepository;
    private final NgoProfileRepository ngoProfileRepository;
    private final RestaurantProfileRepository restaurantProfileRepository;
    private final UserRepository userRepository;
    private final NGOMatchingRepository ngoMatchingRepository;
    private final AllocationStrategyFactory strategyFactory;
    private final AllocationValidationService validationService;

    public DonationAllocationServiceImpl(
            DonationAllocationRepository donationAllocationRepository,
            DonationRepository donationRepository,
            NgoProfileRepository ngoProfileRepository,
            RestaurantProfileRepository restaurantProfileRepository,
            UserRepository userRepository,
            NGOMatchingRepository ngoMatchingRepository,
            AllocationStrategyFactory strategyFactory,
            AllocationValidationService validationService) {
        this.donationAllocationRepository = donationAllocationRepository;
        this.donationRepository = donationRepository;
        this.ngoProfileRepository = ngoProfileRepository;
        this.restaurantProfileRepository = restaurantProfileRepository;
        this.userRepository = userRepository;
        this.ngoMatchingRepository = ngoMatchingRepository;
        this.strategyFactory = strategyFactory;
        this.validationService = validationService;
    }

    @Override
    public AllocationResponse generateAllocation(String email, String donationId, AllocationRequest request) {
        DonationDocument donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new DonationNotFoundException("Donation not found: " + donationId));

        if (donation.getStatus() != com.foodflow.enums.DonationStatus.PUBLISHED) {
            throw new DonationValidationException("Only published donations can be allocated");
        }

        NGOMatchingDocument matching = ngoMatchingRepository.findTopByDonationIdOrderByCreatedAtDesc(donationId)
                .orElseThrow(() -> new MatchingNotFoundException("No matching results found for donation: " + donationId));

        // 1. Validation
        validationService.validateGeneration(email, donation, matching);

        // Determine algorithm (defaults to GREEDY if null)
        AllocationAlgorithm algorithm = (request != null && request.getAlgorithm() != null) 
                ? request.getAlgorithm() 
                : AllocationAlgorithm.GREEDY;

        // 2. Fetch matched NGO profiles for capacity check and active checks
        List<String> matchedNgoIds = matching.getRankedNGOs().stream()
                .map(RankedNGO::getNgoId)
                .collect(Collectors.toList());

        List<NgoProfileDocument> ngoProfiles = ngoProfileRepository.findAllById(matchedNgoIds);

        // Re-verify eligibility at allocation time (APPROVED, not deleted, capacity > 0)
        Map<String, NgoProfileDocument> activeNgosMap = ngoProfiles.stream()
                .filter(ngo -> ngo.getVerificationStatus() == VerificationStatus.APPROVED 
                        && !ngo.isDeleted() 
                        && ngo.getCapacity() != null 
                        && ngo.getCapacity() > 0)
                .collect(Collectors.toMap(NgoProfileDocument::getId, Function.identity()));

        if (activeNgosMap.isEmpty()) {
            // Save a failed allocation
            DonationAllocationDocument failedDoc = DonationAllocationDocument.builder()
                    .donationId(donationId)
                    .restaurantId(donation.getRestaurantId())
                    .totalQuantity(donation.getQuantity())
                    .quantityUnit(donation.getQuantityUnit())
                    .remainingQuantity(donation.getQuantity())
                    .allocationStatus(AllocationStatus.FAILED)
                    .allocationAlgorithm(algorithm)
                    .generatedAt(Instant.now())
                    .allocations(List.of())
                    .build();
            donationAllocationRepository.save(failedDoc);

            throw new NoEligibleNgosFoundException("No eligible approved active NGOs remain for allocation");
        }

        // 3. Select strategy and allocate
        AllocationStrategy strategy = strategyFactory.getStrategy(algorithm);
        List<EmbeddedAllocation> splits;
        try {
            splits = strategy.allocate(donation, activeNgosMap, matching.getRankedNGOs());
        } catch (UnsupportedOperationException e) {
            DonationAllocationDocument failedDoc = DonationAllocationDocument.builder()
                    .donationId(donationId)
                    .restaurantId(donation.getRestaurantId())
                    .totalQuantity(donation.getQuantity())
                    .quantityUnit(donation.getQuantityUnit())
                    .remainingQuantity(donation.getQuantity())
                    .allocationStatus(AllocationStatus.FAILED)
                    .allocationAlgorithm(algorithm)
                    .generatedAt(Instant.now())
                    .allocations(List.of())
                    .build();
            donationAllocationRepository.save(failedDoc);

            throw new AllocationFailedException("The requested allocation algorithm is not implemented yet");
        }

        // 4. Calculate total allocated quantity
        double totalAllocated = splits.stream()
                .mapToDouble(EmbeddedAllocation::getAllocatedQuantity)
                .sum();

        double remainingQuantity = donation.getQuantity() - totalAllocated;

        AllocationStatus status;
        if (totalAllocated <= 0.0) {
            status = AllocationStatus.FAILED;
        } else if (remainingQuantity <= 0.0) {
            status = AllocationStatus.FULLY_ALLOCATED;
        } else {
            status = AllocationStatus.PARTIALLY_ALLOCATED;
        }

        DonationAllocationDocument allocationDoc = DonationAllocationDocument.builder()
                .donationId(donationId)
                .restaurantId(donation.getRestaurantId())
                .totalQuantity(donation.getQuantity())
                .quantityUnit(donation.getQuantityUnit())
                .remainingQuantity(remainingQuantity)
                .allocationStatus(status)
                .allocationAlgorithm(algorithm)
                .generatedAt(Instant.now())
                .allocations(splits)
                .build();

        DonationAllocationDocument saved = donationAllocationRepository.save(allocationDoc);

        if (status == AllocationStatus.FAILED) {
            throw new NoEligibleNgosFoundException("Allocation failed: no quantities could be allocated to candidate NGOs");
        }

        return toResponse(saved);
    }

    @Override
    public AllocationResponse getAllocationById(String email, String id) {
        UserDocument user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        DonationAllocationDocument doc = donationAllocationRepository.findById(id)
                .orElseThrow(() -> new AllocationException("Allocation not found: " + id));

        // Rejects NGO users from direct view of full breakdown
        if (user.getRole() == Role.NGO) {
            throw new AccessDeniedException("NGO users are blocked from accessing full allocation breakdowns");
        }

        // Restaurants can only view their own allocations
        if (user.getRole() == Role.RESTAURANT) {
            RestaurantProfileDocument restaurant = restaurantProfileRepository.findByUserIdAndIsDeletedFalse(user.getId())
                    .orElseThrow(() -> new RestaurantNotFoundException("Restaurant profile not found"));
            if (!doc.getRestaurantId().equals(restaurant.getId())) {
                throw new AccessDeniedException("You do not own this allocation");
            }
        }

        return toResponse(doc);
    }

    @Override
    public NGOAllocationResponse getNgoAllocationById(String email, String id) {
        UserDocument user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        if (user.getRole() != Role.NGO && user.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only NGOs and Admins can view individual NGO allocation slices");
        }

        DonationAllocationDocument doc = donationAllocationRepository.findById(id)
                .orElseThrow(() -> new AllocationException("Allocation not found: " + id));

        String ngoId = null;
        if (user.getRole() == Role.NGO) {
            NgoProfileDocument ngo = ngoProfileRepository.findByUserIdAndIsDeletedFalse(user.getId())
                    .orElseThrow(() -> new NgoNotFoundException("NGO profile not found"));
            ngoId = ngo.getId();
        }

        return toNgoResponse(doc, ngoId);
    }

    @Override
    public AllocationResponse getAllocationByDonationId(String email, String donationId) {
        UserDocument user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        DonationAllocationDocument doc = donationAllocationRepository.findTopByDonationIdOrderByCreatedAtDesc(donationId)
                .orElseThrow(() -> new AllocationException("No allocations found for donation: " + donationId));

        if (user.getRole() == Role.NGO) {
            throw new AccessDeniedException("NGO users are blocked from accessing full allocation breakdowns");
        }

        if (user.getRole() == Role.RESTAURANT) {
            RestaurantProfileDocument restaurant = restaurantProfileRepository.findByUserIdAndIsDeletedFalse(user.getId())
                    .orElseThrow(() -> new RestaurantNotFoundException("Restaurant profile not found"));
            if (!doc.getRestaurantId().equals(restaurant.getId())) {
                throw new AccessDeniedException("You do not own this allocation");
            }
        }

        return toResponse(doc);
    }

    @Override
    public NGOAllocationResponse getNgoAllocationByDonationId(String email, String donationId) {
        UserDocument user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        if (user.getRole() != Role.NGO && user.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only NGOs and Admins can view individual NGO allocation slices");
        }

        DonationAllocationDocument doc = donationAllocationRepository.findTopByDonationIdOrderByCreatedAtDesc(donationId)
                .orElseThrow(() -> new AllocationException("No allocations found for donation: " + donationId));

        String ngoId = null;
        if (user.getRole() == Role.NGO) {
            NgoProfileDocument ngo = ngoProfileRepository.findByUserIdAndIsDeletedFalse(user.getId())
                    .orElseThrow(() -> new NgoNotFoundException("NGO profile not found"));
            ngoId = ngo.getId();
        }

        return toNgoResponse(doc, ngoId);
    }

    @Override
    public Page<AllocationSummaryResponse> getAllocationHistory(String email, int page, int size) {
        UserDocument user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        Pageable pageable = PageRequest.of(page, size);
        Page<DonationAllocationDocument> documents;

        if (user.getRole() == Role.ADMIN) {
            documents = donationAllocationRepository.findAllByOrderByCreatedAtDesc(pageable);
        } else if (user.getRole() == Role.RESTAURANT) {
            RestaurantProfileDocument restaurant = restaurantProfileRepository.findByUserIdAndIsDeletedFalse(user.getId())
                    .orElseThrow(() -> new RestaurantNotFoundException("Restaurant profile not found"));
            documents = donationAllocationRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurant.getId(), pageable);
        } else if (user.getRole() == Role.NGO) {
            NgoProfileDocument ngo = ngoProfileRepository.findByUserIdAndIsDeletedFalse(user.getId())
                    .orElseThrow(() -> new NgoNotFoundException("NGO profile not found"));
            documents = donationAllocationRepository.findByAllocationsNgoIdOrderByCreatedAtDesc(ngo.getId(), pageable);
        } else {
            throw new AccessDeniedException("Unauthorized role access");
        }

        return documents.map(this::toSummary);
    }

    // --- Private Inline Mappers ---

    private AllocationResponse toResponse(DonationAllocationDocument doc) {
        List<NGOAllocationResponse> mappedSplits = null;
        if (doc.getAllocations() != null) {
            mappedSplits = doc.getAllocations().stream()
                    .map(this::mapEmbeddedToNgoResponse)
                    .collect(Collectors.toList());
        }

        return AllocationResponse.builder()
                .id(doc.getId())
                .donationId(doc.getDonationId())
                .restaurantId(doc.getRestaurantId())
                .totalQuantity(doc.getTotalQuantity())
                .quantityUnit(doc.getQuantityUnit())
                .remainingQuantity(doc.getRemainingQuantity())
                .allocationStatus(doc.getAllocationStatus())
                .allocationAlgorithm(doc.getAllocationAlgorithm())
                .generatedAt(doc.getGeneratedAt())
                .allocations(mappedSplits)
                .createdAt(doc.getCreatedAt())
                .updatedAt(doc.getUpdatedAt())
                .build();
    }

    private NGOAllocationResponse toNgoResponse(DonationAllocationDocument doc, String ngoId) {
        if (doc.getAllocations() == null || doc.getAllocations().isEmpty()) {
            throw new AllocationException("No allocations present in this run");
        }

        String targetNgoId = ngoId;
        EmbeddedAllocation match = doc.getAllocations().stream()
                .filter(alloc -> targetNgoId == null || alloc.getNgoId().equals(targetNgoId))
                .findFirst()
                .orElseThrow(() -> new AccessDeniedException("You are not part of this donation allocation"));

        return mapEmbeddedToNgoResponse(match);
    }

    private NGOAllocationResponse mapEmbeddedToNgoResponse(EmbeddedAllocation alloc) {
        return NGOAllocationResponse.builder()
                .ngoId(alloc.getNgoId())
                .ngoName(alloc.getNgoName())
                .matchingScore(alloc.getMatchingScore())
                .allocationRank(alloc.getAllocationRank())
                .allocatedQuantity(alloc.getAllocatedQuantity())
                .allocatedUnit(alloc.getAllocatedUnit())
                .ngoCapacity(alloc.getNgoCapacity())
                .remainingCapacity(alloc.getRemainingCapacity())
                .estimatedPickupTime(alloc.getEstimatedPickupTime())
                .allocationReason(alloc.getAllocationReason())
                .build();
    }

    private AllocationSummaryResponse toSummary(DonationAllocationDocument doc) {
        int allocatedNgosCount = (doc.getAllocations() != null) ? doc.getAllocations().size() : 0;

        return AllocationSummaryResponse.builder()
                .id(doc.getId())
                .donationId(doc.getDonationId())
                .totalQuantity(doc.getTotalQuantity())
                .quantityUnit(doc.getQuantityUnit())
                .remainingQuantity(doc.getRemainingQuantity())
                .allocationStatus(doc.getAllocationStatus())
                .allocationAlgorithm(doc.getAllocationAlgorithm())
                .generatedAt(doc.getGeneratedAt())
                .allocatedNgosCount(allocatedNgosCount)
                .build();
    }
}
