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
@Document(collection = "restaurant_profiles")
public class RestaurantProfileDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private String userId;

    private String restaurantName;

    private String address;

    private GeoCoordinates coordinates;

    @Indexed(unique = true)
    private String licenseNumber;

    private String ownerName;

    private String email;

    private String phoneNumber;

    private String restaurantType;

    private List<String> foodCategories;

    @Indexed
    private String city;

    private String state;

    private String pincode;

    private String openingTime;

    private String closingTime;

    private String profileImage;

    private String description;

    @Builder.Default
    private boolean isDeleted = false;

    @Builder.Default
    private Double trustScore = 5.0;

    @Builder.Default
    private Integer totalMealsDonated = 0;

    @Builder.Default
    private Double totalCO2Saved = 0.0;

    @Builder.Default
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
