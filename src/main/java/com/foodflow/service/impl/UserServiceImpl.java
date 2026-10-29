package com.foodflow.service.impl;

import com.foodflow.document.NgoProfileDocument;
import com.foodflow.document.RestaurantProfileDocument;
import com.foodflow.document.UserDocument;
import com.foodflow.dto.ChangePasswordRequest;
import com.foodflow.dto.UpdateProfileRequest;
import com.foodflow.dto.UserResponse;
import com.foodflow.enums.Role;
import com.foodflow.exception.InvalidPasswordException;
import com.foodflow.exception.ProfileNotFoundException;
import com.foodflow.mapper.UserMapper;
import com.foodflow.repository.NgoProfileRepository;
import com.foodflow.repository.RestaurantProfileRepository;
import com.foodflow.repository.UserRepository;
import com.foodflow.service.UserService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RestaurantProfileRepository restaurantProfileRepository;
    private final NgoProfileRepository ngoProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserServiceImpl(
            UserRepository userRepository,
            RestaurantProfileRepository restaurantProfileRepository,
            NgoProfileRepository ngoProfileRepository,
            PasswordEncoder passwordEncoder,
            UserMapper userMapper) {
        this.userRepository = userRepository;
        this.restaurantProfileRepository = restaurantProfileRepository;
        this.ngoProfileRepository = ngoProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    @Override
    public UserResponse getCurrentUserProfile(String email) {
        UserDocument user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        RestaurantProfileDocument restaurantProfile = null;
        NgoProfileDocument ngoProfile = null;

        if (user.getRole() == Role.RESTAURANT) {
            restaurantProfile = restaurantProfileRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new ProfileNotFoundException("Profile data missing for user"));
        } else if (user.getRole() == Role.NGO) {
            ngoProfile = ngoProfileRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new ProfileNotFoundException("Profile data missing for user"));
        }

        return userMapper.toUserResponse(user, restaurantProfile, ngoProfile);
    }

    @Override
    public UserResponse updateProfile(String email, UpdateProfileRequest request) {
        UserDocument user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        userRepository.save(user);

        RestaurantProfileDocument restaurantProfile = null;
        NgoProfileDocument ngoProfile = null;

        if (user.getRole() == Role.RESTAURANT) {
            restaurantProfile = restaurantProfileRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new ProfileNotFoundException("Profile data missing for user"));
        } else if (user.getRole() == Role.NGO) {
            ngoProfile = ngoProfileRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new ProfileNotFoundException("Profile data missing for user"));
        }

        return userMapper.toUserResponse(user, restaurantProfile, ngoProfile);
    }

    @Override
    public void changePassword(String email, ChangePasswordRequest request) {
        UserDocument user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new InvalidPasswordException("Current password does not match");
        }

        if (request.getNewPassword().equals(request.getOldPassword())) {
            throw new IllegalArgumentException("New password cannot be the same as old password");
        }

        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new IllegalArgumentException("New password and confirmation do not match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}
