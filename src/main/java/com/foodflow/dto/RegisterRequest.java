package com.foodflow.dto;

import com.foodflow.document.GeoCoordinates;
import com.foodflow.enums.Role;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @NotNull(message = "Role is required")
    private Role role;

    @Valid
    private RestaurantDetails restaurantDetails;

    @Valid
    private NgoDetails ngoDetails;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RestaurantDetails {
        @NotBlank(message = "Restaurant name is required")
        private String restaurantName;

        @NotBlank(message = "Restaurant address is required")
        private String address;

        @NotNull(message = "Coordinates are required")
        @Valid
        private GeoCoordinates coordinates;

        @NotBlank(message = "License number is required")
        private String licenseNumber;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NgoDetails {
        @NotBlank(message = "NGO name is required")
        private String ngoName;

        @NotBlank(message = "NGO address is required")
        private String address;

        @NotNull(message = "Coordinates are required")
        @Valid
        private GeoCoordinates coordinates;

        @NotNull(message = "NGO capacity is required")
        @Min(value = 1, message = "Capacity must be at least 1")
        private Integer capacity;

        @NotBlank(message = "NGO storage type is required")
        private String storageType;

        @NotBlank(message = "Registration number is required")
        private String registrationNumber;
    }
}
