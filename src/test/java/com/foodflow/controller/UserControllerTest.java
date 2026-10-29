package com.foodflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodflow.dto.ChangePasswordRequest;
import com.foodflow.dto.UserResponse;
import com.foodflow.enums.Role;
import com.foodflow.security.JwtAuthenticationEntryPoint;
import com.foodflow.service.JwtService;
import com.foodflow.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.foodflow.config.SecurityConfig;
import com.foodflow.security.JwtAuthenticationEntryPoint;
import com.foodflow.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Import;

@WebMvcTest(controllers = UserController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtAuthenticationEntryPoint.class})
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    public void testGetProfileUnauthenticatedReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/user/profile")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user@foodflow.com")
    public void testGetProfileAuthenticatedReturnsProfile() throws Exception {
        UserResponse mockUser = UserResponse.builder()
                .id("user-123")
                .email("user@foodflow.com")
                .fullName("John Doe")
                .role(Role.NGO)
                .build();

        when(userService.getCurrentUserProfile("user@foodflow.com")).thenReturn(mockUser);

        mockMvc.perform(get("/api/user/profile")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("user-123"))
                .andExpect(jsonPath("$.email").value("user@foodflow.com"))
                .andExpect(jsonPath("$.role").value("NGO"));
    }

    @Test
    @WithMockUser(username = "user@foodflow.com")
    public void testChangePasswordValidationErrors() throws Exception {
        // Password fields empty
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .oldPassword("")
                .newPassword("")
                .confirmNewPassword("")
                .build();

        mockMvc.perform(put("/api/user/change-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.oldPassword").value("Current password is required"))
                .andExpect(jsonPath("$.newPassword").exists());
    }

    @Test
    @WithMockUser(username = "user@foodflow.com")
    public void testChangePasswordServiceChecks() throws Exception {
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .oldPassword("oldPassword123")
                .newPassword("newPassword123")
                .confirmNewPassword("newPassword123")
                .build();

        // Simulate identical old and new password exception in service layer
        doThrow(new IllegalArgumentException("New password cannot be the same as old password"))
                .when(userService).changePassword(anyString(), any(ChangePasswordRequest.class));

        mockMvc.perform(put("/api/user/change-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("New password cannot be the same as old password"));
    }
}
