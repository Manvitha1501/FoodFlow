package com.foodflow.service.impl;

import com.foodflow.document.RestaurantProfileDocument;
import com.foodflow.document.UserDocument;
import com.foodflow.dto.RestaurantResponse;
import com.foodflow.dto.RestaurantUpdateRequest;
import com.foodflow.enums.AccountStatus;
import com.foodflow.enums.VerificationStatus;
import com.foodflow.exception.ProfileNotFoundException;
import com.foodflow.mapper.ProfileMapper;
import com.foodflow.repository.RestaurantProfileRepository;
import com.foodflow.repository.UserRepository;
import com.foodflow.service.RestaurantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RestaurantServiceImpl implements RestaurantService {

    private final UserRepository userRepository;
    private final RestaurantProfileRepository restaurantProfileRepository;
    private final ProfileMapper profileMapper;

    public RestaurantServiceImpl(
            UserRepository userRepository,
            RestaurantProfileRepository restaurantProfileRepository,
            ProfileMapper profileMapper) {
        this.userRepository = userRepository;
        this.restaurantProfileRepository = restaurantProfileRepository;
        this.profileMapper = profileMapper;
    }

    @Override
    public RestaurantResponse getProfile(String email) {
        UserDocument user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        RestaurantProfileDocument profile = restaurantProfileRepository.findByUserIdAndIsDeletedFalse(user.getId())
                .orElseThrow(() -> new ProfileNotFoundException("Restaurant profile not found"));

        return profileMapper.toResponse(profile);
    }

    @Override
    public RestaurantResponse updateProfile(String email, RestaurantUpdateRequest request) {
        UserDocument user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        RestaurantProfileDocument profile = restaurantProfileRepository.findByUserIdAndIsDeletedFalse(user.getId())
                .orElseThrow(() -> new ProfileNotFoundException("Restaurant profile not found"));

        profileMapper.updateDocument(profile, request);
        RestaurantProfileDocument saved = restaurantProfileRepository.save(profile);
        return profileMapper.toResponse(saved);
    }

    @Override
    public void deleteProfile(String email) {
        UserDocument user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        RestaurantProfileDocument profile = restaurantProfileRepository.findByUserIdAndIsDeletedFalse(user.getId())
                .orElseThrow(() -> new ProfileNotFoundException("Restaurant profile not found"));

        profile.setDeleted(true);
        restaurantProfileRepository.save(profile);
    }

    @Override
    public Page<RestaurantResponse> search(String name, String city, int page, int size, String sortBy, String direction) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<RestaurantProfileDocument> docs;

        boolean hasName = name != null && !name.isBlank();
        boolean hasCity = city != null && !city.isBlank();

        if (hasName && hasCity) {
            docs = restaurantProfileRepository
                    .findByRestaurantNameContainingIgnoreCaseAndCityIgnoreCaseAndIsDeletedFalse(name.trim(), city.trim(), pageable);
        } else if (hasName) {
            docs = restaurantProfileRepository
                    .findByRestaurantNameContainingIgnoreCaseAndIsDeletedFalse(name.trim(), pageable);
        } else if (hasCity) {
            docs = restaurantProfileRepository
                    .findByCityIgnoreCaseAndIsDeletedFalse(city.trim(), pageable);
        } else {
            docs = restaurantProfileRepository.findByIsDeletedFalse(pageable);
        }

        return docs.map(profileMapper::toResponse);
    }

    @Override
    public Page<RestaurantResponse> getAllRestaurants(int page, int size, String sortBy, String direction) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<RestaurantProfileDocument> docs = restaurantProfileRepository.findAll(pageable);
        return docs.map(profileMapper::toResponse);
    }

    @Override
    public RestaurantResponse verifyRestaurant(String id) {
        RestaurantProfileDocument profile = restaurantProfileRepository.findById(id)
                .orElseThrow(() -> new ProfileNotFoundException("Restaurant profile not found"));

        profile.setVerificationStatus(VerificationStatus.APPROVED);
        RestaurantProfileDocument saved = restaurantProfileRepository.save(profile);
        return profileMapper.toResponse(saved);
    }

    @Override
    public RestaurantResponse deactivateRestaurant(String id) {
        RestaurantProfileDocument profile = restaurantProfileRepository.findById(id)
                .orElseThrow(() -> new ProfileNotFoundException("Restaurant profile not found"));

        profile.setDeleted(true);
        RestaurantProfileDocument savedProfile = restaurantProfileRepository.save(profile);

        try {
            UserDocument user = userRepository.findById(profile.getUserId())
                    .orElseThrow(() -> new UsernameNotFoundException("Associated user not found"));
            user.setAccountStatus(AccountStatus.SUSPENDED);
            userRepository.save(user);
        } catch (Exception e) {
            log.error("CRITICAL: Failed to suspend user account after deactivating restaurant profile. User ID: {}", profile.getUserId(), e);
        }

        return profileMapper.toResponse(savedProfile);
    }
}
