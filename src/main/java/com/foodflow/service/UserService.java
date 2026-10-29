package com.foodflow.service;

import com.foodflow.dto.ChangePasswordRequest;
import com.foodflow.dto.UpdateProfileRequest;
import com.foodflow.dto.UserResponse;

public interface UserService {

    UserResponse getCurrentUserProfile(String email);

    UserResponse updateProfile(String email, UpdateProfileRequest request);

    void changePassword(String email, ChangePasswordRequest request);
}
