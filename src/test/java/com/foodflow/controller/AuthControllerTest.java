package com.foodflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodflow.dto.JwtResponse;
import com.foodflow.dto.LoginRequest;
import com.foodflow.dto.RegisterRequest;
import com.foodflow.enums.Role;
import com.foodflow.exception.DuplicateEmailException;
import com.foodflow.security.JwtAuthenticationEntryPoint;
import com.foodflow.service.AuthService;
import com.foodflow.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import com.foodflow.config.SecurityConfig;
import com.foodflow.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Import;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.foodflow.security.JwtAuthenticationEntryPoint;

@WebMvcTest(controllers = AuthController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtAuthenticationEntryPoint.class})
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    public void testRegisterSuccessfully() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email("newuser@foodflow.com")
                .password("password123")
                .fullName("John Doe")
                .phoneNumber("1234567890")
                .role(Role.ADMIN)
                .build();

        JwtResponse response = JwtResponse.builder()
                .token("mocked_jwt_token")
                .expiresIn(3600000L)
                .role(Role.ADMIN)
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("mocked_jwt_token"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    public void testRegisterInvalidEmailReturnsBadRequest() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email("invalid-email")
                .password("password123")
                .fullName("John Doe")
                .phoneNumber("1234567890")
                .role(Role.ADMIN)
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").value("Invalid email format"));
    }

    @Test
    public void testRegisterDuplicateEmailReturnsConflict() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email("duplicate@foodflow.com")
                .password("password123")
                .fullName("John Doe")
                .phoneNumber("1234567890")
                .role(Role.ADMIN)
                .build();

        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new DuplicateEmailException("Email is already registered"));

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email is already registered"));
    }

    @Test
    public void testLoginSuccessfully() throws Exception {
        LoginRequest request = new LoginRequest("user@foodflow.com", "password123");
        JwtResponse response = JwtResponse.builder()
                .token("mocked_jwt_token")
                .expiresIn(3600000L)
                .role(Role.NGO)
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mocked_jwt_token"))
                .andExpect(jsonPath("$.role").value("NGO"));
    }
}
