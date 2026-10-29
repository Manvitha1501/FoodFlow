package com.foodflow.dto;

import com.foodflow.document.GeoCoordinates;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NGOUpdateRequest {

    @NotBlank(message = "NGO name is required")
    private String ngoName;

    @NotBlank(message = "Contact person is required")
    private String contactPerson;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "Pincode is required")
    private String pincode;

    @NotNull(message = "Coordinates are required")
    @Valid
    private GeoCoordinates coordinates;

    @NotNull(message = "Storage capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;

    @NotBlank(message = "Storage type is required")
    private String storageType;

    private boolean vehicleAvailable;

    @NotBlank(message = "Operating hours are required")
    private String operatingHours;

    @NotEmpty(message = "Accepted food types cannot be empty")
    private List<String> acceptedFoodTypes;

    private String profileImage;

    private String description;
}
