package com.foodflow.dto;

import com.foodflow.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtResponse {

    private String token;

    @Builder.Default
    private String tokenType = "Bearer";

    // Token duration in milliseconds from now (e.g. 86400000 ms = 24 hours)
    private Long expiresIn;

    private Role role;

    private UserResponse user;
}
