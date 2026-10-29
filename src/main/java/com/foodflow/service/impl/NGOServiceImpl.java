package com.foodflow.service.impl;

import com.foodflow.document.NgoProfileDocument;
import com.foodflow.document.UserDocument;
import com.foodflow.dto.NGOResponse;
import com.foodflow.dto.NGOUpdateRequest;
import com.foodflow.enums.AccountStatus;
import com.foodflow.enums.VerificationStatus;
import com.foodflow.exception.ProfileNotFoundException;
import com.foodflow.mapper.ProfileMapper;
import com.foodflow.repository.NgoProfileRepository;
import com.foodflow.repository.UserRepository;
import com.foodflow.service.NGOService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NGOServiceImpl implements NGOService {

    private final UserRepository userRepository;
    private final NgoProfileRepository ngoProfileRepository;
    private final ProfileMapper profileMapper;

    public NGOServiceImpl(
            UserRepository userRepository,
            NgoProfileRepository ngoProfileRepository,
            ProfileMapper profileMapper) {
        this.userRepository = userRepository;
        this.ngoProfileRepository = ngoProfileRepository;
        this.profileMapper = profileMapper;
    }

    @Override
    public NGOResponse getProfile(String email) {
        UserDocument user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        NgoProfileDocument profile = ngoProfileRepository.findByUserIdAndIsDeletedFalse(user.getId())
                .orElseThrow(() -> new ProfileNotFoundException("NGO profile not found"));

        return profileMapper.toResponse(profile);
    }

    @Override
    public NGOResponse updateProfile(String email, NGOUpdateRequest request) {
        UserDocument user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        NgoProfileDocument profile = ngoProfileRepository.findByUserIdAndIsDeletedFalse(user.getId())
                .orElseThrow(() -> new ProfileNotFoundException("NGO profile not found"));

        profileMapper.updateDocument(profile, request);
        NgoProfileDocument saved = ngoProfileRepository.save(profile);
        return profileMapper.toResponse(saved);
    }

    @Override
    public void deleteProfile(String email) {
        UserDocument user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        NgoProfileDocument profile = ngoProfileRepository.findByUserIdAndIsDeletedFalse(user.getId())
                .orElseThrow(() -> new ProfileNotFoundException("NGO profile not found"));

        profile.setDeleted(true);
        ngoProfileRepository.save(profile);
    }

    @Override
    public Page<NGOResponse> search(String name, String city, int page, int size, String sortBy, String direction) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<NgoProfileDocument> docs;

        boolean hasName = name != null && !name.isBlank();
        boolean hasCity = city != null && !city.isBlank();

        if (hasName && hasCity) {
            docs = ngoProfileRepository
                    .findByNgoNameContainingIgnoreCaseAndCityIgnoreCaseAndIsDeletedFalse(name.trim(), city.trim(), pageable);
        } else if (hasName) {
            docs = ngoProfileRepository
                    .findByNgoNameContainingIgnoreCaseAndIsDeletedFalse(name.trim(), pageable);
        } else if (hasCity) {
            docs = ngoProfileRepository
                    .findByCityIgnoreCaseAndIsDeletedFalse(city.trim(), pageable);
        } else {
            docs = ngoProfileRepository.findByIsDeletedFalse(pageable);
        }

        return docs.map(profileMapper::toResponse);
    }

    @Override
    public Page<NGOResponse> getAllNgos(int page, int size, String sortBy, String direction) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<NgoProfileDocument> docs = ngoProfileRepository.findAll(pageable);
        return docs.map(profileMapper::toResponse);
    }

    @Override
    public NGOResponse verifyNgo(String id) {
        NgoProfileDocument profile = ngoProfileRepository.findById(id)
                .orElseThrow(() -> new ProfileNotFoundException("NGO profile not found"));

        profile.setVerificationStatus(VerificationStatus.APPROVED);
        NgoProfileDocument saved = ngoProfileRepository.save(profile);
        return profileMapper.toResponse(saved);
    }

    @Override
    public NGOResponse deactivateNgo(String id) {
        NgoProfileDocument profile = ngoProfileRepository.findById(id)
                .orElseThrow(() -> new ProfileNotFoundException("NGO profile not found"));

        profile.setDeleted(true);
        NgoProfileDocument savedProfile = ngoProfileRepository.save(profile);

        try {
            UserDocument user = userRepository.findById(profile.getUserId())
                    .orElseThrow(() -> new UsernameNotFoundException("Associated user not found"));
            user.setAccountStatus(AccountStatus.SUSPENDED);
            userRepository.save(user);
        } catch (Exception e) {
            log.error("CRITICAL: Failed to suspend user account after deactivating NGO profile. User ID: {}", profile.getUserId(), e);
        }

        return profileMapper.toResponse(savedProfile);
    }
}
