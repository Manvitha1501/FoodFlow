package com.foodflow.service;

import com.foodflow.document.GeoCoordinates;
import com.foodflow.document.NgoProfileDocument;
import com.foodflow.document.RestaurantProfileDocument;
import com.foodflow.document.UserDocument;
import com.foodflow.dto.JwtResponse;
import com.foodflow.dto.LoginRequest;
import com.foodflow.dto.RegisterRequest;
import com.foodflow.dto.UserResponse;
import com.foodflow.enums.AccountStatus;
import com.foodflow.enums.Role;
import com.foodflow.enums.VerificationStatus;
import com.foodflow.exception.DuplicateEmailException;
import com.foodflow.exception.DuplicateLicenseNumberException;
import com.foodflow.exception.DuplicateRegistrationNumberException;
import com.foodflow.exception.ProfileNotFoundException;
import com.foodflow.mapper.UserMapper;
import com.foodflow.repository.NgoProfileRepository;
import com.foodflow.repository.RestaurantProfileRepository;
import com.foodflow.repository.UserRepository;
import com.foodflow.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AuthServiceImplTest {

    private UserRepository userRepository;
    private RestaurantProfileRepository restaurantProfileRepository;
    private NgoProfileRepository ngoProfileRepository;
    private PasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;
    private JwtService jwtService;
    private UserMapper userMapper;

    private AuthServiceImpl authService;

    @BeforeEach
    public void setUp() {
        userRepository = mock(UserRepository.class);
        restaurantProfileRepository = mock(RestaurantProfileRepository.class);
        ngoProfileRepository = mock(NgoProfileRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        authenticationManager = mock(AuthenticationManager.class);
        jwtService = mock(JwtService.class);
        userMapper = mock(UserMapper.class);

        authService = new AuthServiceImpl(
                userRepository,
                restaurantProfileRepository,
                ngoProfileRepository,
                passwordEncoder,
                authenticationManager,
                jwtService,
                userMapper
        );
    }

    @Test
    public void testRegisterNgoSuccessfully() {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .email("ngo@foodflow.org")
                .password("password123")
                .fullName("Feed NGO")
                .phoneNumber("1234567890")
                .role(Role.NGO)
                .ngoDetails(RegisterRequest.NgoDetails.builder()
                        .ngoName("Feed the Needy")
                        .address("123 Care Street")
                        .coordinates(GeoCoordinates.builder().longitude(77.0).latitude(28.0).build())
                        .capacity(300)
                        .storageType("COLD")
                        .registrationNumber("REG123")
                        .build())
                .build();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");

        UserDocument savedUser = UserDocument.builder()
                .id("user-123")
                .email(request.getEmail())
                .fullName(request.getFullName())
                .password("encodedPassword")
                .phoneNumber(request.getPhoneNumber())
                .role(Role.NGO)
                .accountStatus(AccountStatus.ACTIVE)
                .build();
        when(userRepository.save(any(UserDocument.class))).thenReturn(savedUser);

        NgoProfileDocument savedProfile = NgoProfileDocument.builder()
                .id("profile-123")
                .userId("user-123")
                .ngoName("Feed the Needy")
                .verificationStatus(VerificationStatus.PENDING)
                .build();
        when(ngoProfileRepository.save(any(NgoProfileDocument.class))).thenReturn(savedProfile);

        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("mockToken");
        when(jwtService.getExpirationTime()).thenReturn(3600000L);
        when(userMapper.toUserResponse(any(), any(), any())).thenReturn(UserResponse.builder().id("user-123").role(Role.NGO).build());

        // Act
        JwtResponse response = authService.register(request);

        // Assert
        assertNotNull(response);
        assertEquals("mockToken", response.getToken());
        assertEquals(Role.NGO, response.getRole());

        // Verify User was saved with ACTIVE status
        ArgumentCaptor<UserDocument> userCaptor = ArgumentCaptor.forClass(UserDocument.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals(AccountStatus.ACTIVE, userCaptor.getValue().getAccountStatus());

        // Verify Profile was saved with PENDING status
        ArgumentCaptor<NgoProfileDocument> profileCaptor = ArgumentCaptor.forClass(NgoProfileDocument.class);
        verify(ngoProfileRepository).save(profileCaptor.capture());
        assertEquals(VerificationStatus.PENDING, profileCaptor.getValue().getVerificationStatus());
    }

    @Test
    public void testRegisterThrowsDuplicateEmailException() {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .email("duplicate@foodflow.com")
                .build();
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateEmailException.class, () -> authService.register(request));
    }

    @Test
    public void testRegisterProfileFailureTriggersUserRollback() {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .email("ngo@foodflow.org")
                .password("password123")
                .fullName("Feed NGO")
                .phoneNumber("1234567890")
                .role(Role.NGO)
                .ngoDetails(RegisterRequest.NgoDetails.builder()
                        .ngoName("Feed the Needy")
                        .address("123 Care Street")
                        .coordinates(GeoCoordinates.builder().longitude(77.0).latitude(28.0).build())
                        .capacity(300)
                        .storageType("COLD")
                        .registrationNumber("REG123")
                        .build())
                .build();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");

        UserDocument savedUser = UserDocument.builder().id("user-123").build();
        when(userRepository.save(any(UserDocument.class))).thenReturn(savedUser);

        // Simulate db save failure on Profile
        when(ngoProfileRepository.save(any(NgoProfileDocument.class))).thenThrow(new RuntimeException("DB error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> authService.register(request));

        // Verify User delete was triggered
        verify(userRepository).delete(savedUser);
    }

    @Test
    public void testRegisterRestaurantThrowsDuplicateLicenseNumberException() {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .email("restaurant@foodflow.com")
                .password("password123")
                .fullName("John Doe")
                .phoneNumber("1234567890")
                .role(Role.RESTAURANT)
                .restaurantDetails(RegisterRequest.RestaurantDetails.builder()
                        .restaurantName("Tasty Bistro")
                        .address("123 Street")
                        .coordinates(GeoCoordinates.builder().longitude(77.0).latitude(28.0).build())
                        .licenseNumber("LIC-DUPLICATE")
                        .build())
                .build();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(restaurantProfileRepository.existsByLicenseNumber("LIC-DUPLICATE")).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateLicenseNumberException.class, () -> authService.register(request));
    }

    @Test
    public void testRegisterNgoThrowsDuplicateRegistrationNumberException() {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .email("ngo@foodflow.org")
                .password("password123")
                .fullName("Jane NGO")
                .phoneNumber("9876543210")
                .role(Role.NGO)
                .ngoDetails(RegisterRequest.NgoDetails.builder()
                        .ngoName("Feed NGO")
                        .address("456 Avenue")
                        .coordinates(GeoCoordinates.builder().longitude(77.0).latitude(28.0).build())
                        .capacity(100)
                        .storageType("DRY")
                        .registrationNumber("REG-DUPLICATE")
                        .build())
                .build();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(ngoProfileRepository.existsByRegistrationNumber("REG-DUPLICATE")).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateRegistrationNumberException.class, () -> authService.register(request));
    }

    @Test
    public void testLoginThrowsProfileNotFoundException() {
        // Arrange
        LoginRequest request = new LoginRequest("restaurant@foodflow.com", "password");
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(new User("restaurant@foodflow.com", "password", List.of()));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);

        UserDocument user = UserDocument.builder().id("user-123").email("restaurant@foodflow.com").role(Role.RESTAURANT).build();
        when(userRepository.findByEmail("restaurant@foodflow.com")).thenReturn(Optional.of(user));

        // Profile missing in DB
        when(restaurantProfileRepository.findByUserId("user-123")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ProfileNotFoundException.class, () -> authService.login(request));
    }

    @Test
    public void testAdminLoginMismatchedRoleThrowsGenericException() {
        // Arrange
        LoginRequest request = new LoginRequest("notadmin@foodflow.com", "password");
        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);

        // User role is RESTAURANT, not ADMIN
        UserDocument user = UserDocument.builder().id("user-123").email("notadmin@foodflow.com").role(Role.RESTAURANT).build();
        when(userRepository.findByEmail("notadmin@foodflow.com")).thenReturn(Optional.of(user));

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> authService.adminLogin(request));
    }
}
