package com.foodflow.document;

import com.foodflow.enums.DonationStatus;
import com.foodflow.enums.PackagingType;
import com.foodflow.enums.StorageCondition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "donations")
public class DonationDocument {

    @Id
    private String id;

    @Indexed
    private String restaurantId;

    @Indexed
    private String ngoId;

    private String foodName;

    private String foodCategory;

    private String foodDescription;

    private String foodType;

    private Double quantity;

    private String quantityUnit;

    private Instant preparedTime;

    private Instant expiryTime;

    private StorageCondition storageCondition;

    private PackagingType packagingType;

    private String foodTemperature;

    private String pickupAddress;

    @Indexed
    private String city;

    private String state;

    private String pincode;

    private GeoCoordinates coordinates;

    private List<String> foodImages;

    private String specialInstructions;

    @Indexed
    private DonationStatus status;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @Builder.Default
    private boolean isActive = true;
}
