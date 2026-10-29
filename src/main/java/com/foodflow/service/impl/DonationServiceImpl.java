package com.foodflow.service.impl;

import com.foodflow.document.DonationDocument;
import com.foodflow.document.RestaurantProfileDocument;
import com.foodflow.document.UserDocument;
import com.foodflow.dto.CreateDonationRequest;
import com.foodflow.dto.DonationResponse;
import com.foodflow.dto.DonationSummaryResponse;
import com.foodflow.dto.UpdateDonationRequest;
import com.foodflow.enums.DonationStatus;
import com.foodflow.enums.Role;
import com.foodflow.exception.DonationNotFoundException;
import com.foodflow.exception.DonationValidationException;
import com.foodflow.exception.InvalidDonationStateException;
import com.foodflow.exception.ProfileNotFoundException;
import com.foodflow.exception.UnauthorizedException;
import com.foodflow.repository.DonationRepository;
import com.foodflow.repository.RestaurantProfileRepository;
import com.foodflow.repository.UserRepository;
import com.foodflow.service.DonationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DonationServiceImpl implements DonationService {

    private final UserRepository userRepository;
    private final RestaurantProfileRepository restaurantProfileRepository;
    private final DonationRepository donationRepository;
    private final MongoTemplate mongoTemplate;

    public DonationServiceImpl(
            UserRepository userRepository,
            RestaurantProfileRepository restaurantProfileRepository,
            DonationRepository donationRepository,
            MongoTemplate mongoTemplate) {
        this.userRepository = userRepository;
        this.restaurantProfileRepository = restaurantProfileRepository;
        this.donationRepository = donationRepository;
        this.mongoTemplate = mongoTemplate;
    }

    private RestaurantProfileDocument getRestaurantProfileByEmail(String email) {
        UserDocument user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return restaurantProfileRepository.findByUserIdAndIsDeletedFalse(user.getId())
                .orElseThrow(() -> new ProfileNotFoundException("Restaurant profile not found"));
    }

    private DonationDocument getAndVerifyDonationOwnership(String id, String email) {
        RestaurantProfileDocument restaurantProfile = getRestaurantProfileByEmail(email);
        DonationDocument donation = donationRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new DonationNotFoundException("Donation not found"));
        if (!donation.getRestaurantId().equals(restaurantProfile.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("You do not own this donation");
        }
        return donation;
    }

    private DonationResponse toResponse(DonationDocument doc) {
        if (doc == null) {
            return null;
        }
        return DonationResponse.builder()
                .id(doc.getId())
                .restaurantId(doc.getRestaurantId())
                .foodName(doc.getFoodName())
                .foodCategory(doc.getFoodCategory())
                .foodDescription(doc.getFoodDescription())
                .foodType(doc.getFoodType())
                .quantity(doc.getQuantity())
                .quantityUnit(doc.getQuantityUnit())
                .preparedTime(doc.getPreparedTime())
                .expiryTime(doc.getExpiryTime())
                .storageCondition(doc.getStorageCondition())
                .packagingType(doc.getPackagingType())
                .foodTemperature(doc.getFoodTemperature())
                .pickupAddress(doc.getPickupAddress())
                .city(doc.getCity())
                .state(doc.getState())
                .pincode(doc.getPincode())
                .coordinates(doc.getCoordinates())
                .foodImages(doc.getFoodImages())
                .specialInstructions(doc.getSpecialInstructions())
                .status(doc.getStatus())
                .createdAt(doc.getCreatedAt())
                .updatedAt(doc.getUpdatedAt())
                .isActive(doc.isActive())
                .build();
    }

    private DonationSummaryResponse toSummaryResponse(DonationDocument doc) {
        if (doc == null) {
            return null;
        }
        return DonationSummaryResponse.builder()
                .id(doc.getId())
                .restaurantId(doc.getRestaurantId())
                .foodName(doc.getFoodName())
                .quantity(doc.getQuantity())
                .quantityUnit(doc.getQuantityUnit())
                .expiryTime(doc.getExpiryTime())
                .status(doc.getStatus())
                .foodCategory(doc.getFoodCategory())
                .foodImages(doc.getFoodImages())
                .city(doc.getCity())
                .build();
    }

    @Override
    public DonationResponse createDonation(String email, CreateDonationRequest request) {
        RestaurantProfileDocument restaurantProfile = getRestaurantProfileByEmail(email);

        DonationDocument donation = DonationDocument.builder()
                .restaurantId(restaurantProfile.getId())
                .foodName(request.getFoodName())
                .foodCategory(request.getFoodCategory())
                .foodDescription(request.getFoodDescription())
                .foodType(request.getFoodType())
                .quantity(request.getQuantity())
                .quantityUnit(request.getQuantityUnit())
                .preparedTime(request.getPreparedTime())
                .expiryTime(request.getExpiryTime())
                .storageCondition(request.getStorageCondition())
                .packagingType(request.getPackagingType())
                .foodTemperature(request.getFoodTemperature())
                .foodImages(request.getFoodImages())
                .specialInstructions(request.getSpecialInstructions())
                .status(DonationStatus.DRAFT)
                .isActive(true)
                .build();

        // Address fallback logic
        donation.setPickupAddress(request.getPickupAddress() != null && !request.getPickupAddress().isBlank()
                ? request.getPickupAddress() : restaurantProfile.getAddress());
        donation.setCity(request.getCity() != null && !request.getCity().isBlank()
                ? request.getCity() : restaurantProfile.getCity());
        donation.setState(request.getState() != null && !request.getState().isBlank()
                ? request.getState() : restaurantProfile.getState());
        donation.setPincode(request.getPincode() != null && !request.getPincode().isBlank()
                ? request.getPincode() : restaurantProfile.getPincode());
        donation.setCoordinates(request.getCoordinates() != null
                ? request.getCoordinates() : restaurantProfile.getCoordinates());

        DonationDocument saved = donationRepository.save(donation);
        return toResponse(saved);
    }

    @Override
    public DonationResponse updateDonation(String email, String id, UpdateDonationRequest request) {
        DonationDocument donation = getAndVerifyDonationOwnership(id, email);

        if (donation.getStatus() != DonationStatus.DRAFT) {
            throw new InvalidDonationStateException("Only draft donations can be updated");
        }

        if (request.getFoodName() != null && !request.getFoodName().isBlank()) {
            donation.setFoodName(request.getFoodName());
        }
        if (request.getFoodCategory() != null && !request.getFoodCategory().isBlank()) {
            donation.setFoodCategory(request.getFoodCategory());
        }
        if (request.getFoodDescription() != null) {
            donation.setFoodDescription(request.getFoodDescription());
        }
        if (request.getFoodType() != null) {
            donation.setFoodType(request.getFoodType());
        }
        if (request.getQuantity() != null) {
            donation.setQuantity(request.getQuantity());
        }
        if (request.getQuantityUnit() != null) {
            donation.setQuantityUnit(request.getQuantityUnit());
        }
        if (request.getPreparedTime() != null) {
            donation.setPreparedTime(request.getPreparedTime());
        }
        if (request.getExpiryTime() != null) {
            donation.setExpiryTime(request.getExpiryTime());
        }
        if (request.getStorageCondition() != null) {
            donation.setStorageCondition(request.getStorageCondition());
        }
        if (request.getPackagingType() != null) {
            donation.setPackagingType(request.getPackagingType());
        }
        if (request.getFoodTemperature() != null) {
            donation.setFoodTemperature(request.getFoodTemperature());
        }
        if (request.getPickupAddress() != null && !request.getPickupAddress().isBlank()) {
            donation.setPickupAddress(request.getPickupAddress());
        }
        if (request.getCity() != null && !request.getCity().isBlank()) {
            donation.setCity(request.getCity());
        }
        if (request.getState() != null && !request.getState().isBlank()) {
            donation.setState(request.getState());
        }
        if (request.getPincode() != null && !request.getPincode().isBlank()) {
            donation.setPincode(request.getPincode());
        }
        if (request.getCoordinates() != null) {
            donation.setCoordinates(request.getCoordinates());
        }
        if (request.getFoodImages() != null) {
            donation.setFoodImages(request.getFoodImages());
        }
        if (request.getSpecialInstructions() != null) {
            donation.setSpecialInstructions(request.getSpecialInstructions());
        }

        DonationDocument saved = donationRepository.save(donation);
        return toResponse(saved);
    }

    @Override
    public DonationResponse publishDonation(String email, String id) {
        DonationDocument donation = getAndVerifyDonationOwnership(id, email);

        if (donation.getStatus() != DonationStatus.DRAFT) {
            throw new InvalidDonationStateException("Only draft donations can be published");
        }

        // Publish validation checks
        if (donation.getFoodName() == null || donation.getFoodName().isBlank()) {
            throw new DonationValidationException("Food name is required to publish");
        }
        if (donation.getFoodCategory() == null || donation.getFoodCategory().isBlank()) {
            throw new DonationValidationException("Category is required to publish");
        }
        if (donation.getQuantity() == null || donation.getQuantity() <= 0) {
            throw new DonationValidationException("Quantity must be greater than 0");
        }
        if (donation.getQuantityUnit() == null || donation.getQuantityUnit().isBlank()) {
            throw new DonationValidationException("Quantity unit is required to publish");
        }
        if (donation.getPreparedTime() == null) {
            throw new DonationValidationException("Prepared time is required to publish");
        }
        if (donation.getExpiryTime() == null) {
            throw new DonationValidationException("Expiry time is required to publish");
        }
        if (donation.getExpiryTime().isBefore(donation.getPreparedTime())) {
            throw new DonationValidationException("Expiry time must be after prepared time");
        }
        if (donation.getPickupAddress() == null || donation.getPickupAddress().isBlank()) {
            throw new DonationValidationException("Pickup address is required to publish");
        }
        if (donation.getCity() == null || donation.getCity().isBlank()) {
            throw new DonationValidationException("City is required to publish");
        }
        if (donation.getState() == null || donation.getState().isBlank()) {
            throw new DonationValidationException("State is required to publish");
        }
        if (donation.getPincode() == null || donation.getPincode().isBlank()) {
            throw new DonationValidationException("Pincode is required to publish");
        }
        if (donation.getCoordinates() == null || donation.getCoordinates().getLatitude() == null || donation.getCoordinates().getLongitude() == null) {
            throw new DonationValidationException("Coordinates are required to publish");
        }
        if (donation.getStorageCondition() == null) {
            throw new DonationValidationException("Storage condition is required to publish");
        }
        if (donation.getPackagingType() == null) {
            throw new DonationValidationException("Packaging type is required to publish");
        }
        if (donation.getFoodTemperature() == null || donation.getFoodTemperature().isBlank()) {
            throw new DonationValidationException("Food temperature is required to publish");
        }
        if (donation.getFoodImages() == null || donation.getFoodImages().isEmpty()) {
            throw new DonationValidationException("At least one food image is required to publish");
        }

        donation.setStatus(DonationStatus.PUBLISHED);
        DonationDocument saved = donationRepository.save(donation);
        return toResponse(saved);
    }

    @Override
    public void deleteDonation(String email, String id) {
        DonationDocument donation = getAndVerifyDonationOwnership(id, email);

        donation.setActive(false);
        if (donation.getStatus() != DonationStatus.COMPLETED) {
            donation.setStatus(DonationStatus.CANCELLED);
        }

        donationRepository.save(donation);
    }

    @Override
    public DonationResponse getDonationById(String email, String id) {
        UserDocument user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (user.getRole() == Role.ADMIN) {
            DonationDocument donation = donationRepository.findById(id)
                    .orElseThrow(() -> new DonationNotFoundException("Donation not found"));
            return toResponse(donation);
        } else if (user.getRole() == Role.RESTAURANT) {
            RestaurantProfileDocument restaurantProfile = restaurantProfileRepository.findByUserIdAndIsDeletedFalse(user.getId())
                    .orElseThrow(() -> new ProfileNotFoundException("Restaurant profile not found"));
            DonationDocument donation = donationRepository.findByIdAndIsActiveTrue(id)
                    .orElseThrow(() -> new DonationNotFoundException("Donation not found"));
            if (!donation.getRestaurantId().equals(restaurantProfile.getId())) {
                throw new UnauthorizedException("You do not own this donation");
            }
            return toResponse(donation);
        }

        throw new UnauthorizedException("Access denied");
    }

    @Override
    public Page<DonationResponse> getMyDonations(String email, int page, int size, String sortBy, String direction) {
        RestaurantProfileDocument restaurantProfile = getRestaurantProfileByEmail(email);
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<DonationDocument> docs = donationRepository.findByRestaurantIdAndIsActiveTrue(restaurantProfile.getId(), pageable);
        return docs.map(this::toResponse);
    }

    @Override
    public Page<DonationResponse> getAllDonationsForAdmin(int page, int size, String sortBy, String direction) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<DonationDocument> docs = donationRepository.findAll(pageable);
        return docs.map(this::toResponse);
    }

    @Override
    public Page<DonationSummaryResponse> search(String name, String category, DonationStatus status, String city, Instant startDate, Instant endDate, int page, int size, String sortBy, String direction) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Query query = new Query().with(pageable);
        query.addCriteria(Criteria.where("isActive").is(true));

        if (name != null && !name.isBlank()) {
            query.addCriteria(Criteria.where("foodName").regex(name.trim(), "i"));
        }
        if (category != null && !category.isBlank()) {
            query.addCriteria(Criteria.where("foodCategory").regex("^" + category.trim() + "$", "i"));
        }
        if (status != null) {
            query.addCriteria(Criteria.where("status").is(status));
        }
        if (city != null && !city.isBlank()) {
            query.addCriteria(Criteria.where("city").regex("^" + city.trim() + "$", "i"));
        }

        if (startDate != null && endDate != null) {
            query.addCriteria(Criteria.where("createdAt").gte(startDate).lte(endDate));
        } else if (startDate != null) {
            query.addCriteria(Criteria.where("createdAt").gte(startDate));
        } else if (endDate != null) {
            query.addCriteria(Criteria.where("createdAt").lte(endDate));
        }

        List<DonationDocument> list = mongoTemplate.find(query, DonationDocument.class);
        long total = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), DonationDocument.class);

        List<DonationSummaryResponse> summaries = list.stream().map(this::toSummaryResponse).collect(Collectors.toList());
        return new PageImpl<>(summaries, pageable, total);
    }

    @Override
    public Page<DonationSummaryResponse> filter(String filterType, int page, int size, String sortBy, String direction) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Query query = new Query().with(pageable);
        query.addCriteria(Criteria.where("isActive").is(true));

        switch (filterType.toUpperCase()) {
            case "TODAY":
                Instant startOfToday = Instant.now().minusSeconds(86400); // Past 24 hours
                query.addCriteria(Criteria.where("createdAt").gte(startOfToday));
                break;
            case "THIS_WEEK":
                Instant startOfWeek = Instant.now().minusSeconds(86400 * 7); // Past 7 days
                query.addCriteria(Criteria.where("createdAt").gte(startOfWeek));
                break;
            case "COMPLETED":
                query.addCriteria(Criteria.where("status").is(DonationStatus.COMPLETED));
                break;
            case "PENDING":
                query.addCriteria(Criteria.where("status").in(Arrays.asList(
                        DonationStatus.DRAFT,
                        DonationStatus.PUBLISHED,
                        DonationStatus.RESERVED,
                        DonationStatus.PICKED_UP
                )));
                break;
            case "PUBLISHED":
                query.addCriteria(Criteria.where("status").is(DonationStatus.PUBLISHED));
                break;
            case "CANCELLED":
                query.addCriteria(Criteria.where("status").is(DonationStatus.CANCELLED));
                break;
            default:
                throw new IllegalArgumentException("Unknown filter type: " + filterType);
        }

        List<DonationDocument> list = mongoTemplate.find(query, DonationDocument.class);
        long total = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), DonationDocument.class);

        List<DonationSummaryResponse> summaries = list.stream().map(this::toSummaryResponse).collect(Collectors.toList());
        return new PageImpl<>(summaries, pageable, total);
    }

    @Override
    public DonationResponse assignNgo(String id, String ngoId) {
        DonationDocument donation = donationRepository.findById(id)
                .orElseThrow(() -> new DonationNotFoundException("Donation not found"));
        donation.setNgoId(ngoId);
        donation.setStatus(DonationStatus.RESERVED);
        DonationDocument saved = donationRepository.save(donation);
        return toResponse(saved);
    }
}
