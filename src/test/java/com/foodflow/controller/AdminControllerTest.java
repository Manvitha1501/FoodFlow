package com.foodflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodflow.dto.JwtResponse;
import com.foodflow.dto.LoginRequest;
import com.foodflow.enums.Role;
import com.foodflow.security.JwtAuthenticationEntryPoint;
import com.foodflow.service.AuthService;
import com.foodflow.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.foodflow.config.SecurityConfig;
import com.foodflow.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Import;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.foodflow.security.JwtAuthenticationEntryPoint;

@WebMvcTest(controllers = AdminController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtAuthenticationEntryPoint.class})
public class AdminControllerTest {

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
    public void testAdminLoginSuccessfully() throws Exception {
        LoginRequest request = new LoginRequest("admin@foodflow.com", "adminPass123");
        JwtResponse response = JwtResponse.builder()
                .token("admin_jwt_token")
                .expiresIn(3600000L)
                .role(Role.ADMIN)
                .build();

        when(authService.adminLogin(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/admin/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("admin_jwt_token"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    @WithMockUser(username = "admin@foodflow.com", roles = {"ADMIN"})
    public void testGetDashboardAsAdminReturnsDashboard() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Welcome to the Admin Command Center. System status: NORMAL. Matching Accuracy: 98.4%"));
    }

    @Test
    @WithMockUser(username = "ngo@foodflow.com", roles = {"NGO"})
    public void testGetDashboardAsNgoReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "restaurant@foodflow.com", roles = {"RESTAURANT"})
    public void testGetDashboardAsRestaurantReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
