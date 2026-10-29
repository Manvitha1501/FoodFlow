package com.foodflow.service;

import com.foodflow.document.UserDocument;
import com.foodflow.dto.ChangePasswordRequest;
import com.foodflow.exception.InvalidPasswordException;
import com.foodflow.mapper.UserMapper;
import com.foodflow.repository.NgoProfileRepository;
import com.foodflow.repository.RestaurantProfileRepository;
import com.foodflow.repository.UserRepository;
import com.foodflow.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserServiceImplTest {

    private UserRepository userRepository;
    private RestaurantProfileRepository restaurantProfileRepository;
    private NgoProfileRepository ngoProfileRepository;
    private PasswordEncoder passwordEncoder;
    private UserMapper userMapper;

    private UserServiceImpl userService;

    @BeforeEach
    public void setUp() {
        userRepository = mock(UserRepository.class);
        restaurantProfileRepository = mock(RestaurantProfileRepository.class);
        ngoProfileRepository = mock(NgoProfileRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        userMapper = mock(UserMapper.class);

        userService = new UserServiceImpl(
                userRepository,
                restaurantProfileRepository,
                ngoProfileRepository,
                passwordEncoder,
                userMapper
        );
    }

    @Test
    public void testChangePasswordMismatchedOldPasswordThrowsException() {
        // Arrange
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .oldPassword("wrongOldPassword")
                .newPassword("newPassword123")
                .confirmNewPassword("newPassword123")
                .build();

        UserDocument user = UserDocument.builder()
                .email("test@foodflow.com")
                .password("encodedOldPassword")
                .build();

        when(userRepository.findByEmail("test@foodflow.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongOldPassword", "encodedOldPassword")).thenReturn(false);

        // Act & Assert
        assertThrows(InvalidPasswordException.class, () -> userService.changePassword("test@foodflow.com", request));
    }

    @Test
    public void testChangePasswordSameNewPasswordThrowsException() {
        // Arrange
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .oldPassword("oldPassword123")
                .newPassword("oldPassword123")
                .confirmNewPassword("oldPassword123")
                .build();

        UserDocument user = UserDocument.builder()
                .email("test@foodflow.com")
                .password("encodedOldPassword")
                .build();

        when(userRepository.findByEmail("test@foodflow.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPassword123", "encodedOldPassword")).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> userService.changePassword("test@foodflow.com", request));
    }

    @Test
    public void testChangePasswordConfirmMismatchThrowsException() {
        // Arrange
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .oldPassword("oldPassword123")
                .newPassword("newPassword123")
                .confirmNewPassword("confirmMismatch")
                .build();

        UserDocument user = UserDocument.builder()
                .email("test@foodflow.com")
                .password("encodedOldPassword")
                .build();

        when(userRepository.findByEmail("test@foodflow.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPassword123", "encodedOldPassword")).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> userService.changePassword("test@foodflow.com", request));
    }

    @Test
    public void testChangePasswordSuccess() {
        // Arrange
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .oldPassword("oldPassword123")
                .newPassword("newPassword123")
                .confirmNewPassword("newPassword123")
                .build();

        UserDocument user = UserDocument.builder()
                .email("test@foodflow.com")
                .password("encodedOldPassword")
                .build();

        when(userRepository.findByEmail("test@foodflow.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPassword123", "encodedOldPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword123")).thenReturn("encodedNewPassword");

        // Act
        userService.changePassword("test@foodflow.com", request);

        // Assert
        verify(userRepository).save(user);
        assertEquals("encodedNewPassword", user.getPassword());
    }
}
