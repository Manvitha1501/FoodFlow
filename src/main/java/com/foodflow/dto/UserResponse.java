package com.foodflow.dto;

import com.foodflow.enums.AccountStatus;
import com.foodflow.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private String id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private Role role;
    private AccountStatus accountStatus;
    private Instant createdAt;
    private Instant updatedAt;
    
    // Role-specific nested profile details (null if not applicable)
    private RestaurantProfileResponse restaurantProfile;
    private NgoProfileResponse ngoProfile;
}
