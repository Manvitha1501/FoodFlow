package com.foodflow.mapper;

import com.foodflow.document.GeoCoordinates;
import com.foodflow.document.NgoProfileDocument;
import com.foodflow.document.RestaurantProfileDocument;
import com.foodflow.document.UserDocument;
import com.foodflow.dto.UserResponse;
import com.foodflow.enums.AccountStatus;
import com.foodflow.enums.Role;
import com.foodflow.enums.VerificationStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

public class UserMapperTest {

    private final UserMapper userMapper = new UserMapper();

    @Test
    public void testMapRestaurantUserSuccessfully() {
        // Arrange
        UserDocument userDoc = UserDocument.builder()
                .id("u-123")
                .fullName("John Doe")
                .email("restaurant@foodflow.com")
                .password("bcrypt_encrypted_password")
                .phoneNumber("1234567890")
                .role(Role.RESTAURANT)
                .accountStatus(AccountStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        RestaurantProfileDocument restaurantDoc = RestaurantProfileDocument.builder()
                .id("p-123")
                .userId("u-123")
                .restaurantName("Tasty Food Cafe")
                .address("123 Food Street")
                .coordinates(GeoCoordinates.builder().longitude(77.1025).latitude(28.7041).build())
                .licenseNumber("LIC12345")
                .trustScore(4.8)
                .verificationStatus(VerificationStatus.APPROVED)
                .build();

        // Act
        UserResponse response = userMapper.toUserResponse(userDoc, restaurantDoc, null);

        // Assert
        assertNotNull(response);
        assertEquals("u-123", response.getId());
        assertEquals("John Doe", response.getFullName());
        assertEquals("restaurant@foodflow.com", response.getEmail());
        assertEquals("1234567890", response.getPhoneNumber());
        assertEquals(Role.RESTAURANT, response.getRole());
        assertEquals(AccountStatus.ACTIVE, response.getAccountStatus());
        assertNotNull(response.getCreatedAt());
        assertNotNull(response.getUpdatedAt());

        // Assert Profile
        assertNotNull(response.getRestaurantProfile());
        assertEquals("Tasty Food Cafe", response.getRestaurantProfile().getRestaurantName());
        assertEquals("123 Food Street", response.getRestaurantProfile().getAddress());
        assertEquals(77.1025, response.getRestaurantProfile().getCoordinates().getLongitude());
        assertEquals(28.7041, response.getRestaurantProfile().getCoordinates().getLatitude());
        assertEquals("LIC12345", response.getRestaurantProfile().getLicenseNumber());
        assertEquals(4.8, response.getRestaurantProfile().getTrustScore());
        assertEquals(VerificationStatus.APPROVED, response.getRestaurantProfile().getVerificationStatus());

        // Ensure NGO profile is null
        assertNull(response.getNgoProfile());
    }

    @Test
    public void testMapNgoUserSuccessfully() {
        // Arrange
        UserDocument userDoc = UserDocument.builder()
                .id("u-456")
                .fullName("Jane NGO")
                .email("ngo@foodflow.org")
                .password("bcrypt_encrypted_password")
                .phoneNumber("9876543210")
                .role(Role.NGO)
                .accountStatus(AccountStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        NgoProfileDocument ngoDoc = NgoProfileDocument.builder()
                .id("p-456")
                .userId("u-456")
                .ngoName("Feed the Needy")
                .address("456 Care Lane")
                .coordinates(GeoCoordinates.builder().longitude(77.2090).latitude(28.6139).build())
                .capacity(500)
                .storageType("COLD")
                .trustScore(4.9)
                .verificationStatus(VerificationStatus.PENDING)
                .build();

        // Act
        UserResponse response = userMapper.toUserResponse(userDoc, null, ngoDoc);

        // Assert
        assertNotNull(response);
        assertEquals("u-456", response.getId());
        assertEquals(Role.NGO, response.getRole());

        // Assert Profile
        assertNotNull(response.getNgoProfile());
        assertEquals("Feed the Needy", response.getNgoProfile().getNgoName());
        assertEquals("456 Care Lane", response.getNgoProfile().getAddress());
        assertEquals(77.2090, response.getNgoProfile().getCoordinates().getLongitude());
        assertEquals(28.6139, response.getNgoProfile().getCoordinates().getLatitude());
        assertEquals(500, response.getNgoProfile().getCapacity());
        assertEquals("COLD", response.getNgoProfile().getStorageType());
        assertEquals(4.9, response.getNgoProfile().getTrustScore());
        assertEquals(VerificationStatus.PENDING, response.getNgoProfile().getVerificationStatus());

        // Ensure Restaurant profile is null
        assertNull(response.getRestaurantProfile());
    }

    @Test
    public void testNullInputReturnsNull() {
        assertNull(userMapper.toUserResponse(null, null, null));
    }
}
