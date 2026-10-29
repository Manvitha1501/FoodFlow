package com.foodflow.service.impl;

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
import com.foodflow.service.AuthService;
import com.foodflow.service.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RestaurantProfileRepository restaurantProfileRepository;
    private final NgoProfileRepository ngoProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserMapper userMapper;

    public AuthServiceImpl(
            UserRepository userRepository,
            RestaurantProfileRepository restaurantProfileRepository,
            NgoProfileRepository ngoProfileRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            UserMapper userMapper) {
        this.userRepository = userRepository;
        this.restaurantProfileRepository = restaurantProfileRepository;
        this.ngoProfileRepository = ngoProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userMapper = userMapper;
    }

    @Override
    public JwtResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("Email is already registered");
        }

        // Validate payload constraints manually for conditional roles
        if (request.getRole() == Role.RESTAURANT && request.getRestaurantDetails() == null) {
            throw new IllegalArgumentException("Restaurant details are required for RESTAURANT registration");
        }
        if (request.getRole() == Role.NGO && request.getNgoDetails() == null) {
            throw new IllegalArgumentException("NGO details are required for NGO registration");
        }

        UserDocument user = UserDocument.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .role(request.getRole())
                .accountStatus(AccountStatus.ACTIVE) // Enabled to login
                .build();

        if (request.getRole() == Role.RESTAURANT) {
            RegisterRequest.RestaurantDetails details = request.getRestaurantDetails();
            if (details != null && restaurantProfileRepository.existsByLicenseNumber(details.getLicenseNumber())) {
                throw new DuplicateLicenseNumberException("License number already registered: " + details.getLicenseNumber());
            }
        } else if (request.getRole() == Role.NGO) {
            RegisterRequest.NgoDetails details = request.getNgoDetails();
            if (details != null && ngoProfileRepository.existsByRegistrationNumber(details.getRegistrationNumber())) {
                throw new DuplicateRegistrationNumberException("Registration number already registered: " + details.getRegistrationNumber());
            }
        }

        UserDocument savedUser = userRepository.save(user);
        RestaurantProfileDocument savedRestaurantProfile = null;
        NgoProfileDocument savedNgoProfile = null;

        try {
            if (request.getRole() == Role.RESTAURANT) {
                RegisterRequest.RestaurantDetails details = request.getRestaurantDetails();
                RestaurantProfileDocument profile = RestaurantProfileDocument.builder()
                        .userId(savedUser.getId())
                        .restaurantName(details.getRestaurantName())
                        .address(details.getAddress())
                        .coordinates(details.getCoordinates())
                        .licenseNumber(details.getLicenseNumber())
                        .verificationStatus(VerificationStatus.PENDING)
                        .build();
                savedRestaurantProfile = restaurantProfileRepository.save(profile);

            } else if (request.getRole() == Role.NGO) {
                RegisterRequest.NgoDetails details = request.getNgoDetails();
                NgoProfileDocument profile = NgoProfileDocument.builder()
                        .userId(savedUser.getId())
                        .ngoName(details.getNgoName())
                        .address(details.getAddress())
                        .coordinates(details.getCoordinates())
                        .capacity(details.getCapacity())
                        .storageType(details.getStorageType())
                        .registrationNumber(details.getRegistrationNumber())
                        .verificationStatus(VerificationStatus.PENDING)
                        .build();
                savedNgoProfile = ngoProfileRepository.save(profile);
            }
        } catch (Exception e) {
            // Orphan Prevention Rollback
            try {
                userRepository.delete(savedUser);
            } catch (Exception rollbackEx) {
                log.error("CRITICAL: Failed to clean up orphaned user document after profile creation failure. User ID: {}", savedUser.getId(), rollbackEx);
            }
            throw e;
        }

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                savedUser.getEmail(),
                savedUser.getPassword(),
                savedUser.getRole() == Role.ADMIN ? 
                        org.springframework.security.core.authority.AuthorityUtils.createAuthorityList("ROLE_ADMIN") :
                        savedUser.getRole() == Role.RESTAURANT ?
                                org.springframework.security.core.authority.AuthorityUtils.createAuthorityList("ROLE_RESTAURANT") :
                                org.springframework.security.core.authority.AuthorityUtils.createAuthorityList("ROLE_NGO")
        );

        String token = jwtService.generateToken(userDetails);
        UserResponse userResponse = userMapper.toUserResponse(savedUser, savedRestaurantProfile, savedNgoProfile);

        return JwtResponse.builder()
                .token(token)
                .expiresIn(jwtService.getExpirationTime())
                .role(savedUser.getRole())
                .user(userResponse)
                .build();
    }

    @Override
    public JwtResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        UserDocument user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        RestaurantProfileDocument restaurantProfile = null;
        NgoProfileDocument ngoProfile = null;

        if (user.getRole() == Role.RESTAURANT) {
            restaurantProfile = restaurantProfileRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new ProfileNotFoundException("Profile data missing for user"));
        } else if (user.getRole() == Role.NGO) {
            ngoProfile = ngoProfileRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new ProfileNotFoundException("Profile data missing for user"));
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(userDetails);
        UserResponse userResponse = userMapper.toUserResponse(user, restaurantProfile, ngoProfile);

        return JwtResponse.builder()
                .token(token)
                .expiresIn(jwtService.getExpirationTime())
                .role(user.getRole())
                .user(userResponse)
                .build();
    }

    @Override
    public JwtResponse adminLogin(LoginRequest request) {
        // Authenticate standard username and password
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        UserDocument user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        // Assert role is ADMIN. Mismatches must throw generic "Invalid credentials" error to avoid leaking details.
        if (user.getRole() != Role.ADMIN) {
            throw new BadCredentialsException("Invalid credentials");
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(userDetails);
        UserResponse userResponse = userMapper.toUserResponse(user, null, null);

        return JwtResponse.builder()
                .token(token)
                .expiresIn(jwtService.getExpirationTime())
                .role(user.getRole())
                .user(userResponse)
                .build();
    }
}
