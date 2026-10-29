package com.foodflow.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.foodflow.enums.VerificationStatus;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "ngo_profiles")
public class NgoProfileDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private String userId;

    private String ngoName;

    private String address;

    private GeoCoordinates coordinates;

    private Integer capacity;

    private String storageType; // COLD, DRY, BOTH

    @Indexed(unique = true)
    private String registrationNumber;

    private String contactPerson;

    private String email;

    private String phoneNumber;

    @Indexed
    private String city;

    private String state;

    private String pincode;

    private boolean vehicleAvailable;

    private String operatingHours;

    private List<String> acceptedFoodTypes;

    private String profileImage;

    private String description;

    @Builder.Default
    private boolean isDeleted = false;

    @Builder.Default
    private Double trustScore = 5.0;

    @Builder.Default
    private Integer beneficiariesServed = 0;

    @Builder.Default
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
